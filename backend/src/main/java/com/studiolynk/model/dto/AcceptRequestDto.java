package com.studiolynk.model.dto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public class AcceptRequestDto {

    @DecimalMin(value = "0.00", message = "Agreed price cannot be negative")
    private BigDecimal agreedPrice;

    public AcceptRequestDto() {
    }

    public AcceptRequestDto(BigDecimal agreedPrice) {
        this.agreedPrice = agreedPrice;
    }

    public BigDecimal getAgreedPrice() {
        return agreedPrice;
    }

    public void setAgreedPrice(BigDecimal agreedPrice) {
        this.agreedPrice = agreedPrice;
    }
}
