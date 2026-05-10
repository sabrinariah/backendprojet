package com.example.backendprojet.repository;



import com.example.backendprojet.entity.Processus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessusRepository extends JpaRepository<Processus, Long> {
}