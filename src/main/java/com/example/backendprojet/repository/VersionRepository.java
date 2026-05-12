package com.example.backendprojet.repository;

import com.example.backendprojet.entity.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VersionRepository extends JpaRepository<Version, Long> {

    // ✅ Même règle : utiliser "RegleMetier" pas "Regle"
    List<Version> findByRegleMetier_Id(Long regleId);
}