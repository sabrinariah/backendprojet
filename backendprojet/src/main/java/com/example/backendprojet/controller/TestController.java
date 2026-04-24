package com.example.backendprojet.controller;

import com.example.backendprojet.entity.DemandeExport;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private RuntimeService runtimeService;

    @PostMapping
    public String startProcess() {

        DemandeExport demande = new DemandeExport();
        demande.setMontant(8000);
        demande.setPays("FRANCE");

        runtimeService.startProcessInstanceByKey(
                "process_export",
                Map.of("demande", demande)
        );

        return "Process started";
    }
}