package com.sankatmochan.prescription_service.controller;

import com.sankatmochan.prescription_service.model.Prescription;
import com.sankatmochan.prescription_service.service.PrescriptionService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prescriptions")
@CrossOrigin(origins = "*")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    /**
     * Endpoint to create a prescription and generate a unique SNKTMOCH ID.
     */
    @PostMapping
    public ResponseEntity<Prescription> createPrescription(@RequestBody Prescription prescription) {
        Prescription savedPrescription = prescriptionService.createPrescription(prescription);
        return new ResponseEntity<>(savedPrescription, HttpStatus.CREATED);
    }

    /**
     * Endpoint to download the styled PDF using the unique ID.
     */
    @GetMapping(value = "/{id}/download", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPrescription(@PathVariable String id) {
        byte[] pdfContent = prescriptionService.generatePrescriptionPdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        // This makes the browser download the file as 'SNKTMOCHXXXXXXXX.pdf'
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(id + ".pdf")
                .build());

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }

    /**
     * Endpoint to search history by patient name (e.g., /patient/Rahul).
     */
    @GetMapping("/patient/{name}")
    public ResponseEntity<List<Prescription>> getPatientHistory(@PathVariable String name) {
        return ResponseEntity.ok(prescriptionService.getHistoryByPatient(name));
    }
}