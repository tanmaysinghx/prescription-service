package com.sankatmochan.prescription_service.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.DottedLineSeparator;
import com.sankatmochan.prescription_service.model.Prescription;
import com.sankatmochan.prescription_service.repository.PrescriptionRepository;
import com.sankatmochan.prescription_service.service.PrescriptionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository repository;

    // Exact colors from the Sankat Mochan samples
    private static final Color THEME_TEAL = new Color(0, 128, 128);
    private static final Color LIGHT_GRAY = new Color(248, 248, 248);

    public PrescriptionServiceImpl(PrescriptionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Prescription createPrescription(Prescription p) {
        // Generate unique ID: SNKTMOCH + 8 random digits
        String uniqueId = "SNKTMOCH" + String.format("%08d", new Random().nextInt(100000000));
        p.setId(uniqueId);
        return repository.save(p);
    }

    @Override
    public Prescription getPrescriptionById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prescription " + id + " not found"));
    }

    @Override
    public byte[] generatePrescriptionPdf(String id) {
        Prescription p = getPrescriptionById(id);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Setting margins for the professional spacious look
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        PdfWriter.getInstance(document, out);
        document.open();

        // 1. Header - Program Info (Aligned Right)
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        infoCell.addElement(new Paragraph("SANKAT MOCHAN HEALTH PROGRAM", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, THEME_TEAL)));
        infoCell.addElement(new Paragraph("info@sankatmochan.co.in", FontFactory.getFont(FontFactory.HELVETICA, 8)));
        infoCell.addElement(new Paragraph("3/045 Mahatma Gandhi Marg, Hazratganj, Lucknow", FontFactory.getFont(FontFactory.HELVETICA, 8)));
        infoCell.addElement(new Paragraph("Prescription ID: " + p.getId(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
        header.addCell(infoCell);
        document.add(header);

        // 2. Section Break: Patient Info
        addSectionBreak(document, "Section Break(Continuous)");

        PdfPTable patientInfo = new PdfPTable(3);
        patientInfo.setWidthPercentage(100);
        addLabelValue(patientInfo, "PATIENT NAME", p.getPatientName(), Element.ALIGN_LEFT);
        addLabelValue(patientInfo, "AGE", String.valueOf(p.getAge()), Element.ALIGN_CENTER);
        addLabelValue(patientInfo, "GENDER", p.getGender(), Element.ALIGN_RIGHT);
        document.add(patientInfo);

        document.add(new Paragraph("\n"));
        document.add(new Chunk(new DottedLineSeparator()));

        // 3. Vitals Grid (Shaded Grey Table)
        PdfPTable vitals = new PdfPTable(5);
        vitals.setWidthPercentage(100);
        vitals.setSpacingBefore(10);
        addVitalCell(vitals, "BP (MMHG)", p.getBp());
        addVitalCell(vitals, "PULSE (BPM)", p.getPulse());
        addVitalCell(vitals, "SPO2 (%)", p.getSpo2());
        addVitalCell(vitals, "TEMP (°F)", p.getTemp());
        addVitalCell(vitals, "WEIGHT (KG)", p.getWeight());
        document.add(vitals);

        // 4. Problem Statement & Diagnosis
        document.add(new Paragraph("\nPROBLEM STATEMENT / CLINICAL NOTES", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, THEME_TEAL)));
        document.add(new Paragraph(p.getClinicalNotes(), FontFactory.getFont(FontFactory.HELVETICA, 10)));
        document.add(new Paragraph("\nDiagnosis: " + p.getDiagnosis(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13)));

        // 5. Medication Table
        PdfPTable meds = new PdfPTable(4);
        meds.setWidthPercentage(100);
        meds.setSpacingBefore(15);
        meds.setWidths(new float[]{0.5f, 4, 1.5f, 1.5f});

        // Table Headers with Teal Accent and Light Gray Fill
        String[] headers = {"#", "MEDICINE NAME", "DOSAGE", "DURATION"};
        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8)));
            c.setBackgroundColor(LIGHT_GRAY);
            c.setPadding(5);
            meds.addCell(c);
        }

        int count = 1;
        for (Map<String, String> med : p.getMedicationData()) {
            meds.addCell(createBorderedCell(String.valueOf(count++)));
            meds.addCell(createBorderedCell(med.get("name")));
            meds.addCell(createBorderedCell(med.get("dosage")));
            meds.addCell(createBorderedCell(med.get("duration")));
        }
        document.add(meds);

        // 6. Footer: Signatures and Date
        document.add(new Paragraph("\n"));
        addSectionBreak(document, "Section Break(Continuous)");

        PdfPTable footer = new PdfPTable(2);
        footer.setWidthPercentage(100);
        String date = p.getCreatedAt().format(DateTimeFormatter.ofPattern("MM/dd/yy"));

        addSignatureArea(footer, "Consulting Doctor:", "Name: " + p.getDoctorName() + "\nReg No: " + p.getDoctorRegNo(), date);
        addSignatureArea(footer, "Sankat Mochan Nagrik (SMN):", "Name: \nTylor ID: 5", date);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    // --- Layout and Styling Helper Methods ---

    private void addSectionBreak(Document doc, String text) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Chunk(new DottedLineSeparator()));
        p.add(new Chunk(" " + text + " ", FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY)));
        p.add(new Chunk(new DottedLineSeparator()));
        doc.add(p);
    }

    private void addLabelValue(PdfPTable table, String label, String value, int align) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(align);
        cell.addElement(new Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA, 7, Color.GRAY)));
        cell.addElement(new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA, 11)));
        table.addCell(cell);
    }

    private void addVitalCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(LIGHT_GRAY);
        cell.setBorderColor(Color.LIGHT_GRAY);
        cell.setPadding(5);
        Paragraph l = new Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA, 6, Color.GRAY));
        l.setAlignment(Element.ALIGN_CENTER);
        Paragraph v = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10));
        v.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(l);
        cell.addElement(v);
        table.addCell(cell);
    }

    private PdfPCell createBorderedCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 10)));
        cell.setPadding(5);
        cell.setBorderColor(Color.LIGHT_GRAY);
        return cell;
    }

    private void addSignatureArea(PdfPTable table, String title, String details, String date) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(15);
        cell.addElement(new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA, 9)));
        cell.addElement(new Paragraph(details, FontFactory.getFont(FontFactory.HELVETICA, 8)));
        cell.addElement(new Paragraph("\n__________________________", FontFactory.getFont(FontFactory.HELVETICA, 8)));
        cell.addElement(new Paragraph("SIGNATURE                                  " + date, FontFactory.getFont(FontFactory.HELVETICA, 6, Color.GRAY)));
        table.addCell(cell);
    }

    @Override
    public List<Prescription> getHistoryByPatient(String name) {
        return repository.findByPatientNameIgnoreCase(name);
    }
}