package com.example.backendprojet.entity;
public class DemandeExport {

    private double montant;
    private String pays;
    private boolean exportAutorise;

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public boolean isExportAutorise() {
        return exportAutorise;
    }

    public void setExportAutorise(boolean exportAutorise) {
        this.exportAutorise = exportAutorise;
    }
}