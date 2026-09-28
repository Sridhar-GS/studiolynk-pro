package com.studiolynk.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

public class UpdateAvailabilityRequestDto {

    @NotEmpty(message = "Availability entries list cannot be empty")
    @Valid
    private List<UpdateAvailabilityItemDto> availability = new ArrayList<>();

    public UpdateAvailabilityRequestDto() {
    }

    public UpdateAvailabilityRequestDto(List<UpdateAvailabilityItemDto> availability) {
        this.availability = availability != null ? availability : new ArrayList<>();
    }

    public List<UpdateAvailabilityItemDto> getAvailability() {
        return availability;
    }

    public void setAvailability(List<UpdateAvailabilityItemDto> availability) {
        this.availability = availability;
    }
}
