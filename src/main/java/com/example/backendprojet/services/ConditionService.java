package com.example.backendprojet.services;

import com.example.backendprojet.entity.Condition;
import com.example.backendprojet.repository.ConditionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConditionService {

    private final ConditionRepository conditionRepository;

    public ConditionService(ConditionRepository conditionRepository) {
        this.conditionRepository = conditionRepository;
    }

    public List<Condition> findByRegleId(Long regleId) {
        return conditionRepository.findByRegleMetier_Id(regleId);
    }

    public List<Condition> getAll() {
        return conditionRepository.findAll();
    }
}