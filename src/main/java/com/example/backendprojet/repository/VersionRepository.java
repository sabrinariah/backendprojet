package com.example.backendprojet.repository;

import com.example.backendprojet.entity.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VersionRepository extends JpaRepository<Version, Long> {

    // ✅ Méthode existante conservée
    List<Version> findByRegleMetier_Id(Long regleId);

    // ✅ FIX suppression : grâce au CASCADE ALL dans RegleMetier,
    // cette méthode n'est plus nécessaire pour le delete,
    // mais on la garde pour usage explicite si besoin
    void deleteByRegleMetier_Id(Long regleId);
}
