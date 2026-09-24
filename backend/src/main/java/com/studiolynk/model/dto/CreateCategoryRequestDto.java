package com.studiolynk.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCategoryRequestDto {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    private Integer sortOrder;

    public CreateCategoryRequestDto() {
    }

    public CreateCategoryRequestDto(String name, Integer sortOrder) {
        this.name = name;
        this.sortOrder = sortOrder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
