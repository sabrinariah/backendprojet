package com.example.backendprojet.services;



import com.example.backendprojet.entity.Processus;
import com.example.backendprojet.entity.Tache;
import com.example.backendprojet.repository.ProcessusRepository;
import com.example.backendprojet.repository.TacheRepository;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.camunda.bpm.engine.TaskService;
import java.util.List;

@Service
public class ProcessusService {

    private final ProcessusRepository processusRepository;
    private final TacheRepository tacheRepository;
    @Autowired
    private TaskService taskService;

    public ProcessusService(ProcessusRepository processusRepository, TacheRepository tacheRepository) {
        this.processusRepository = processusRepository;
        this.tacheRepository = tacheRepository;
    }

    public List<Processus> getAllProcessus() {
        return processusRepository.findAll();
    }

    public Processus getProcessusById(Long id) {
        return processusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processus introuvable"));
    }

    public Processus createProcessus(Processus processus) {
        return processusRepository.save(processus);
    }

    public Processus updateProcessus(Long id, Processus data) {
        Processus p = getProcessusById(id);
        p.setNom(data.getNom());
        p.setTypeProcessus(data.getTypeProcessus());
        p.setDateDebut(data.getDateDebut());
        p.setDateFin(data.getDateFin());
        p.setActif(data.getActif());
        return processusRepository.save(p);
    }

    public Processus toggleProcessus(Long id) {
        Processus p = getProcessusById(id);
        p.setActif(!p.getActif());
        return processusRepository.save(p);
    }

    public Tache addTacheToProcessus(Long processusId, Tache tache) {
        Processus p = getProcessusById(processusId);
        tache.setProcessus(p);
        return tacheRepository.save(tache);
    }

    public List<Tache> getTachesByProcessus(Long processusId) {
        return tacheRepository.findByProcessusId(processusId);
    }

    public Tache updateTache(Long id, Tache data) {
        Tache t = tacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche introuvable"));

        t.setNom(data.getNom());
        t.setDescription(data.getDescription());
        t.setAssignee(data.getAssignee());
        t.setOrdre(data.getOrdre());

        return tacheRepository.save(t);
    }
    public void deleteProcessus(Long id) {
        Processus p = getProcessusById(id);
        processusRepository.delete(p);
    }

    // Supprimer une tâche
    public void deleteTache(Long id) {
        Tache t = tacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche introuvable"));
        tacheRepository.delete(t);
    }
    public Tache updateTache(Long processusId, Long tacheId, Tache updatedTache) {

        Processus processus = processusRepository.findById(processusId)
                .orElseThrow(() -> new RuntimeException("Processus not found"));

        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tache not found"));

        tache.setNom(updatedTache.getNom());
        tache.setDescription(updatedTache.getDescription());

        return tacheRepository.save(tache);
    }

    public void verifierTache(String processInstanceId) {

        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .active()
                .singleResult();

        if (task == null) {
            System.out.println("⚠️ Process terminé ou aucune tâche active");
        } else {
            System.out.println("✅ Tâche trouvée : " + task.getName());
            System.out.println("🆔 Task ID : " + task.getId());
        }
    }

    public void completeTask(String taskId) {

        taskService.complete(taskId);

        System.out.println("✅ Tâche complétée : " + taskId);
    }

}