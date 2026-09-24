package com.studiolynk.model.dto;

public class SkillDto {
    private Long id;
    private String name;
    private boolean isCustom;

    public SkillDto() {
    }

    public SkillDto(Long id, String name, boolean isCustom) {
        this.id = id;
        this.name = name;
        this.isCustom = isCustom;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }
}
