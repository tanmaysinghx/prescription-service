package com.sankatmochan.prescription_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "prescriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Prescription {

    @Id
    @Column(length = 50)
    private String id;

    private String patientName;
    private String patientAddress;
    private String patientPhone;
    private Integer age;
    private String gender;

    private String bp;
    private String pulse;
    private String spo2;
    private String temp;
    private String weight;
    private String height;
    private String bmi;

    @Column(columnDefinition = "TEXT")
    private String clinicalNotes;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "medication_data", columnDefinition = "json")
    private List<Map<String, String>> medicationData;

    // Hard-coding the default at the JPA/Hibernate level
    @Column(name = "approved_by_doctor", nullable = false)
    @ColumnDefault("0")
    private Boolean approvedByDoctor = false;

    @Column(name = "is_ai_generated", nullable = false)
    @ColumnDefault("1")
    private Boolean isAiGenerated = true;

    private String doctorName;
    private String doctorRegNo;
    private String doctorQualification;
    private String doctorSpecialization;

    private String clinicName;
    private String clinicAddress;

    @Column(name = "next_visit_date")
    private LocalDateTime nextVisitDate;

    @Column(columnDefinition = "TEXT")
    private String advice;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Safety Pre-Persist hook to ensure values are never null during save
    @PrePersist
    protected void onCreate() {
        if (approvedByDoctor == null) {
            approvedByDoctor = false;
        }
        if (isAiGenerated == null) {
            isAiGenerated = true;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}