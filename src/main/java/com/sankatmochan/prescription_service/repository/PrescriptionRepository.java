package com.sankatmochan.prescription_service.repository;

import com.sankatmochan.prescription_service.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, String> {

    /**
     * Finds a prescription by the unique SNKTMOCH ID.
     *
     */
    Optional<Prescription> findById(String id);

    /**
     * Finds all prescriptions for a specific patient name.
     * Useful for retrieving history for "Rahul".
     */
    List<Prescription> findByPatientNameIgnoreCase(String patientName);

    /**
     * Finds prescriptions by diagnosis, such as "Pulmonary Tuberculosis".
     *
     */
    List<Prescription> findByDiagnosisContainingIgnoreCase(String diagnosis);
}