package com.studiolynk.model.dto;

import com.studiolynk.model.enums.AvailabilityStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class AvailabilitySlotDto {

    private Long id;
    private LocalDate date;
    private String dayOfWeek;
    private int dayOfMonth;
    private String month;
    private AvailabilityStatus status;
    private LocalTime startTime;
    private LocalTime endTime;
    private String formattedTime;
    private boolean withinWindow;
    private boolean available;

    public AvailabilitySlotDto() {
    }

    public AvailabilitySlotDto(Long id, LocalDate date, AvailabilityStatus status, LocalTime startTime, LocalTime endTime, boolean withinWindow) {
        this.id = id;
        this.date = date;
        this.dayOfWeek = date != null ? date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) : "";
        this.dayOfMonth = date != null ? date.getDayOfMonth() : 0;
        this.month = date != null ? date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) : "";
        this.status = status != null ? status : AvailabilityStatus.NOT_SET;
        this.startTime = startTime;
        this.endTime = endTime;
        this.withinWindow = withinWindow;
        this.available = this.status == AvailabilityStatus.AVAILABLE;

        if (this.status == AvailabilityStatus.AVAILABLE && startTime != null && endTime != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            this.formattedTime = startTime.format(formatter) + " - " + endTime.format(formatter);
        } else {
            this.formattedTime = null;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public AvailabilityStatus getStatus() {
        return status;
    }

    public void setStatus(AvailabilityStatus status) {
        this.status = status;
        this.available = this.status == AvailabilityStatus.AVAILABLE;
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

    public String getFormattedTime() {
        return formattedTime;
    }

    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }

    public boolean isWithinWindow() {
        return withinWindow;
    }

    public void setWithinWindow(boolean withinWindow) {
        this.withinWindow = withinWindow;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
