package com.studiolynk.repository;

import com.studiolynk.model.entity.StudioSocialLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudioSocialLinkRepository extends JpaRepository<StudioSocialLink, Long> {
    List<StudioSocialLink> findByStudioId(Long studioId);
    void deleteByStudioId(Long studioId);
}
