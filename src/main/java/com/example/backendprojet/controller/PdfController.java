package com.example.backendprojet.controller;



import com.example.backendprojet.services.PdfService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/process")
@CrossOrigin("*")
public class PdfController {

    private final PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping("/pdf/{processInstanceId}")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable String processInstanceId
    ) {

        byte[] pdf =
                pdfService.generateProcessPdf(processInstanceId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=rapport-processus.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}