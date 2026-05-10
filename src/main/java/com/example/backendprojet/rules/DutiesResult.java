package com.example.backendprojet.rules;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DutiesResult {
    @Builder.Default
    private BigDecimal droitsExport = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal redevances = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal taxesParafiscales = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    public void calculerTotal() {
        total = droitsExport.add(redevances).add(taxesParafiscales);
    }
}
