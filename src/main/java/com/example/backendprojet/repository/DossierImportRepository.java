package com.example.backendprojet.repository;



import com.example.backendprojet.entity.DossierImport;
import com.example.backendprojet.entity.StatutDossier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Spring Data pour la gestion des dossiers d'import.
 */
@Repository
public interface DossierImportRepository extends JpaRepository<DossierImport, Long> {

    /** Recherche par numéro de dossier (clé métier). */
    Optional<DossierImport> findByDossierId(String dossierId);

    /** Recherche par identifiant d'instance Camunda. */
    Optional<DossierImport> findByProcessInstanceId(String processInstanceId);

    /** Liste des dossiers par statut. */
    List<DossierImport> findByStatut(StatutDossier statut);

    /** Liste des dossiers d'un importateur. */
    List<DossierImport> findByImportateur(String importateur);
}