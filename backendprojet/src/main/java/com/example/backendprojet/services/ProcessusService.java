package com.example.backendprojet.services;

import com.example.backendprojet.entity.Processus;
import com.example.backendprojet.entity.Tache;
import com.example.backendprojet.repository.ProcessusRepository;
import com.example.backendprojet.repository.TacheRepository;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcessusService {

    private final ProcessusRepository processusRepository;
    private final TacheRepository tacheRepository;
    private final TaskService taskService;

    public ProcessusService(ProcessusRepository processusRepository,
                            TacheRepository tacheRepository,
                            TaskService taskService) {
        this.processusRepository = processusRepository;
        this.tacheRepository = tacheRepository;
        this.taskService = taskService;
    }

    // =========================
    // PROCESSUS
    // =========================

    public List<Processus> getAllProcessus() {
        return processusRepository.findAll();
    }

    public Processus getProcessusById(Long id) {
        return processusRepository.findById(id)
                .orElse(null); // ✅ important pour éviter 500
    }

    public Processus createProcessus(Processus processus) {
        return processusRepository.save(processus);
    }

    public Processus updateProcessus(Long id, Processus data) {
        Processus p = getProcessusById(id);

        if (p == null) return null;

        p.setNom(data.getNom());
        p.setTypeProcessus(data.getTypeProcessus());
        p.setDateDebut(data.getDateDebut());
        p.setDateFin(data.getDateFin());
        p.setActif(data.getActif());

        return processusRepository.save(p);
    }

    public Processus toggleProcessus(Long id) {
        Processus p = getProcessusById(id);

        if (p == null) return null;

        p.setActif(!p.getActif());
        return processusRepository.save(p);
    }

    public void deleteProcessus(Long id) {
        Processus p = getProcessusById(id);
        if (p != null) {
            processusRepository.delete(p);
        }
    }

    // =========================
    // TÂCHES (DB)
    // =========================

    public Tache addTacheToProcessus(Long processusId, Tache tache) {
        Processus p = getProcessusById(processusId);
        if (p == null) return null;

        tache.setProcessus(p);
        return tacheRepository.save(tache);
    }

    public List<Tache> getTachesByProcessus(Long processusId) {
        return tacheRepository.findByProcessusId(processusId);
    }

    public Tache updateTache(Long processusId, Long tacheId, Tache updatedTache) {

        Processus processus = processusRepository.findById(processusId)
                .orElse(null);

        Tache tache = tacheRepository.findById(tacheId)
                .orElse(null);

        if (processus == null || tache == null) return null;

        tache.setNom(updatedTache.getNom());
        tache.setDescription(updatedTache.getDescription());
        tache.setAssignee(updatedTache.getAssignee());
        tache.setOrdre(updatedTache.getOrdre());

        return tacheRepository.save(tache);
    }

    public void deleteTache(Long id) {
        Tache t = tacheRepository.findById(id).orElse(null);
        if (t != null) {
            tacheRepository.delete(t);
        }
    }

    // =========================
    // CAMUNDA
    // =========================

    public void verifierTache(String processInstanceId) {

        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .active()
                .singleResult();

        if (task == null) {
            System.out.println("⚠️ Aucune tâche active");
        } else {
            System.out.println("✅ Task : " + task.getName());
        }
    }

    public void completeTask(String taskId) {
        taskService.complete(taskId);
        System.out.println("✅ Task complétée : " + taskId);
    }
}