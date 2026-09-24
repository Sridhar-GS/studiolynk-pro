package com.studiolynk.model.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class ReorderItemsRequestDto {

    @NotEmpty(message = "Item IDs list cannot be empty")
    private List<Long> itemIds;

    public ReorderItemsRequestDto() {
    }

    public ReorderItemsRequestDto(List<Long> itemIds) {
        this.itemIds = itemIds;
    }

    public List<Long> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<Long> itemIds) {
        this.itemIds = itemIds;
    }
}
