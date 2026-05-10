package com.example.backendprojet.repository;

import com.example.backendprojet.entity.Exportateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExportateurRepository extends JpaRepository<Exportateur, UUID> {
    Optional<Exportateur> findByNumeroContribuable(String numero);
}
