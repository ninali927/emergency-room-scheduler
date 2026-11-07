package queue;

import models.Patient;
import queue.TreatmentQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for TreatmentQueue ADT.
 *
 * These tests verify functional correctness of priority queue operations
 * independent of the underlying implementation. Tests focus on behavior
 * (highest priority returned first) rather than internal structure.
 */
public abstract class TreatmentQueueTest {

    private TreatmentQueue queue;

    public abstract TreatmentQueue createTreatmentQueue();

    @BeforeEach
    public void setUp() {
        queue = createTreatmentQueue();
    }

    @Test
    @DisplayName("Queue is empty after construction")
    public void emptyAfterConstruction() {
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    @DisplayName("empty queue returns null")
    public void operationsOnEmptyQueue() {
        TreatmentQueue queue = createTreatmentQueue();
        assertNull(queue.remove());
        assertNull(queue.peek());
        assertNull(queue.kthMostSevere(1));
    }

    @Test
    @DisplayName("Insert, insert with null argument")
    public void insertTest() {
        TreatmentQueue queue = createTreatmentQueue();

        //insert
        Patient p1 = new Patient(1, "P1", 1, "Condition1");
        queue.insert(p1);
        assertFalse(queue.isEmpty());
        assertEquals(1, queue.size());

        //insert null
        assertThrows(IllegalArgumentException.class, () -> queue.insert(null));
    }

    @Test
    @DisplayName("Remove returns the removed node")
    public void removeTest() {
        TreatmentQueue queue = createTreatmentQueue();
        Patient p1 = new Patient(1, "P1", 1, "Condition1");
        Patient p2 = new Patient(2, "P2", 2, "Condition2");
        Patient p3 = new Patient(3, "P3", 3, "Condition3");
        queue.insert(p1);
        queue.insert(p2);
        queue.insert(p3);

        Patient removed = queue.remove();
        assertEquals(3, removed.getSeverity());
        assertEquals(3, removed.getPatientId());
        assertEquals(2, queue.size());
    }

    @Test
    @DisplayName("Peek gives the highest priority node")
    public void peekTest() {
        TreatmentQueue queue = createTreatmentQueue();
        Patient p1 = new Patient(1, "P1", 1, "Condition1");
        Patient p2 = new Patient(2, "P2", 2, "Condition2");
        Patient p3 = new Patient(3, "P3", 3, "Condition3");
        queue.insert(p1);
        queue.insert(p2);
        queue.insert(p3);

        Patient peeked = queue.peek();
        assertEquals(3, peeked.getSeverity());
        assertEquals(3, peeked.getPatientId());
        assertEquals(3, queue.size());
    }

    @Test
    @DisplayName("UpdatePriority, updatePriority with nonexistent patient, updatePriority with invalid arguments")
    public void updatePriorityTest() {
        TreatmentQueue queue = createTreatmentQueue();
        Patient p1 = new Patient(1, "P1", 1, "Condition1");
        Patient p2 = new Patient(2, "P2", 2, "Condition2");
        Patient p3 = new Patient(3, "P3", 3, "Condition3");
        queue.insert(p1);
        queue.insert(p2);
        queue.insert(p3);

        //update priority
        queue.updatePriority(1, 4);
        assertEquals(4, queue.peek().getSeverity());

        //nonexistent patient
        assertFalse(queue.updatePriority(100, 1));

        //invalid severity - too high
        assertThrows(IllegalArgumentException.class, () -> queue.updatePriority(1, 100));

        //invalid severity - too low
        assertThrows(IllegalArgumentException.class, () -> queue.updatePriority(1, 0));
    }

    @Test
    @DisplayName("Remove specific patient, remove with nonexistent patient")
    public void removeSpecificPatient() {
        TreatmentQueue queue = createTreatmentQueue();
        Patient p1 = new Patient(1, "P1", 1, "Condition1");
        Patient p2 = new Patient(2, "P2", 2, "Condition2");
        Patient p3 = new Patient(3, "P3", 3, "Condition3");
        queue.insert(p1);
        queue.insert(p2);
        queue.insert(p3);

        //remove
        Patient removed = queue.remove(2);
        assertEquals(2, removed.getPatientId());
        assertEquals(2, removed.getSeverity());
        assertEquals(2, queue.size());

        //remove nonexistent patient
        Patient removed2 = queue.remove(100);
        assertNull(removed2);
        assertEquals(2, queue.size());
    }

    @Test
    @DisplayName("kthMostSevere, kthMostSevere with invalid argument")
    public void kthMostSevereTest() {
        TreatmentQueue queue = createTreatmentQueue();
        Patient p1 = new Patient(1, "P1", 1, "Condition1");
        Patient p2 = new Patient(2, "P2", 2, "Condition2");
        Patient p3 = new Patient(3, "P3", 3, "Condition3");
        queue.insert(p1);
        queue.insert(p2);
        queue.insert(p3);

        assertEquals(3, queue.kthMostSevere(1).getSeverity());
        assertEquals(2, queue.kthMostSevere(2).getSeverity());
        assertEquals(1, queue.kthMostSevere(3).getSeverity());

        assertEquals(3, queue.size());

        //k < 1
        assertThrows(IllegalArgumentException.class, () -> queue.kthMostSevere(0));

        //k > size
        assertNull(queue.kthMostSevere(100));
    }

    @Test
    @DisplayName("Contains, contains with nonexistent patient")
    public void ContainsTest() {
        TreatmentQueue queue = createTreatmentQueue();
        Patient p1 = new Patient(1, "P1", 1, "Condition1");
        Patient p2 = new Patient(2, "P2", 2, "Condition2");
        Patient p3 = new Patient(3, "P3", 3, "Condition3");
        queue.insert(p1);
        queue.insert(p2);
        queue.insert(p3);

        //contains
        assertTrue(queue.contains(1));

        //contains nonexistent patient
        assertFalse(queue.contains(100));
    }

    @Test
    @DisplayName("Clear empties queue")
    public void clearEmpties() {
        TreatmentQueue queue = createTreatmentQueue();
        Patient p1 = new Patient(1, "P1", 1, "Condition1");
        Patient p2 = new Patient(2, "P2", 2, "Condition2");
        Patient p3 = new Patient(3, "P3", 3, "Condition3");
        queue.insert(p1);
        queue.insert(p2);
        queue.insert(p3);

        queue.clear();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
        assertNull(queue.peek());
    }



}
