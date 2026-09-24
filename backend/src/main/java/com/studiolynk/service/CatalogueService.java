package com.studiolynk.service;

import com.studiolynk.model.entity.Equipment;
import com.studiolynk.model.entity.EquipmentCategory;
import com.studiolynk.model.entity.ServiceEntity;
import com.studiolynk.model.entity.Skill;

import java.util.List;

public interface CatalogueService {
    List<Skill> getAllSkills();
    List<ServiceEntity> getAllServices();
    List<EquipmentCategory> getAllEquipmentCategories();
    List<Equipment> getAllEquipment();
    List<Equipment> getEquipmentByCategory(Long categoryId);

    // Custom catalogue additions (FRL-002, FRL-003, FRL-005)
    Skill createCustomSkill(String name, Long userId);
    ServiceEntity createCustomService(String name, Long userId);
    Equipment createCustomEquipment(Long categoryId, String name, Long userId);
}
