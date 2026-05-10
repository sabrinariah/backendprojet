package com.example.backendprojet.services;



import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfService {

    private final HistoryService historyService;

    public PdfService(HistoryService historyService) {
        this.historyService = historyService;
    }

    public byte[] generateProcessPdf(String processInstanceId) {

        try {

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document document = new Document();

            PdfWriter.getInstance(document, out);

            document.open();

            Font titleFont = new Font(
                    Font.HELVETICA,
                    18,
                    Font.BOLD
            );

            Paragraph title = new Paragraph(
                    "Rapport du Processus Export",
                    titleFont
            );

            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);

            document.add(new Paragraph(" "));

            document.add(new Paragraph(
                    "Process Instance ID : " + processInstanceId
            ));

            document.add(new Paragraph(" "));

            List<HistoricVariableInstance> variables =
                    historyService
                            .createHistoricVariableInstanceQuery()
                            .processInstanceId(processInstanceId)
                            .list();

            for (HistoricVariableInstance variable : variables) {

                document.add(
                        new Paragraph(
                                variable.getName()
                                        + " : "
                                        + variable.getValue()
                        )
                );
            }

            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}