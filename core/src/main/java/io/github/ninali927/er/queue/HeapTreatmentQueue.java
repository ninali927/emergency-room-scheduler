package io.github.ninali927.er.queue;

import io.github.ninali927.er.models.Patient;
import io.github.ninali927.er.util.HeapPrinter;
import java.util.Arrays;

/**
 * Binary Heap implementation of the TreatmentQueue interface with adaptive priority updates.
 *
 * This class implements a max-heap based on severity, where patients with higher severity
 * values are treated first. The root of the heap always contains the patient with the
 * highest severity.
 *
 * The key feature is adaptive priority updates: when a patient's severity changes, we can
 * update their position in the heap to maintain the max-heap property.
 */
public class HeapTreatmentQueue implements TreatmentQueue {

    private static final int DEFAULT_CAPACITY = 16;
    private Patient[] heap;
    private int size;

    /**
     * Constructs a new TreatmentQueue with default capacity.
     */
    public HeapTreatmentQueue() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Constructs a new TreatmentQueue with specified initial capacity.
     */
    public HeapTreatmentQueue(int initialCapacity) {
        this.heap = new Patient[initialCapacity + 1]; // 1-indexed for easier parent/child calculations
        this.size = 0;
    }

    @Override
    public void insert(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient cannot be null.");
        }
        if (size + 1 == heap.length) {
            resize();
        }
        size++;
        heap[size] = patient;
        bubbleUp(size);
    }

    @Override
    public Patient remove() {
        if (isEmpty()) {
            return null;
        }
        Patient root = heap[1];
        heap[1] = heap[size];
        heap[size] = null;
        size--;
        bubbleDown(1);
        return root;
    }

    @Override
    public Patient peek() {
        if (isEmpty()) {
            return null;
        }
        return heap[1];
    }

    @Override
    public boolean updatePriority(int patientId, int newSeverity) {
        if (newSeverity > 5 || newSeverity < 1) {
            throw new IllegalArgumentException("Severity must be in range [1,5].");
        }
        int index = findPatientIndex(patientId);
        if (index == -1) {
            return false;
        }
        int oldSeverity = heap[index].getSeverity();
        heap[index].setSeverity(newSeverity);
        if (newSeverity > oldSeverity) {
            bubbleUp(index);
        } else {
            bubbleDown(index);
        }
        return true;
    }

    @Override
    public Patient remove(int patientId) {
        int index = findPatientIndex(patientId);
        if (index == -1) {
            return null;
        }

        Patient removed = heap[index];
        heap[index] = heap[size];
        heap[size] = null;
        size--;
        if (index <= size) {
            bubbleUp(index);
            bubbleDown(index);
        }
        return removed;
    }


    @Override
    public Patient kthMostSevere(int k) {
        if (k < 1) {
            throw new IllegalArgumentException("k must be greater than 1.");
        }
        if (k > size) {
            return null;
        }

        Patient[] originalHeap = heap;
        int originalSize = size;
        heap = Arrays.copyOf(originalHeap, originalHeap.length);
        int tempSize = size;

        for (int i = 1; i <= k - 1; i++) {
            heap[1] = heap[tempSize];
            heap[tempSize] = null;
            tempSize--;
            size = tempSize;
            bubbleDown(1);
        }

        Patient kthPatient = heap[1];
        heap = originalHeap;
        size = originalSize;
        return kthPatient;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(int patientId) {
        return findPatientIndex(patientId) != -1;
    }

    @Override
    public void clear() {
        Arrays.fill(heap, 1, size + 1, null);
        size = 0;
    }

    @Override
    public String toString() {
        return HeapPrinter.printHeap(heap, size);
    }

    // ------------ Helper methods --------------------

    // Restores heap property by moving element up.
    protected void bubbleUp(int index) {
        int parentIndex = index / 2;
        while (index > 1 && heap[index].getSeverity() > heap[parentIndex].getSeverity()) {
            swap(index, parentIndex);
            index = parentIndex;
            parentIndex = index / 2;
        }
    }

    // Restores heap property by moving element down.
    protected void bubbleDown(int index) {
        while (hasLeft(index)) {
            int largerChildIndex = leftIndex(index);

            if (hasRight(index) && heap[rightIndex(index)].getSeverity() > heap[largerChildIndex]
                    .getSeverity()) {
                largerChildIndex = rightIndex(index);
            }

            if (heap[index].getSeverity() > heap[largerChildIndex].getSeverity()) {
                break;
            }

            swap(index, largerChildIndex);
            index = largerChildIndex;
        }
    }

    private int leftIndex(int parentIndex) {
        return 2 * parentIndex;
    }

    private int rightIndex(int parentIndex) {
        return 2 * parentIndex + 1;
    }

    private boolean hasLeft(int parentIndex) {
        return leftIndex(parentIndex) <= size;
    }

    private boolean hasRight(int parentIndex) {
        return rightIndex(parentIndex) <= size;
    }

    private void resize() {
        Patient[] newHeap = new Patient[heap.length * 2];
        System.arraycopy(heap, 0, newHeap, 0, heap.length);
        heap = newHeap;
    }

    private void swap(int i, int j) {
        Patient temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    private int findPatientIndex(int patientId) {
        for (int i = 1; i <= size; i++) {
            if (heap[i].getPatientId() == patientId) {
                return i;
            }
        }
        return -1;
    }
}
