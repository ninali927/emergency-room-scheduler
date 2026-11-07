package io.github.ninali927.er.registry;

import io.github.ninali927.er.models.Patient;
import java.util.List;

/**
 * A Map-like interface for a patient registry data structure.
 *
 * The PatientRegistry maintains a mapping from arrival timestamps (keys) to
 * Patient objects (values).
 *
 * The registry is iterable: the iterator goes over the patients in this registry
 * in ascending order of arrival time.
 *
 * SIMPLIFYING ASSUMPTION: We assume no two patients arrive at exactly the same
 * timestamp. This allows us to use arrival time as a unique key.
 *
 */
public interface PatientRegistry extends Iterable<Patient> {

    /**
     * Associates the specified patient with the specified arrival time in this registry.
     *
     * If the registry previously contained a mapping for the arrival time, the old
     * patient is replaced by the specified patient.
     *
     * @param arrivalTime arrival timestamp to use as the key
     * @param patient patient to be associated with the specified arrival time
     * @throws IllegalArgumentException if patient is null or arrivalTime is negative
     */
    void put(int arrivalTime, Patient patient);

    /**
     * Returns the patient associated with the specified arrival time.
     *
     * @param arrivalTime the arrival timestamp whose associated patient is to be returned
     * @return the patient associated with the given arrival time, or null if not found
     * @throws IllegalArgumentException if arrivalTime is negative
     */
    Patient get(int arrivalTime);

    /**
     * Removes the mapping for the specified arrival time from this registry if present.
     *
     * @param arrivalTime arrival timestamp whose mapping is to be removed
     * @return the patient previously associated with the arrival time, or null if none
     * @throws IllegalArgumentException if arrivalTime is negative
     */
    Patient remove(int arrivalTime);

    /**
     * Returns true if this registry contains a mapping for the specified arrival time.
     *
     * @param arrivalTime arrival timestamp whose presence is to be tested
     * @return true if this registry contains a mapping for the specified key
     * @throws IllegalArgumentException if arrivalTime is negative
     */
    boolean containsKey(int arrivalTime);

    /**
     * Finds all patients whose arrival timestamps fall within the specified range.
     *
     * This is a range query operation that returns all patients who arrived
     * between startTime and endTime (inclusive). The results are returned in
     * sorted order by arrival time.
     *
     * @param startTime the beginning of the time range (inclusive)
     * @param endTime the end of the time range (inclusive)
     * @return a list of patients in the given time range, sorted by arrival time
     * @throws IllegalArgumentException if startTime > endTime or if either time is negative
     */
    List<Patient> rangeSearch(int startTime, int endTime);

    /**
     * Returns the number of patients currently in the registry.
     *
     * @return the number of patients
     */
    int size();

    /**
     * Checks if the registry is empty.
     *
     * @return true if the registry contains no patients, false otherwise
     */
    boolean isEmpty();

    /**
     * Removes all patients from the registry.
     *
     * Note: In a real medical system, patient arrival records would be
     * append-only and never cleared, but this operation is provided for
     * testing and demonstration purposes.
     */
    void clear();

    /**
     * Finds the patient who arrived earliest (minimum arrival time).
     *
     * @return the patient with the minimum arrival timestamp, or null if registry is empty
     */
    Patient findMin();

    /**
     * Finds the patient who arrived most recently (maximum arrival time).
     *
     * @return the patient with the maximum arrival timestamp, or null if registry is empty
     */
    Patient findMax();

    /**
     * Returns the number of patients who arrived before the specified arrival time.
     *
     * @param arrivalTime the arrival timestamp to query
     * @return the number of patients who arrived before the given time
     * @throws IllegalArgumentException if arrivalTime is negative
     */
    int countPatientsBeforeTime(int arrivalTime);

    /**
     * Returns the patient with the k-th smallest arrival time.
     *
     * @param k the rank to select (1-indexed)
     * @return the patient with the k-th smallest arrival time, or null if k is out of bounds
     * @throws IllegalArgumentException if k < 1
     */
    Patient kthByArrival(int k);
}
