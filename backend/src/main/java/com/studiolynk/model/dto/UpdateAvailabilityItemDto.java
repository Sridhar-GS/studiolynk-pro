package com.studiolynk.model.dto;

import com.studiolynk.model.enums.AvailabilityStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class UpdateAvailabilityItemDto {

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Status is required (AVAILABLE, BUSY, or NOT_SET)")
    private AvailabilityStatus status;

    private LocalTime startTime;

    private LocalTime endTime;

    public UpdateAvailabilityItemDto() {
    }

    public UpdateAvailabilityItemDto(LocalDate date, AvailabilityStatus status, LocalTime startTime, LocalTime endTime) {
        this.date = date;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public AvailabilityStatus getStatus() {
        return status;
    }

    public void setStatus(AvailabilityStatus status) {
        this.status = status;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}
