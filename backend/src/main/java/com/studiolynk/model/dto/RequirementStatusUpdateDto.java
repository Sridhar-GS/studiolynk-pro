package com.studiolynk.model.dto;

import com.studiolynk.model.enums.RequirementStatus;
import jakarta.validation.constraints.NotNull;

public class RequirementStatusUpdateDto {

    @NotNull(message = "New requirement status is required")
    private RequirementStatus status;

    public RequirementStatusUpdateDto() {
    }

    public RequirementStatusUpdateDto(RequirementStatus status) {
        this.status = status;
    }

    public RequirementStatus getStatus() {
        return status;
    }

    public void setStatus(RequirementStatus status) {
        this.status = status;
    }
}
