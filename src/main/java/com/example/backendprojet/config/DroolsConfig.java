package com.example.backendprojet.config;


import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;

@Configuration
public class DroolsConfig {

    private static final String RULES_PATH = "classpath*:rules/*.drl";

    @Bean
    public KieContainer kieContainer() throws IOException {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources(RULES_PATH);

        for (Resource resource : resources) {
            kieFileSystem.write(
                    "src/main/resources/rules/" + resource.getFilename(),
                    kieServices.getResources().newInputStreamResource(resource.getInputStream())
            );
        }

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();

        Results results = kieBuilder.getResults();
        if (results.hasMessages(Message.Level.ERROR)) {
            throw new IllegalStateException(
                    "Erreurs de compilation Drools : " + results.getMessages());
        }

        return kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
    }
}