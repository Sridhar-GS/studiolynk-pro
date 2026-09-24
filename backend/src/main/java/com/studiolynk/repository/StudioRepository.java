package com.studiolynk.repository;

import com.studiolynk.model.entity.Studio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudioRepository extends JpaRepository<Studio, Long> {
    Optional<Studio> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
