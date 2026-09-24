package com.studiolynk.repository;

import com.studiolynk.model.entity.EquipmentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipmentCategoryRepository extends JpaRepository<EquipmentCategory, Long> {
    Optional<EquipmentCategory> findByNameIgnoreCase(String name);
}
