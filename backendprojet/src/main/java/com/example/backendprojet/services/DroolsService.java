package com.example.backendprojet.services;

import com.example.backendprojet.entity.DemandeExport;
import org.kie.api.runtime.KieSession;
import org.kie.api.KieServices;
import org.springframework.stereotype.Service;

@Service
public class DroolsService {

    public void executeRules(DemandeExport demande) {

        KieSession kieSession = KieServices.Factory
                .get()
                .getKieClasspathContainer()
                .newKieSession();

        kieSession.insert(demande);
        kieSession.fireAllRules();
        kieSession.dispose();
    }
}