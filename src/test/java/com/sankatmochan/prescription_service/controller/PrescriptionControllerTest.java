package com.sankatmochan.prescription_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sankatmochan.prescription_service.model.Prescription;
import com.sankatmochan.prescription_service.service.PrescriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PrescriptionService prescriptionService;

    @InjectMocks
    private PrescriptionController prescriptionController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Prescription prescription;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(prescriptionController).build();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        prescription = new Prescription();
        prescription.setId("SNKTMOCH12345678");
        prescription.setPatientName("John Doe");
        prescription.setAge(30);
        prescription.setGender("Male");
        prescription.setBp("120/80");
        prescription.setPulse("72");
        prescription.setSpo2("98");
        prescription.setTemp("98.6");
        prescription.setWeight("70");
        prescription.setClinicalNotes("Test Notes");
        prescription.setDiagnosis("Test Diagnosis");
    }

    @Test
    void createPrescription() throws Exception {
        when(prescriptionService.createPrescription(any(Prescription.class))).thenReturn(prescription);

        mockMvc.perform(post("/api/v1/prescriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prescription)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("SNKTMOCH12345678"))
                .andExpect(jsonPath("$.patientName").value("John Doe"));
    }

    @Test
    void downloadPrescription() throws Exception {
        byte[] pdfContent = new byte[] { 1, 2, 3 };
        when(prescriptionService.generatePrescriptionPdf(anyString())).thenReturn(pdfContent);

        mockMvc.perform(get("/api/v1/prescriptions/{id}/download", "SNKTMOCH12345678"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"SNKTMOCH12345678.pdf\""))
                .andExpect(content().bytes(pdfContent));
    }

    @Test
    void getPatientHistory() throws Exception {
        when(prescriptionService.getHistoryByPatient(anyString())).thenReturn(Collections.singletonList(prescription));

        mockMvc.perform(get("/api/v1/prescriptions/patient/{name}", "John Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("SNKTMOCH12345678"));
    }
}
