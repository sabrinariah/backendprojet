package com.example.backendprojet.services;



import com.example.backendprojet.entity.DossierImport;
import com.example.backendprojet.entity.StatutDossier;
import com.example.backendprojet.repository.DossierImportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service métier principal du processus d'import.
 * Pilote la création des dossiers, le démarrage du processus Camunda,
 * et la complétion des tâches utilisateur.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DossierImportService {

    /** Clé du processus BPMN (correspond à l'attribut id du <bpmn:process>). */
    private static final String PROCESS_KEY = "process_import_corrected";

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final DossierImportRepository repository;

    // ===================================================
    //   CREATION ET DEMARRAGE DU PROCESSUS
    // ===================================================

    /**
     * Crée un nouveau dossier d'import et démarre une instance du processus BPMN.
     */
    @Transactional
    public DossierImport demarrerProcessus(DossierImport dossier) {
        log.info("Démarrage du processus pour le dossier : {}", dossier.getDossierId());

        // 1. Sauvegarde initiale du dossier
        dossier.setStatut(StatutDossier.EN_COURS);
        DossierImport saved = repository.save(dossier);

        // 2. Préparation des variables initiales du processus
        Map<String, Object> variables = new HashMap<>();
        variables.put("dossierId", saved.getDossierId());
        variables.put("nomDossier", saved.getNomDossier());
        variables.put("importateur", saved.getImportateur());
        variables.put("paysOrigine", saved.getPaysOrigine());
        variables.put("typeProduit", saved.getTypeProduit());
        variables.put("dateDepot", saved.getDateDepot());

        // 3. Démarrage de l'instance Camunda
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(
                PROCESS_KEY,
                saved.getDossierId(),
                variables
        );

        // 4. Sauvegarde du lien processInstanceId ↔ dossier
        saved.setProcessInstanceId(pi.getId());
        DossierImport result = repository.save(saved);

        log.info("Processus démarré - dossier: {} | processInstanceId: {}",
                saved.getDossierId(), pi.getId());

        return result;
    }

    // ===================================================
    //   GESTION DES TACHES UTILISATEUR
    // ===================================================

    /**
     * Complète une tâche utilisateur avec les variables du formulaire.
     * Met à jour le dossier en BDD avec les nouvelles données.
     */
    @Transactional
    public void completerTache(String taskId, Map<String, Object> variables) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new IllegalArgumentException("Tâche introuvable : " + taskId);
        }

        log.info("Complétion tâche : {} ({}) - variables: {}",
                task.getName(), taskId, variables.keySet());

        // Synchronisation : mise à jour de l'entité métier
        String processInstanceId = task.getProcessInstanceId();
        repository.findByProcessInstanceId(processInstanceId).ifPresent(dossier -> {
            mettreAJourDossier(dossier, variables);
            repository.save(dossier);
        });

        // Complétion dans Camunda (déclenche les serviceTasks suivantes)
        taskService.complete(taskId, variables);
    }

    /**
     * Liste des tâches accessibles à un utilisateur (par groupes ou assignation).
     */
    public List<Task> listerMesTaches(List<String> groupes, String userId) {
        var query = taskService.createTaskQuery();

        if (userId != null && groupes != null && !groupes.isEmpty()) {
            query.or()
                    .taskAssignee(userId)
                    .taskCandidateGroupIn(groupes)
                    .endOr();
        } else if (groupes != null && !groupes.isEmpty()) {
            query.taskCandidateGroupIn(groupes);
        } else if (userId != null) {
            query.taskAssignee(userId);
        }

        return query.orderByTaskCreateTime().desc().list();
    }

    /**
     * Récupère toutes les variables courantes d'une instance de processus.
     */
    public Map<String, Object> getVariables(String processInstanceId) {
        return runtimeService.getVariables(processInstanceId);
    }

    /**
     * Permet à un utilisateur de s'assigner une tâche (lock).
     */
    @Transactional
    public void claimerTache(String taskId, String userId) {
        log.info("Claim tâche {} par {}", taskId, userId);
        taskService.claim(taskId, userId);
    }

    /**
     * Libère une tâche déjà claimée.
     */
    @Transactional
    public void unclaimerTache(String taskId) {
        log.info("Unclaim tâche {}", taskId);
        taskService.setAssignee(taskId, null);
    }

    // ===================================================
    //   SYNCHRONISATION DES VARIABLES → ENTITE
    // ===================================================

    /**
     * Met à jour les champs du DossierImport selon les variables soumises
     * par les formulaires des userTasks.
     */
    private void mettreAJourDossier(DossierImport d, Map<String, Object> v) {
        // Phase 1 - Saisie & Procédures préalables
        setIfPresent(v, "licenceImport", String.class, d::setLicenceImport);
        setIfPresent(v, "autoriteCompetente", String.class, d::setAutoriteCompetente);
        setIfPresent(v, "referenceAutorisation", String.class, d::setReferenceAutorisation);
        setIfPresent(v, "observationsPrealables", String.class, d::setObservationsPrealables);

        // DI
        setIfPresent(v, "numeroDI", String.class, d::setNumeroDI);
        setIfPresent(v, "codeSH", String.class, d::setCodeSH);
        if (v.containsKey("quantite")) d.setQuantite(toLong(v.get("quantite")));
        if (v.containsKey("valeurCAF")) d.setValeurCAF(toLong(v.get("valeurCAF")));
        setIfPresent(v, "deviseFacture", String.class, d::setDeviseFacture);
        setIfPresent(v, "origineMarchandise", String.class, d::setOrigineMarchandise);

        // Domiciliation
        setIfPresent(v, "banqueDomiciliataire", String.class, d::setBanqueDomiciliataire);
        setIfPresent(v, "numeroCompte", String.class, d::setNumeroCompte);
        setIfPresent(v, "referenceAssurance", String.class, d::setReferenceAssurance);
        if (v.containsKey("montantAssure")) d.setMontantAssure(toLong(v.get("montantAssure")));
        setIfPresent(v, "devise", String.class, d::setDevise);

        // Visas
        setIfPresent(v, "visaTechnique", Boolean.class, d::setVisaTechnique);
        setIfPresent(v, "certificatConformite", Boolean.class, d::setCertificatConformite);
        setIfPresent(v, "certificatOrigine", Boolean.class, d::setCertificatOrigine);
        setIfPresent(v, "autresDocuments", String.class, d::setAutresDocuments);

        // Phase 2 - Manifeste & déchargement
        setIfPresent(v, "numeroManifeste", String.class, d::setNumeroManifeste);
        setIfPresent(v, "navire", String.class, d::setNavire);
        setIfPresent(v, "portArrivee", String.class, d::setPortArrivee);
        setIfPresent(v, "numeroConnaissement", String.class, d::setNumeroConnaissement);
        setIfPresent(v, "numeroEntrepot", String.class, d::setNumeroEntrepot);
        setIfPresent(v, "emplacement", String.class, d::setEmplacement);
        setIfPresent(v, "numeroConteneur", String.class, d::setNumeroConteneur);

        // Reconnaissance & anomalie
        setIfPresent(v, "agentReconnaissance", String.class, d::setAgentReconnaissance);
        if (v.containsKey("poidsConstate")) d.setPoidsConstate(toLong(v.get("poidsConstate")));
        setIfPresent(v, "anomaliesDetectees", Boolean.class, d::setAnomaliesDetectees);
        setIfPresent(v, "observationsRecon", String.class, d::setObservationsRecon);
        setIfPresent(v, "marchandiseConforme", Boolean.class, d::setMarchandiseConforme);
        setIfPresent(v, "typeAnomalie", String.class, d::setTypeAnomalie);
        setIfPresent(v, "actionCorrective", String.class, d::setActionCorrective);
        setIfPresent(v, "anomalieResolue", Boolean.class, d::setAnomalieResolue);

        // Phase 3 - DAU & contrôles
        setIfPresent(v, "numeroDeclaration", String.class, d::setNumeroDeclaration);
        setIfPresent(v, "bureauDouane", String.class, d::setBureauDouane);
        setIfPresent(v, "declarant", String.class, d::setDeclarant);

        setIfPresent(v, "agentDouaneDoc", String.class, d::setAgentDouaneDoc);
        setIfPresent(v, "observationsControle", String.class, d::setObservationsControle);
        setIfPresent(v, "agentDouanePhys", String.class, d::setAgentDouanePhys);
        setIfPresent(v, "resultScanner", String.class, d::setResultScanner);
        setIfPresent(v, "anomaliesInspection", Boolean.class, d::setAnomaliesInspection);
        setIfPresent(v, "rapportInspection", String.class, d::setRapportInspection);
        setIfPresent(v, "controleConforme", Boolean.class, d::setControleConforme);

        // Paiement
        setIfPresent(v, "referencePaiement", String.class, d::setReferencePaiement);
        setIfPresent(v, "numeroQuittance", String.class, d::setNumeroQuittance);
        if (v.containsKey("montantPaiement")) d.setMontantPaiement(toLong(v.get("montantPaiement")));
        setIfPresent(v, "methodePaiement", String.class, d::setMethodePaiement);
        setIfPresent(v, "banquePaiement", String.class, d::setBanquePaiement);

        // Phase 4
        setIfPresent(v, "numeroBAE", String.class, d::setNumeroBAE);
        setIfPresent(v, "agentDouaneBAE", String.class, d::setAgentDouaneBAE);
        setIfPresent(v, "observationsBAE", String.class, d::setObservationsBAE);
        setIfPresent(v, "numeroBAD", String.class, d::setNumeroBAD);
        setIfPresent(v, "armateur", String.class, d::setArmateur);
        setIfPresent(v, "quaiLivraison", String.class, d::setQuaiLivraison);
        setIfPresent(v, "numeroConstatSortie", String.class, d::setNumeroConstatSortie);
        setIfPresent(v, "agentControleSortie", String.class, d::setAgentControleSortie);
        setIfPresent(v, "conformiteSortie", Boolean.class, d::setConformiteSortie);
        setIfPresent(v, "observationsSortie", String.class, d::setObservationsSortie);
    }

    @SuppressWarnings("unchecked")
    private <T> void setIfPresent(Map<String, Object> map, String key,
                                  Class<T> type, java.util.function.Consumer<T> setter) {
        if (map.containsKey(key) && map.get(key) != null) {
            Object val = map.get(key);
            if (type.isInstance(val)) {
                setter.accept((T) val);
            }
        }
    }

    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Long l) return l;
        if (o instanceof Integer i) return i.longValue();
        if (o instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(o.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}