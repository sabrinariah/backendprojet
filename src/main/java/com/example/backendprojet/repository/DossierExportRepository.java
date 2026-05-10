package com.example.backendprojet.repository;

import com.example.backendprojet.entity.StatutDossier;
import com.example.backendprojet.entity.DossierExport;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DossierExportRepository extends JpaRepository<DossierExport, UUID> {

    Optional<DossierExport> findByReference(String reference);

    Optional<DossierExport> findByProcessInstanceId(String processInstanceId);

    Page<DossierExport> findByStatut(StatutDossier statut, Pageable pageable);

    Page<DossierExport> findByCreePar(String creePar, Pageable pageable);

    boolean existsByReference(String reference);
    @Query("SELECT d FROM DossierExport d " +
            "LEFT JOIN FETCH d.exportateur e " +
            "LEFT JOIN FETCH d.marchandise m " +
            "WHERE d.id = :id")
    Optional<DossierExport> findByIdWithRelations(@Param("id") UUID id);
}

