package com.example.backendprojet.delegates;



import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("calculFraisExportDelegate")

public class CalculFraisExportDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        // ✅ Parser valeur de façon sécurisée
        Double valeur = 0.0;
        Object valeurObj = execution.getVariable("valeur");
        if (valeurObj instanceof String) {
            valeur = Double.parseDouble((String) valeurObj);
        } else if (valeurObj instanceof Double) {
            valeur = (Double) valeurObj;
        } else if (valeurObj instanceof Long) {
            valeur = ((Long) valeurObj).doubleValue();
        }

        double taxe      = valeur * 0.05;
        double fraisAdmin = 5000.0;
        double total     = taxe + fraisAdmin;

        execution.setVariable("taxeExport", taxe);
        execution.setVariable("fraisAdmin", fraisAdmin);
        execution.setVariable("totalFrais", total);

        System.out.println("✅ Frais calculés : " + total + " DZD");
    }
}