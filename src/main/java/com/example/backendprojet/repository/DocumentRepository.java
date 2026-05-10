package com.example.backendprojet.repository;

import com.example.backendprojet.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByDossierId(UUID dossierId);
    List<Document> findByDossierIdAndType(UUID dossierId, String type);
}