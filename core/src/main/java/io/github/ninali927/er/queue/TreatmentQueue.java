package io.github.ninali927.er.queue;

import io.github.ninali927.er.models.Patient;

/**
 * An interface for a treatment priority queue.
 *
 * The TreatmentQueue maintains patients ordered by severity level (priority).
 * Higher severity patients (severity level 5) have higher priority and should be treated first.
 *
 * This queue supports adaptive priority updates, allowing a patient's severity
 * to be changed after they are already in the queue.
 */
public interface TreatmentQueue {

    /**
     * Inserts a patient into the treatment queue.
     *
     * The patient is positioned based on their severity level. Patients with
     * higher severity (closer to 5) have higher priority.
     *
     * @param patient the patient to insert
     * @throws IllegalArgumentException if patient is null
     */
    void insert(Patient patient);

    /**
     * Removes and returns the patient with the highest severity (highest priority).
     *
     * In case of ties (patients with same severity), the implementation may choose
     * any of the tied patients.
     *
     * @return the patient with the highest severity, or null if queue is empty
     */
    Patient remove();

    /**
     * Returns the patient with the highest severity without removing them.
     *
     * @return the patient with the highest severity, or null if queue is empty
     */
    Patient peek();

    /**
     * Updates the severity (priority) of a patient already in the queue.
     *
     * When a patient's condition changes, their severity can be updated, which
     * may require repositioning them in the queue to maintain proper ordering.
     *
     * @param patientId the ID of the patient to update
     * @param newSeverity the new severity level (1-5)
     * @return true if patient was found and updated, false if patient not in queue
     * @throws IllegalArgumentException if newSeverity is not in range [1, 5]
     */
    boolean updatePriority(int patientId, int newSeverity);

    /**
     * Removes a specific patient from the queue by patient ID.
     *
     * This operation is useful when a patient leaves the ER before treatment
     * (e.g., against medical advice, transferred to another hospital).
     *
     * @param patientId the ID of the patient to remove
     * @return the removed patient, or null if patient was not in the queue
     */
    Patient remove(int patientId);

    /**
     * Returns the patient with the k-th highest severity without removing them.
     *
     * This operation allows querying the queue for the k-th most severe patient.
     * k=1 returns the most severe (same as peek), k=2 returns second most severe, etc.
     *
     * @param k the rank to query (1-indexed)
     * @return the patient with the k-th highest severity, or null if k is out of bounds
     * @throws IllegalArgumentException if k < 1
     */
    Patient kthMostSevere(int k);

    /**
     * Returns the number of patients currently in the queue.
     *
     * @return the number of patients
     */
    int size();

    /**
     * Checks if the queue is empty.
     *
     * @return true if the queue contains no patients, false otherwise
     */
    boolean isEmpty();

    /**
     * Checks if a patient is currently in the treatment queue.
     *
     * @param patientId the ID of the patient to check
     * @return true if the patient is in the queue, false otherwise
     */
    boolean contains(int patientId);

    /**
     * Removes all patients from the queue.
     */
    void clear();
}
