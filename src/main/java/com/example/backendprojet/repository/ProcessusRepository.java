package com.example.backendprojet.repository;

import com.example.backendprojet.entity.Processus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessusRepository extends JpaRepository<Processus, Long> {

    @EntityGraph(attributePaths = {"taches"})
    @Query("SELECT DISTINCT p FROM Processus p")
    List<Processus> findAllWithTaches();

    @EntityGraph(attributePaths = {"taches"})
    Optional<Processus> findWithTachesById(Long id);
}