package com.sankatmochan.prescription_service.service;

import com.sankatmochan.prescription_service.model.Prescription;
import java.util.List;

public interface PrescriptionService {
    Prescription createPrescription(Prescription prescription);
    Prescription getPrescriptionById(String id);
    byte[] generatePrescriptionPdf(String id);
    List<Prescription> getHistoryByPatient(String patientName);
}