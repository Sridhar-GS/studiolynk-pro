package com.studiolynk.repository;

import com.studiolynk.model.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    Optional<ServiceEntity> findByNameIgnoreCase(String name);
    List<ServiceEntity> findByIsCustomFalse();
}
