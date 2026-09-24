package com.studiolynk.service.impl;

import com.studiolynk.model.entity.Equipment;
import com.studiolynk.model.entity.EquipmentCategory;
import com.studiolynk.model.entity.ServiceEntity;
import com.studiolynk.model.entity.Skill;
import com.studiolynk.repository.EquipmentCategoryRepository;
import com.studiolynk.repository.EquipmentRepository;
import com.studiolynk.repository.ServiceRepository;
import com.studiolynk.repository.SkillRepository;
import com.studiolynk.service.CatalogueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogueServiceImpl implements CatalogueService {

    private final SkillRepository skillRepository;
    private final ServiceRepository serviceRepository;
    private final EquipmentCategoryRepository equipmentCategoryRepository;
    private final EquipmentRepository equipmentRepository;

    public CatalogueServiceImpl(
            SkillRepository skillRepository,
            ServiceRepository serviceRepository,
            EquipmentCategoryRepository equipmentCategoryRepository,
            EquipmentRepository equipmentRepository) {
        this.skillRepository = skillRepository;
        this.serviceRepository = serviceRepository;
        this.equipmentCategoryRepository = equipmentCategoryRepository;
        this.equipmentRepository = equipmentRepository;
    }

    @Override
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    @Override
    public List<ServiceEntity> getAllServices() {
        return serviceRepository.findAll();
    }

    @Override
    public List<EquipmentCategory> getAllEquipmentCategories() {
        return equipmentCategoryRepository.findAll();
    }

    @Override
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    @Override
    public List<Equipment> getEquipmentByCategory(Long categoryId) {
        return equipmentRepository.findByCategoryId(categoryId);
    }
}
