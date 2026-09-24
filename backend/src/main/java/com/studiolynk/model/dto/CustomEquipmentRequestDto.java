package com.studiolynk.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CustomEquipmentRequestDto {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Equipment name is required")
    @Size(max = 150, message = "Equipment name must not exceed 150 characters")
    private String name;

    public CustomEquipmentRequestDto() {
    }

    public CustomEquipmentRequestDto(Long categoryId, String name) {
        this.categoryId = categoryId;
        this.name = name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
