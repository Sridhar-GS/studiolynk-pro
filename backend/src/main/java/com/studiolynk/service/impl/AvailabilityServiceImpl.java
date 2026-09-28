package com.studiolynk.service.impl;

import com.studiolynk.exception.BadRequestException;
import com.studiolynk.exception.ResourceNotFoundException;
import com.studiolynk.model.dto.AvailabilityCheckRequestDto;
import com.studiolynk.model.dto.AvailabilityCheckResponseDto;
import com.studiolynk.model.dto.AvailabilitySlotDto;
import com.studiolynk.model.dto.AvailabilityWindowResponseDto;
import com.studiolynk.model.dto.UpdateAvailabilityItemDto;
import com.studiolynk.model.dto.UpdateAvailabilityRequestDto;
import com.studiolynk.model.entity.Freelancer;
import com.studiolynk.model.entity.FreelancerAvailability;
import com.studiolynk.model.entity.User;
import com.studiolynk.model.enums.AvailabilityStatus;
import com.studiolynk.repository.FreelancerAvailabilityRepository;
import com.studiolynk.repository.FreelancerRepository;
import com.studiolynk.repository.UserRepository;
import com.studiolynk.service.AvailabilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AvailabilityServiceImpl implements AvailabilityService {

    private static final Logger log = LoggerFactory.getLogger(AvailabilityServiceImpl.class);
    private static final int ROLLING_WINDOW_DAYS = 10;

    private final FreelancerAvailabilityRepository availabilityRepository;
    private final FreelancerRepository freelancerRepository;
    private final UserRepository userRepository;

    public AvailabilityServiceImpl(FreelancerAvailabilityRepository availabilityRepository,
                                  FreelancerRepository freelancerRepository,
                                  UserRepository userRepository) {
        this.availabilityRepository = availabilityRepository;
        this.freelancerRepository = freelancerRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AvailabilityWindowResponseDto getMyAvailability(String userEmail) {
        Freelancer freelancer = getFreelancerByUserEmail(userEmail);
        return getAvailabilityWindowForFreelancer(freelancer.getId());
    }

    @Override
    @Transactional
    public AvailabilityWindowResponseDto updateMyAvailability(String userEmail, UpdateAvailabilityRequestDto request) {
        Freelancer freelancer = getFreelancerByUserEmail(userEmail);

        if (request == null || request.getAvailability() == null || request.getAvailability().isEmpty()) {
            throw new BadRequestException("Availability update request cannot be empty.");
        }

        LocalDate windowStart = LocalDate.now();
        LocalDate windowEnd = windowStart.plusDays(ROLLING_WINDOW_DAYS - 1);

        for (UpdateAvailabilityItemDto item : request.getAvailability()) {
            if (item.getDate() == null) {
                throw new BadRequestException("Date is required for all availability entries.");
            }

            // Cannot update dates in the past
            if (item.getDate().isBefore(windowStart)) {
                throw new BadRequestException("Cannot update availability for past date: " + item.getDate());
            }

            // Cannot update dates beyond the rolling 10-day window
            if (item.getDate().isAfter(windowEnd)) {
                throw new BadRequestException("Date " + item.getDate() + " is beyond the rolling " + ROLLING_WINDOW_DAYS + "-day window (ending on " + windowEnd + ").");
            }

            if (item.getStatus() == null) {
                throw new BadRequestException("Status is required for date: " + item.getDate());
            }

            // Validation for AVAILABLE status (AVL-003)
            LocalTime startTime = item.getStartTime();
            LocalTime endTime = item.getEndTime();

            if (item.getStatus() == AvailabilityStatus.AVAILABLE) {
                if (startTime == null || endTime == null) {
                    throw new BadRequestException("Start time and end time are required when marked AVAILABLE on " + item.getDate());
                }
                if (!startTime.isBefore(endTime)) {
                    throw new BadRequestException("Start time (" + startTime + ") must be before end time (" + endTime + ") on " + item.getDate());
                }
            } else {
                // BUSY or NOT_SET clear the specific time interval
                startTime = null;
                endTime = null;
            }

            // Upsert availability record
            Optional<FreelancerAvailability> existingOpt = availabilityRepository
                    .findByFreelancerIdAndAvailableDate(freelancer.getId(), item.getDate());

            FreelancerAvailability record;
            if (existingOpt.isPresent()) {
                record = existingOpt.get();
                record.setStatus(item.getStatus());
                record.setStartTime(startTime);
                record.setEndTime(endTime);
            } else {
                record = new FreelancerAvailability(freelancer, item.getDate(), item.getStatus());
                record.setStartTime(startTime);
                record.setEndTime(endTime);
            }
            availabilityRepository.save(record);
        }

        log.info("Updated availability for freelancer id {} across {} dates", freelancer.getId(), request.getAvailability().size());
        return getAvailabilityWindowForFreelancer(freelancer.getId());
    }

    @Override
    @Transactional
    public AvailabilityWindowResponseDto resetMyAvailability(String userEmail) {
        Freelancer freelancer = getFreelancerByUserEmail(userEmail);

        LocalDate windowStart = LocalDate.now();
        LocalDate windowEnd = windowStart.plusDays(ROLLING_WINDOW_DAYS - 1);

        // Delete all availability records in the rolling window to reset back to NOT_SET
        availabilityRepository.deleteByFreelancerIdAndAvailableDateBetween(freelancer.getId(), windowStart, windowEnd);
        log.info("Reset availability for freelancer id {} back to NOT_SET across window {} to {}", freelancer.getId(), windowStart, windowEnd);

        return getAvailabilityWindowForFreelancer(freelancer.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public AvailabilityWindowResponseDto getAvailabilityWindowForFreelancer(Long freelancerId) {
        Freelancer freelancer = freelancerRepository.findById(freelancerId)
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer not found with id: " + freelancerId));

        LocalDate windowStart = LocalDate.now();
        LocalDate windowEnd = windowStart.plusDays(ROLLING_WINDOW_DAYS - 1);

        // Fetch persisted availability records for the 10-day window
        List<FreelancerAvailability> records = availabilityRepository
                .findByFreelancerIdAndAvailableDateBetweenOrderByAvailableDateAsc(freelancerId, windowStart, windowEnd);

        Map<LocalDate, FreelancerAvailability> recordMap = records.stream()
                .collect(Collectors.toMap(FreelancerAvailability::getAvailableDate, Function.identity()));

        List<AvailabilitySlotDto> slots = new ArrayList<>(ROLLING_WINDOW_DAYS);

        // Generate full 10-day consecutive sequence
        for (int i = 0; i < ROLLING_WINDOW_DAYS; i++) {
            LocalDate currentDate = windowStart.plusDays(i);
            FreelancerAvailability record = recordMap.get(currentDate);

            if (record != null) {
                slots.add(new AvailabilitySlotDto(
                        record.getId(),
                        currentDate,
                        record.getStatus(),
                        record.getStartTime(),
                        record.getEndTime(),
                        true
                ));
            } else {
                // Default to NOT_SET with null times when no record exists (AVL-001, AVL-002)
                slots.add(new AvailabilitySlotDto(
                        null,
                        currentDate,
                        AvailabilityStatus.NOT_SET,
                        null,
                        null,
                        true
                ));
            }
        }

        String displayName = freelancer.getUser() != null ? freelancer.getUser().getEmail() : "Freelancer #" + freelancerId;
        return new AvailabilityWindowResponseDto(freelancerId, displayName, windowStart, windowEnd, slots);
    }

    @Override
    @Transactional(readOnly = true)
    public AvailabilityCheckResponseDto checkAvailability(Long freelancerId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (freelancerId == null) {
            throw new BadRequestException("Freelancer ID is required for availability check.");
        }
        if (date == null) {
            throw new BadRequestException("Date is required for availability check.");
        }

        if (!freelancerRepository.existsById(freelancerId)) {
            throw new ResourceNotFoundException("Freelancer not found with id: " + freelancerId);
        }

        LocalDate windowStart = LocalDate.now();
        LocalDate windowEnd = windowStart.plusDays(ROLLING_WINDOW_DAYS - 1);

        // Date in the past
        if (date.isBefore(windowStart)) {
            return new AvailabilityCheckResponseDto(
                    freelancerId, date, startTime, endTime, false, null, null, null, false,
                    "Requested date is in the past."
            );
        }

        // AVL-006: Dates beyond the 10-day window
        if (date.isAfter(windowEnd)) {
            return new AvailabilityCheckResponseDto(
                    freelancerId, date, startTime, endTime, false, AvailabilityStatus.NOT_SET, null, null, false,
                    "Beyond the rolling 10-day scheduling window. Availability is not set / unknown."
            );
        }

        // Inside the rolling 10-day window
        Optional<FreelancerAvailability> availOpt = availabilityRepository.findByFreelancerIdAndAvailableDate(freelancerId, date);

        if (availOpt.isEmpty() || availOpt.get().getStatus() == AvailabilityStatus.NOT_SET) {
            // AVL-005: NOT_SET days shall not appear as available
            return new AvailabilityCheckResponseDto(
                    freelancerId, date, startTime, endTime, true, AvailabilityStatus.NOT_SET, null, null, false,
                    "Freelancer availability is NOT SET for this date. Excluded from available search."
            );
        }

        FreelancerAvailability record = availOpt.get();

        if (record.getStatus() == AvailabilityStatus.BUSY) {
            // AVL-005: BUSY days shall not appear as available
            return new AvailabilityCheckResponseDto(
                    freelancerId, date, startTime, endTime, true, AvailabilityStatus.BUSY, null, null, false,
                    "Freelancer is marked BUSY on this date. Excluded from available search."
            );
        }

        // Status is AVAILABLE (AVL-004)
        LocalTime availStart = record.getStartTime();
        LocalTime availEnd = record.getEndTime();

        if (startTime != null && endTime != null) {
            if (availStart != null && availEnd != null) {
                // Requested interval must fall within or match freelancer's available time interval
                boolean coversStart = !startTime.isBefore(availStart);
                boolean coversEnd = !endTime.isAfter(availEnd);

                if (coversStart && coversEnd) {
                    return new AvailabilityCheckResponseDto(
                            freelancerId, date, startTime, endTime, true, AvailabilityStatus.AVAILABLE,
                            availStart, availEnd, true,
                            "Freelancer is AVAILABLE for the requested date and time interval (" + startTime + " - " + endTime + ")."
                    );
                } else {
                    return new AvailabilityCheckResponseDto(
                            freelancerId, date, startTime, endTime, true, AvailabilityStatus.AVAILABLE,
                            availStart, availEnd, false,
                            "Requested time (" + startTime + " - " + endTime + ") is outside freelancer's available hours (" + availStart + " - " + availEnd + ")."
                    );
                }
            }
        }

        // Available on this date without specific time restriction
        return new AvailabilityCheckResponseDto(
                freelancerId, date, startTime, endTime, true, AvailabilityStatus.AVAILABLE,
                availStart, availEnd, true,
                "Freelancer is AVAILABLE on this date."
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AvailabilityCheckResponseDto checkAvailability(AvailabilityCheckRequestDto request) {
        if (request == null) {
            throw new BadRequestException("Availability check request cannot be null.");
        }
        return checkAvailability(request.getFreelancerId(), request.getDate(), request.getStartTime(), request.getEndTime());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> filterAvailableFreelancerIds(List<Long> freelancerIds, LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (freelancerIds == null || freelancerIds.isEmpty() || date == null) {
            return new ArrayList<>();
        }

        List<Long> availableList = new ArrayList<>();
        for (Long freelancerId : freelancerIds) {
            try {
                AvailabilityCheckResponseDto result = checkAvailability(freelancerId, date, startTime, endTime);
                if (result.isMatch()) {
                    availableList.add(freelancerId);
                }
            } catch (Exception e) {
                log.warn("Error checking availability for freelancer id {}: {}", freelancerId, e.getMessage());
            }
        }
        return availableList;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasConflict(Long freelancerId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        // AVL-008, AVL-009: Conflict detection
        // If not available or requested time outside available interval, it is a conflict
        AvailabilityCheckResponseDto check = checkAvailability(freelancerId, date, startTime, endTime);
        return !check.isMatch();
    }

    private Freelancer getFreelancerByUserEmail(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        return freelancerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found for user: " + userEmail));
    }
}
