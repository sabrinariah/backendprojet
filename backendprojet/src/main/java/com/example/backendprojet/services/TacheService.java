package com.example.backendprojet.services;

import com.example.backendprojet.entity.Processus;
import com.example.backendprojet.entity.Tache;
import com.example.backendprojet.repository.ProcessusRepository;
import com.example.backendprojet.repository.TacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TacheService {

    @Autowired
    private TacheRepository tacheRepo;
    @Autowired
    private TacheRepository tacheRepository;
    @Autowired
    private ProcessusRepository processusRepo;

    public Tache addTache(Long processusId, Tache tache) {
        Processus p = processusRepo.findById(processusId).orElseThrow();
        tache.setProcessus(p);
        return tacheRepo.save(tache);
    }

    public List<Tache> getByProcessus(Long processusId) {
        return tacheRepo.findByProcessusId(processusId);
    }
    public void deleteTache(Long id) {
        // Vérifie si la tâche existe
        Optional<Tache> tache = tacheRepository.findById(id);
        if (tache.isPresent()) {
            tacheRepository.deleteById(id);
        } else {
            throw new RuntimeException("Tâche non trouvée avec l'id : " + id);
        }
    }
    public Tache updateTache(Long id, Tache tacheDetails) {
        Tache tache = tacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec l'id : " + id));

        tache.setNom(tacheDetails.getNom());
        tache.setDescription(tacheDetails.getDescription());
        // Ajoute d'autres champs si tu en as (date, statut, etc.)

        return tacheRepository.save(tache);
    }
}