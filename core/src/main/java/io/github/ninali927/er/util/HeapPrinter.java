package io.github.ninali927.er.util;

import io.github.ninali927.er.models.Patient;

/**
 * Utility class for printing binary heap structure.
 *
 * This provides an easy-to-understand view of the heap array structure for testing
 * and debugging purposes. The heap is printed level by level, showing the array
 * representation in a hierarchical format.
 *
 * Format: Each node is printed as severity:patientId (e.g., "5:2" means severity 5, patient ID 2)
 * This matches the format used by BinaryTreePrinter for consistency.
 */
public class HeapPrinter {

    /**
     * Prints a 1-indexed binary heap array level by level.
     *
     * Each level of the heap is printed on a separate line with elements
     * separated by spaces. Empty slots are not printed (array ends at last element).
     * Returns empty string if the heap has no elements.
     *
     * Format: severity:patientId for each patient (e.g., "5:2" for severity 5, patient ID 2)
     *
     * @param heap the 1-indexed heap array (index 0 is ignored)
     * @param size the number of elements in the heap
     * @return string representation of the heap structure
     */
    public static String printHeap(Object[] heap, int size) {
        if (size == 0) {
            return "";
        }

        StringBuilder s = new StringBuilder();
        int level = 0;
        int levelStart = 1;  // 2^0 = 1 (first index in 1-indexed heap)

        while (levelStart <= size) {
            int levelEnd = Math.min((1 << (level + 1)) - 1, size);  // 2^(level+1) - 1

            for (int i = levelStart; i <= levelEnd; i++) {
                Patient patient = (Patient) heap[i];
                s.append(patient.getSeverity()).append(":").append(patient.getPatientId());
                if (i < levelEnd) {
                    s.append(" ");
                }
            }

            s.append("\n");
            levelStart = levelEnd + 1;
            level++;
        }

        return s.toString();
    }
}
