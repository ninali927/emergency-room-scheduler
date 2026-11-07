package io.github.ninali927.er.system;

import io.github.ninali927.er.models.Patient;
import io.github.ninali927.er.registry.PatientRegistry;
import io.github.ninali927.er.registry.AVLPatientRegistry;
import io.github.ninali927.er.queue.TreatmentQueue;
import io.github.ninali927.er.queue.HeapTreatmentQueue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main Emergency Room system that coordinates patient registration and treatment.
 *
 * This class uses two data structures to manage patients:
 * 1. PatientRegistry (AVL tree) - maps arrival times to patients for administrative queries
 * 2. TreatmentQueue (priority queue) - orders patients by severity for treatment prioritization
 *
 * Patients are maintained in both structures simultaneously. When a patient is registered,
 * they are added to both. When they are treated (removed), they are removed from both.
 *
 * Additionally, the system maintains a mapping from patient IDs to arrival times to enable
 * removal from the registry when a patient is treated.
 */
public class EmergencyRoom {

    private final PatientRegistry registry;
    private final TreatmentQueue treatmentQueue;
    private final Map<Integer, Integer> patientIdToArrivalTime; // Track arrival times for removal
    private int patientCounter;

    /**
     * Constructs a new Emergency Room system.
     */
    public EmergencyRoom() {
        this.registry = new AVLPatientRegistry();
        this.treatmentQueue = new HeapTreatmentQueue();

        // Patient ID -> arrival time, so a treated patient can be removed from the registry.
        this.patientIdToArrivalTime = new HashMap<>();

        this.patientCounter = 0;
    }

    /**
     * Registers a new patient in the emergency room.
     *
     * The patient is added to both the registry (for administrative tracking)
     * and the treatment queue (for priority-based treatment).
     *
     * @param arrivalTime the timestamp when the patient arrived
     * @param patient the patient to register
     * @throws IllegalArgumentException if patient is null
     */
    public void registerPatient(int arrivalTime, Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient cannot be null");
        }

        // Add to registry with arrival time as key
        registry.put(arrivalTime, patient);

        // Add to treatment queue (prioritized by severity)
        treatmentQueue.insert(patient);

        // Remember the arrival time for this patient
        patientIdToArrivalTime.put(patient.getPatientId(), arrivalTime);

        patientCounter++;
    }

    /**
     * Gets the next patient to be treated (highest severity).
     *
     * Removes the patient with the highest severity from the treatment queue
     * and also removes them from the registry.
     *
     * @return the next patient to treat, or null if no patients waiting
     */
    public Patient treatNextPatient() {
        Patient patient = treatmentQueue.remove();

        if (patient != null) {
            // Look up the arrival time for this patient
            Integer arrivalTime = patientIdToArrivalTime.get(patient.getPatientId());
            if (arrivalTime != null) {
                // Remove from registry
                registry.remove(arrivalTime);
                // Remove from tracking map
                patientIdToArrivalTime.remove(patient.getPatientId());
            }
        }

        return patient;
    }

    /**
     * Peeks at the next patient without removing them.
     *
     * @return the next patient to be treated, or null if no patients waiting
     */
    public Patient peekNextPatient() {
        return treatmentQueue.peek();
    }

    /**
     * Updates a patient's severity level.
     *
     * This may change their position in the treatment queue. The patient
     * remains in the registry unchanged (arrival time doesn't change).
     *
     * @param patientId the ID of the patient to update
     * @param newSeverity the new severity level (1-5)
     * @return true if patient was found and updated, false otherwise
     * @throws IllegalArgumentException if newSeverity is not in range [1, 5]
     */
    public boolean updatePatientSeverity(int patientId, int newSeverity) {
        return treatmentQueue.updatePriority(patientId, newSeverity);
    }

    /**
     * Finds all patients who arrived within a specific time range.
     *
     * This is useful for administrative queries like "show all patients
     * who arrived in the last hour" or analyzing patient flow patterns.
     *
     * @param startTime beginning of time range (inclusive)
     * @param endTime end of time range (inclusive)
     * @return list of patients who arrived in the time range, sorted by arrival time
     * @throws IllegalArgumentException if startTime > endTime
     */
    public List<Patient> getPatientsInTimeRange(int startTime, int endTime) {
        return registry.rangeSearch(startTime, endTime);
    }

    /**
     * Searches for a patient by their arrival time.
     *
     * @param arrivalTime the arrival timestamp to search for
     * @return the patient with that arrival time, or null if not found
     */
    public Patient findPatientByArrivalTime(int arrivalTime) {
        return registry.get(arrivalTime);
    }

    /**
     * Gets all patients currently in the ER, sorted by arrival time.
     *
     * Uses the iterator from PatientRegistry to traverse in sorted order.
     *
     * @return list of all patients sorted by arrival time
     */
    public List<Patient> getAllPatientsByArrivalTime() {
        List<Patient> result = new java.util.ArrayList<>();
        for (Patient patient : registry) {
            result.add(patient);
        }
        return result;
    }

    /**
     * Returns the number of patients currently waiting for treatment.
     *
     * @return number of patients in the system
     */
    public int getWaitingCount() {
        return treatmentQueue.size();
    }

    /**
     * Checks if there are any patients waiting.
     *
     * @return true if no patients waiting, false otherwise
     */
    public boolean isEmpty() {
        return treatmentQueue.isEmpty();
    }

    /**
     * Checks if a specific patient is in the treatment queue.
     *
     * @param patientId the patient ID to check
     * @return true if patient is waiting for treatment, false otherwise
     */
    public boolean isPatientWaiting(int patientId) {
        return treatmentQueue.contains(patientId);
    }

    /**
     * Gets the total number of patients registered (including those already treated).
     *
     * @return total patient count
     */
    public int getTotalPatientsRegistered() {
        return patientCounter;
    }

    /**
     * Clears all patients from the emergency room.
     */
    public void clearAll() {
        registry.clear();
        treatmentQueue.clear();
        patientIdToArrivalTime.clear();
    }

    /**
     * Returns the number of patients who arrived before the specified arrival time.
     *
     * @param arrivalTime the arrival timestamp to query
     * @return the number of patients who arrived before the given time
     */
    public int rankByArrivalTime(int arrivalTime) {
        return registry.countPatientsBeforeTime(arrivalTime);
    }

    /**
     * Returns the patient with the k-th smallest arrival time.
     *
     * @param k the rank to select (1-indexed)
     * @return the patient with the k-th smallest arrival time
     * @throws IllegalArgumentException if k < 1
     */
    public Patient getKthByArrivalTime(int k) {
        return registry.kthByArrival(k);
    }

    /**
     * Removes a specific patient from the treatment queue.
     *
     * Useful when a patient leaves before treatment (e.g., AMA, transfer).
     * The patient remains in the registry but is removed from the treatment queue.
     *
     * @param patientId the ID of the patient to remove from queue
     * @return the removed patient, or null if not in queue
     */
    public Patient removePatientFromQueue(int patientId) {
        return treatmentQueue.remove(patientId);
    }

    /**
     * Returns the patient with the k-th highest severity.
     *
     * @param k the rank to query (1-indexed, 1 = most severe)
     * @return the patient with the k-th highest severity
     * @throws IllegalArgumentException if k < 1
     */
    public Patient getKthMostSeverePatient(int k) {
        return treatmentQueue.kthMostSevere(k);
    }

    /**
     * Gets the arrival time for a specific patient.
     *
     * @param patientId the patient ID to look up
     * @return the arrival time of the patient, or null if patient not found
     */
    public Integer getPatientArrivalTime(int patientId) {
        return patientIdToArrivalTime.get(patientId);
    }

    /**
     * Returns a summary of the ER status.
     *
     * @return formatted status string
     */
    @Override
    public String toString() {
        return String.format("EmergencyRoom [waiting=%d, total_registered=%d]",
                getWaitingCount(), getTotalPatientsRegistered());
    }
}
