package com.sankatmochan.prescription_service.service.impl;

import com.sankatmochan.prescription_service.model.Prescription;
import com.sankatmochan.prescription_service.repository.PrescriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceImplTest {

    @Mock
    private PrescriptionRepository repository;

    @InjectMocks
    private PrescriptionServiceImpl prescriptionService;

    private Prescription prescription;

    @BeforeEach
    void setUp() {
        prescription = new Prescription();
        prescription.setId("SNKTMOCH12345678");
        prescription.setPatientName("John Doe");
        prescription.setPatientAddress("123 Main St, Springfield");
        prescription.setPatientPhone("555-1234");
        prescription.setAge(30);
        prescription.setGender("Male");
        prescription.setBp("120/80");
        prescription.setPulse("72");
        prescription.setSpo2("98");
        prescription.setTemp("98.6");
        prescription.setWeight("70");
        prescription.setHeight("175");
        prescription.setBmi("22.9");
        prescription.setClinicalNotes("Fever and cough");
        prescription.setDiagnosis("Viral Fever");
        prescription.setAdvice("Drink plenty of water. Rest.");
        prescription.setNextVisitDate(LocalDateTime.now().plusDays(7));

        List<Map<String, String>> meds = new ArrayList<>();
        Map<String, String> med = new HashMap<>();
        med.put("name", "Paracetamol");
        med.put("dosage", "500mg");
        med.put("duration", "5 days");
        meds.add(med);
        prescription.setMedicationData(meds);

        prescription.setDoctorName("Dr. Smith");
        prescription.setDoctorRegNo("MD12345");
        prescription.setDoctorQualification("MBBS, MD");
        prescription.setDoctorSpecialization("General Physician");

        prescription.setClinicName("Sankat Mochan Health Clinic");
        prescription.setClinicAddress("123 Temple Road, Varanasi");

        prescription.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createPrescription() {
        when(repository.save(any(Prescription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Prescription created = prescriptionService.createPrescription(new Prescription());

        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId().startsWith("SNKTMOCH"));
        verify(repository, times(1)).save(any(Prescription.class));
    }

    @Test
    void getPrescriptionById_Success() {
        when(repository.findById("SNKTMOCH12345678")).thenReturn(Optional.of(prescription));

        Prescription found = prescriptionService.getPrescriptionById("SNKTMOCH12345678");

        assertNotNull(found);
        assertEquals("SNKTMOCH12345678", found.getId());
    }

    @Test
    void getPrescriptionById_NotFound() {
        when(repository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            prescriptionService.getPrescriptionById("INVALID");
        });
    }

    @Test
    void getHistoryByPatient() {
        when(repository.findByPatientNameIgnoreCase("John Doe")).thenReturn(Collections.singletonList(prescription));

        List<Prescription> history = prescriptionService.getHistoryByPatient("John Doe");

        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals("John Doe", history.get(0).getPatientName());
    }

    @Test
    void generatePrescriptionPdf() {
        when(repository.findById("SNKTMOCH12345678")).thenReturn(Optional.of(prescription));

        byte[] pdf = prescriptionService.generatePrescriptionPdf("SNKTMOCH12345678");

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}
