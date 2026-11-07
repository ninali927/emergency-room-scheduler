package io.github.ninali927.er.models;

import java.util.Objects;

/**
 * Represents a patient in the Emergency Room system.
 */
public class Patient implements Comparable<Patient> {

    private final int patientId;
    private final String name;
    private int severity; // 1-5 scale, where 5 is most severe
    private final String chiefComplaint;

    /**
     * Constructs a new Patient.
     *
     * @param patientId unique identifier for this patient
     * @param name patient's name
     * @param severity initial severity level (1-5, where 5 is most severe)
     * @param chiefComplaint brief description of patient's complaint
     * @throws IllegalArgumentException if severity is not in range [1, 5]
     */
    public Patient(int patientId, String name, int severity, String chiefComplaint) {
        if (severity < 1 || severity > 5) {
            throw new IllegalArgumentException("Severity must be between 1 and 5");
        }
        this.patientId = patientId;
        this.name = name;
        this.severity = severity;
        this.chiefComplaint = chiefComplaint;
    }

    /**
     * Gets the unique patient identifier.
     *
     * @return patient ID
     */
    public int getPatientId() {
        return patientId;
    }

    /**
     * Gets the patient's name.
     *
     * @return patient name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the current severity level.
     *
     * @return severity (1-5, where 5 is most severe)
     */
    public int getSeverity() {
        return severity;
    }

    /**
     * Updates the patient's severity level.
     *
     * @param newSeverity new severity level (1-5)
     * @throws IllegalArgumentException if severity is not in range [1, 5]
     */
    public void setSeverity(int newSeverity) {
        if (newSeverity < 1 || newSeverity > 5) {
            throw new IllegalArgumentException("Severity must be between 1 and 5");
        }
        this.severity = newSeverity;
    }

    /**
     * Gets the patient's chief complaint.
     *
     * @return chief complaint description
     */
    public String getChiefComplaint() {
        return chiefComplaint;
    }

    /**
     * Compares patients by patient ID for natural ordering.
     *
     * @param other the patient to compare to
     * @return negative if this ID < other ID, positive if this ID > other ID, 0 if equal
     */
    @Override
    public int compareTo(Patient other) {
        return Integer.compare(this.patientId, other.patientId);
    }

    /**
     * Checks equality based on patient ID.
     * Two patients are equal if they have the same ID.
     *
     * @param obj object to compare to
     * @return true if same patient ID, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Patient)) {
            return false;
        }
        Patient other = (Patient) obj;
        return this.patientId == other.patientId;
    }

    /**
     * Hash code based on patient ID.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(patientId);
    }

    /**
     * String representation of the patient.
     *
     * @return formatted patient information
     */
    @Override
    public String toString() {
        return String.format("Patient{id=%d, name='%s', severity=%d, complaint='%s'}",
                patientId, name, severity, chiefComplaint);
    }
}
