package com.studiolynk.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CreateWorkRequestDto {

    @NotNull(message = "Requirement ID is required")
    private Long requirementId;

    @NotNull(message = "Freelancer ID is required")
    private Long freelancerId;

    @DecimalMin(value = "0.00", message = "Offered price cannot be negative")
    private BigDecimal offeredPrice;

    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    private String message;

    public CreateWorkRequestDto() {
    }

    public CreateWorkRequestDto(Long requirementId, Long freelancerId, BigDecimal offeredPrice, String message) {
        this.requirementId = requirementId;
        this.freelancerId = freelancerId;
        this.offeredPrice = offeredPrice;
        this.message = message;
    }

    public Long getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(Long requirementId) {
        this.requirementId = requirementId;
    }

    public Long getFreelancerId() {
        return freelancerId;
    }

    public void setFreelancerId(Long freelancerId) {
        this.freelancerId = freelancerId;
    }

    public BigDecimal getOfferedPrice() {
        return offeredPrice;
    }

    public void setOfferedPrice(BigDecimal offeredPrice) {
        this.offeredPrice = offeredPrice;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
