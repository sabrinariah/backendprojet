package com.example.backendprojet.repository;

import com.example.backendprojet.entity.Condition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConditionRepository extends JpaRepository<Condition, Long> {

    // ✅ CORRECT
    List<Condition> findByRegleMetier_Id(Long regleId);
}