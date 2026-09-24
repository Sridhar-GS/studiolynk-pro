package com.studiolynk.repository;

import com.studiolynk.model.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByCategoryId(Long categoryId);
    Optional<Equipment> findByNameIgnoreCase(String name);
}
