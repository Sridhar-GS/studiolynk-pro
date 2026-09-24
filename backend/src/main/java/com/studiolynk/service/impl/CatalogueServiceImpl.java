package com.studiolynk.service.impl;

import com.studiolynk.exception.ResourceNotFoundException;
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
@Transactional
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
    @Transactional(readOnly = true)
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceEntity> getAllServices() {
        return serviceRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentCategory> getAllEquipmentCategories() {
        return equipmentCategoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Equipment> getEquipmentByCategory(Long categoryId) {
        return equipmentRepository.findByCategoryId(categoryId);
    }

    @Override
    public Skill createCustomSkill(String name, Long userId) {
        String trimmed = name.trim();
        return skillRepository.findByNameIgnoreCase(trimmed)
                .orElseGet(() -> {
                    Skill newSkill = new Skill(trimmed, true);
                    newSkill.setCreatedByUserId(userId);
                    return skillRepository.save(newSkill);
                });
    }

    @Override
    public ServiceEntity createCustomService(String name, Long userId) {
        String trimmed = name.trim();
        return serviceRepository.findByNameIgnoreCase(trimmed)
                .orElseGet(() -> {
                    ServiceEntity newService = new ServiceEntity(trimmed, true);
                    newService.setCreatedByUserId(userId);
                    return serviceRepository.save(newService);
                });
    }

    @Override
    public Equipment createCustomEquipment(Long categoryId, String name, Long userId) {
        String trimmed = name.trim();
        EquipmentCategory category = equipmentCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment category not found with id: " + categoryId));

        return equipmentRepository.findByCategoryId(categoryId).stream()
                .filter(e -> e.getName().equalsIgnoreCase(trimmed))
                .findFirst()
                .orElseGet(() -> {
                    Equipment newEq = new Equipment(category, trimmed, true);
                    newEq.setCreatedByUserId(userId);
                    return equipmentRepository.save(newEq);
                });
    }
}
