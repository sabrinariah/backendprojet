package com.example.backendprojet.repository;

import com.example.backendprojet.entity.Tache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TacheRepository extends JpaRepository<Tache, Long> {
    List<Tache> findByProcessusId(Long processusId); // Spring Data derives this automatically
}