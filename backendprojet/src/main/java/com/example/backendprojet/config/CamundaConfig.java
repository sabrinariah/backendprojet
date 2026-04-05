package com.example.backendprojet.config;



import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaConfig extends AbstractCamundaConfiguration {

    @Override
    public void preInit(SpringProcessEngineConfiguration configuration) {
        // ✅ Désactive l'obligation d'avoir historyTimeToLive dans le BPMN
        configuration.setHistoryTimeToLive("180");
    }
}