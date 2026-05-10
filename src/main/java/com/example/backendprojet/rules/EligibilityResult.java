package com.example.backendprojet.rules;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class EligibilityResult {
    private Boolean eligible = null;;
    private String reason;
    private String code;
}
