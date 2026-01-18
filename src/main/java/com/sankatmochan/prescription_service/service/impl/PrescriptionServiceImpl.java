package com.sankatmochan.prescription_service.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
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
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, out);
        document.open();

        // --- 1. Header ---
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        try {
            headerTable.setWidths(new float[] { 1.8f, 1f });
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        // Left Side: Clinic Name (Large)
        PdfPCell leftHeader = new PdfPCell();
        leftHeader.setBorder(Rectangle.NO_BORDER);
        leftHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);

        String clinicName = p.getClinicName() != null ? p.getClinicName().toUpperCase()
                : "SANKAT MOCHAN HEALTH PROGRAM";
        Paragraph title = new Paragraph(clinicName, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, THEME_TEAL)); // Size
                                                                                                                      // 20
        leftHeader.addElement(title);
        headerTable.addCell(leftHeader);

        // Right Side: Contact & Address
        PdfPCell rightHeader = new PdfPCell();
        rightHeader.setBorder(Rectangle.NO_BORDER);
        rightHeader.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rightHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);

        String clinicAddress = p.getClinicAddress() != null ? p.getClinicAddress()
                : "3/045 Mahatma Gandhi Marg, Hazratganj, Lucknow";
        Paragraph infoP = new Paragraph();
        infoP.setAlignment(Element.ALIGN_RIGHT);
        infoP.add(new Chunk("info@sankatmochan.co.in\n", FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY)));
        infoP.add(new Chunk(clinicAddress + "\n", FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY)));
        infoP.add(new Chunk("Prescription ID: " + p.getId(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK)));

        rightHeader.addElement(infoP);

        headerTable.addCell(rightHeader);
        document.add(headerTable);

        // Solid Teal Separator Line
        addTealSeparator(document);

        // --- 2. Patient Info ---
        PdfPTable patientInfo = new PdfPTable(4);
        patientInfo.setWidthPercentage(100);
        patientInfo.setSpacingBefore(10);
        patientInfo.setSpacingAfter(10);
        try {
            patientInfo.setWidths(new float[] { 1f, 2f, 0.8f, 2f });
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        addPatientLabel(patientInfo, "PATIENT NAME");
        addPatientValue(patientInfo, p.getPatientName());

        addPatientLabel(patientInfo, "DATE");
        addPatientValue(patientInfo, p.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));

        addPatientLabel(patientInfo, "AGE / GENDER");
        addPatientValue(patientInfo, p.getAge() + " Y / " + p.getGender());

        addPatientLabel(patientInfo, "PHONE");
        addPatientValue(patientInfo, p.getPatientPhone() != null ? p.getPatientPhone() : "-");

        addPatientLabel(patientInfo, "ADDRESS");
        PdfPCell addrCell = new PdfPCell(new Phrase(p.getPatientAddress() != null ? p.getPatientAddress() : "-",
                FontFactory.getFont(FontFactory.HELVETICA, 11)));
        addrCell.setBorder(Rectangle.NO_BORDER);
        addrCell.setColspan(3);
        patientInfo.addCell(addrCell);

        document.add(patientInfo);

        // Solid Teal Separator Line
        addTealSeparator(document);

        // --- 3. Vitals Grid (Split into 2 rows for spacing) ---
        // Row 1: BP, Pulse, SPO2, Temp
        PdfPTable vitalsRow1 = new PdfPTable(4);
        vitalsRow1.setWidthPercentage(100);
        vitalsRow1.setSpacingBefore(10);
        addVitalCell(vitalsRow1, "BP", p.getBp());
        addVitalCell(vitalsRow1, "PULSE", p.getPulse());
        addVitalCell(vitalsRow1, "SPO2", p.getSpo2());
        addVitalCell(vitalsRow1, "TEMP", p.getTemp());
        document.add(vitalsRow1);

        // Row 2: Weight, Height, BMI (and empty filler)
        PdfPTable vitalsRow2 = new PdfPTable(4);
        vitalsRow2.setWidthPercentage(100);
        vitalsRow2.setSpacingBefore(5);
        vitalsRow2.setSpacingAfter(15);
        addVitalCell(vitalsRow2, "WEIGHT", p.getWeight());
        addVitalCell(vitalsRow2, "HEIGHT", p.getHeight());
        addVitalCell(vitalsRow2, "BMI", p.getBmi());
        addVitalCell(vitalsRow2, "", ""); // Spacer
        document.add(vitalsRow2);

        // --- 4. Clinical Notes ---
        PdfPTable diagnosisTable = new PdfPTable(1);
        diagnosisTable.setWidthPercentage(100);
        diagnosisTable.setSpacingAfter(15);

        PdfPCell diagCell = new PdfPCell();
        diagCell.setBorder(Rectangle.LEFT | Rectangle.BOTTOM); // Minimalist L-bracket border look
        diagCell.setBorderColor(THEME_TEAL);
        diagCell.setBorderWidth(3f); // Thicker border
        diagCell.setPadding(12); // More padding
        diagCell.setBackgroundColor(new Color(248, 255, 255));

        diagCell.addElement(
                new Paragraph("PROBLEM / DIAGNOSIS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, THEME_TEAL)));
        diagCell.addElement(new Paragraph(p.getClinicalNotes(), FontFactory.getFont(FontFactory.HELVETICA, 11)));
        diagCell.addElement(
                new Paragraph("\nDIAGNOSIS: " + p.getDiagnosis(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13)));

        diagnosisTable.addCell(diagCell);
        document.add(diagnosisTable);

        // --- 5. Medication Table ---
        PdfPTable meds = new PdfPTable(4);
        meds.setWidthPercentage(100);
        meds.setSpacingBefore(10);
        try {
            meds.setWidths(new float[] { 0.6f, 4, 1.5f, 1.5f });
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        meds.setHeaderRows(1);

        // Headers
        String[] headers = { "#", "MEDICINE NAME", "DOSAGE", "DURATION" };
        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
            c.setBackgroundColor(THEME_TEAL);
            c.setPadding(8); // Increased padding
            c.setVerticalAlignment(Element.ALIGN_MIDDLE);
            c.setBorderColor(THEME_TEAL);
            meds.addCell(c);
        }

        // Rows with Zebra Striping
        int count = 1;
        if (p.getMedicationData() != null) {
            for (Map<String, String> med : p.getMedicationData()) {
                Color rowColor = (count % 2 == 0) ? LIGHT_GRAY : Color.WHITE;

                meds.addCell(createStripedCell(String.valueOf(count++), rowColor));
                meds.addCell(createStripedCell(med.get("name"), rowColor));
                meds.addCell(createStripedCell(med.get("dosage"), rowColor));
                meds.addCell(createStripedCell(med.get("duration"), rowColor));
            }
        }
        document.add(meds);

        // --- 6. Advice ---
        if (p.getAdvice() != null && !p.getAdvice().isEmpty()) {
            document.add(new Paragraph("\n"));
            PdfPTable adviceTable = new PdfPTable(1);
            adviceTable.setWidthPercentage(100);

            PdfPCell adviceCell = new PdfPCell();
            adviceCell.setBorder(Rectangle.NO_BORDER);
            adviceCell.setPadding(8);

            adviceCell.addElement(new Paragraph("ADVICE / INSTRUCTIONS",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, THEME_TEAL)));
            adviceCell.addElement(new Paragraph(p.getAdvice(), FontFactory.getFont(FontFactory.HELVETICA, 11)));

            adviceTable.addCell(adviceCell);
            document.add(adviceTable);
        }

        // --- 7. Footer ---
        document.add(new Paragraph("\n\n")); // Spacing
        addTealSeparator(document);

        PdfPTable footer = new PdfPTable(2);
        footer.setWidthPercentage(100);

        String doctorDetails = p.getDoctorName().toUpperCase() + "\n" + p.getDoctorRegNo();
        if (p.getDoctorQualification() != null)
            doctorDetails += "\n" + p.getDoctorQualification();
        if (p.getDoctorSpecialization() != null)
            doctorDetails += "\n" + p.getDoctorSpecialization();

        addFooterSignature(footer, "CONSULTING DOCTOR", doctorDetails, Element.ALIGN_LEFT);

        String nextVisit = p.getNextVisitDate() != null
                ? "Next Visit: " + p.getNextVisitDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                : "";
        addFooterSignature(footer, "SANKAT MOCHAN NAGRIK", "Generated by SMN Platform\n" + nextVisit,
                Element.ALIGN_RIGHT);

        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    // --- Layout and Styling Helper Methods ---

    private void addTealSeparator(Document doc) throws DocumentException {
        LineSeparator ls = new LineSeparator();
        ls.setLineColor(THEME_TEAL);
        ls.setLineWidth(2f); // Thicker line
        ls.setPercentage(100);
        ls.setOffset(-2);
        doc.add(new Chunk(ls));
    }

    private void addPatientLabel(PdfPTable table, String label) {
        PdfPCell cell = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingBottom(5);
        table.addCell(cell);
    }

    private void addPatientValue(PdfPTable table, String value) {
        PdfPCell cell = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11))); // Larger
                                                                                                              // Value
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingBottom(5);
        table.addCell(cell);
    }

    private void addVitalCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(Color.WHITE);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(8); // More padding

        Paragraph l = new Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA, 7, Color.GRAY));
        l.setAlignment(Element.ALIGN_CENTER);

        Paragraph v = new Paragraph(value != null && !value.isEmpty() ? value : "-",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11));
        v.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(l);
        cell.addElement(v);
        table.addCell(cell);
    }

    private PdfPCell createStripedCell(String text, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 11))); // Larger text
        cell.setPadding(8); // More padding
        cell.setBackgroundColor(bgColor);
        cell.setBorderColor(new Color(230, 230, 230));
        return cell;
    }

    private void addFooterSignature(PdfPTable table, String title, String details, int align) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(align);
        cell.setPaddingTop(15);

        Paragraph pTitle = new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.GRAY));
        pTitle.setAlignment(align);
        cell.addElement(pTitle);

        Paragraph pDetails = new Paragraph(details, FontFactory.getFont(FontFactory.HELVETICA, 11));
        pDetails.setAlignment(align);
        cell.addElement(pDetails);

        table.addCell(cell);
    }

    @Override
    public List<Prescription> getHistoryByPatient(String name) {
        return repository.findByPatientNameIgnoreCase(name);
    }
}