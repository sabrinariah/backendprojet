package com.example.backendprojet.repository;

import com.example.backendprojet.entity.RegleMetier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegleMetierRepository extends JpaRepository<RegleMetier, Long> {
}