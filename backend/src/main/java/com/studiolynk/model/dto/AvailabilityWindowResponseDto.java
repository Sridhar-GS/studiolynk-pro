package com.studiolynk.model.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AvailabilityWindowResponseDto {

    private Long freelancerId;
    private String freelancerName;
    private LocalDate windowStartDate;
    private LocalDate windowEndDate;
    private int totalDays;
    private int availableDaysCount;
    private int busyDaysCount;
    private int notSetDaysCount;
    private List<AvailabilitySlotDto> slots = new ArrayList<>();

    public AvailabilityWindowResponseDto() {
    }

    public AvailabilityWindowResponseDto(Long freelancerId, String freelancerName, LocalDate windowStartDate,
                                         LocalDate windowEndDate, List<AvailabilitySlotDto> slots) {
        this.freelancerId = freelancerId;
        this.freelancerName = freelancerName;
        this.windowStartDate = windowStartDate;
        this.windowEndDate = windowEndDate;
        this.slots = slots != null ? slots : new ArrayList<>();
        this.totalDays = this.slots.size();

        int avail = 0;
        int busy = 0;
        int notSet = 0;
        for (AvailabilitySlotDto slot : this.slots) {
            if (slot.getStatus() == null) {
                notSet++;
            } else {
                switch (slot.getStatus()) {
                    case AVAILABLE -> avail++;
                    case BUSY -> busy++;
                    case NOT_SET -> notSet++;
                }
            }
        }
        this.availableDaysCount = avail;
        this.busyDaysCount = busy;
        this.notSetDaysCount = notSet;
    }

    public Long getFreelancerId() {
        return freelancerId;
    }

    public void setFreelancerId(Long freelancerId) {
        this.freelancerId = freelancerId;
    }

    public String getFreelancerName() {
        return freelancerName;
    }

    public void setFreelancerName(String freelancerName) {
        this.freelancerName = freelancerName;
    }

    public LocalDate getWindowStartDate() {
        return windowStartDate;
    }

    public void setWindowStartDate(LocalDate windowStartDate) {
        this.windowStartDate = windowStartDate;
    }

    public LocalDate getWindowEndDate() {
        return windowEndDate;
    }

    public void setWindowEndDate(LocalDate windowEndDate) {
        this.windowEndDate = windowEndDate;
    }

    public int getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(int totalDays) {
        this.totalDays = totalDays;
    }

    public int getAvailableDaysCount() {
        return availableDaysCount;
    }

    public void setAvailableDaysCount(int availableDaysCount) {
        this.availableDaysCount = availableDaysCount;
    }

    public int getBusyDaysCount() {
        return busyDaysCount;
    }

    public void setBusyDaysCount(int busyDaysCount) {
        this.busyDaysCount = busyDaysCount;
    }

    public int getNotSetDaysCount() {
        return notSetDaysCount;
    }

    public void setNotSetDaysCount(int notSetDaysCount) {
        this.notSetDaysCount = notSetDaysCount;
    }

    public List<AvailabilitySlotDto> getSlots() {
        return slots;
    }

    public void setSlots(List<AvailabilitySlotDto> slots) {
        this.slots = slots;
    }
}
