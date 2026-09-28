package com.studiolynk.repository;

import com.studiolynk.model.entity.FreelancerAvailability;
import com.studiolynk.model.enums.AvailabilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FreelancerAvailabilityRepository extends JpaRepository<FreelancerAvailability, Long> {

    List<FreelancerAvailability> findByFreelancerIdAndAvailableDateBetweenOrderByAvailableDateAsc(
            Long freelancerId, LocalDate startDate, LocalDate endDate);

    Optional<FreelancerAvailability> findByFreelancerIdAndAvailableDate(Long freelancerId, LocalDate availableDate);

    void deleteByFreelancerIdAndAvailableDateBetween(Long freelancerId, LocalDate startDate, LocalDate endDate);

    List<FreelancerAvailability> findByFreelancerId(Long freelancerId);

    boolean existsByFreelancerIdAndAvailableDateAndStatus(Long freelancerId, LocalDate availableDate, AvailabilityStatus status);
}
