package queue;

import models.Patient;
import queue.HeapTreatmentQueue;
import queue.TreatmentQueue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Heap-specific tests that verify binary heap structure and max-heap property.
 * These tests complement the general TreatmentQueue tests with
 * implementation-specific verification using toString() output.
 */
public class HeapTreatmentQueueTest extends TreatmentQueueTest {

  @Override
  public TreatmentQueue createTreatmentQueue() {
    return new HeapTreatmentQueue();
  }

  private void compareHeapLevels(String actual, String[] expectedLevels) {
    assertEquals(String.join("\n", expectedLevels) + "\n", actual);
  }

  @Test
  @DisplayName("Single insertion creates correct heap structure")
  public void singleInsertionStructure() {
    TreatmentQueue queue = createTreatmentQueue();
    Patient p1 = new Patient(1, "Alice", 3, "Flu");
    queue.insert(p1);

    // Format: severity:patientId
    compareHeapLevels(queue.toString(), new String[] {"3:1"});
  }

  @Test
  @DisplayName("ascending severities insertion maintains correct heap structure")
  public void ascendingInsertionStructure() {
    TreatmentQueue queue = createTreatmentQueue();
    for (int i = 1; i <= 5; i++) {
      Patient p = new Patient(i, "P" + i, i, "Condition" + i);
      queue.insert(p);
    }
    compareHeapLevels(queue.toString(), new String[] {"5:5", "4:4 2:2", "1:1 3:3"});
  }

  @Test
  @DisplayName("decending severities insertion maintains correct heap structure")
  public void decendingInsertionStructure() {
    TreatmentQueue queue = createTreatmentQueue();
    for (int i = 5; i >= 1; i--) {
      Patient p = new Patient(i, "P" + i, 6 - i, "Condition" + i);
      queue.insert(p);
    }
    compareHeapLevels(queue.toString(), new String[] {"5:1", "4:2 2:4", "1:5 3:3"});
  }

  @Test
  @DisplayName("unordered severities insertion maintains correct heap structure")
  public void unorderedInsertionStructure() {
    TreatmentQueue queue = createTreatmentQueue();
    Patient p1 = new Patient(1, "P1", 3, "Condition1");
    Patient p2 = new Patient(2, "P2", 5, "Condition2");
    Patient p3 = new Patient(3, "P3", 4, "Condition3");
    Patient p4 = new Patient(4, "P4", 2, "Condition4");
    Patient p5 = new Patient(5, "P5", 1, "Condition5");

    queue.insert(p1);
    queue.insert(p2);
    queue.insert(p3);
    queue.insert(p4);
    queue.insert(p5);

    compareHeapLevels(queue.toString(), new String[] {"5:2", "3:1 4:3", "2:4 1:5"});
  }

  @Test
  @DisplayName("Remove root rebalances the heap")
  public void removeRootRebalancesHeap() {
    TreatmentQueue queue = createTreatmentQueue();
    Patient p1 = new Patient(1, "P1", 1, "Condition1");
    Patient p2 = new Patient(2, "P2", 2, "Condition2");
    Patient p3 = new Patient(3, "P3", 3, "Condition3");
    queue.insert(p1);
    queue.insert(p2);
    queue.insert(p3);

    queue.remove();
    compareHeapLevels(queue.toString(), new String[] {"2:2", "1:1"});
  }

  @Test
  @DisplayName("Remove middle node rebalances the heap")
  public void removeMiddleRebalanceHeap() {
    TreatmentQueue queue = createTreatmentQueue();
    Patient p1 = new Patient(1, "P1", 1, "Condition1");
    Patient p2 = new Patient(2, "P2", 2, "Condition2");
    Patient p3 = new Patient(3, "P3", 3, "Condition3");
    queue.insert(p1);
    queue.insert(p2);
    queue.insert(p3);
    queue.remove(2);

    compareHeapLevels(queue.toString(), new String[] {"3:3", "1:1"});
  }

  @Test
  @DisplayName("Remove leaf node rebalances the heap")
  public void checkMaxHeapRemovingLeaf() {
    TreatmentQueue queue = createTreatmentQueue();
    Patient p1 = new Patient(1, "P1", 1, "Condition1");
    Patient p2 = new Patient(2, "P2", 2, "Condition2");
    Patient p3 = new Patient(3, "P3", 3, "Condition3");
    queue.insert(p1);
    queue.insert(p2);
    queue.insert(p3);
    queue.remove(1);

    compareHeapLevels(queue.toString(), new String[] {"3:3", "2:2"});
  }

  @Test
  @DisplayName("Remove nonexistent node does not change heap")
  public void removeNonexistentKeepsHeapUnchanged() {
    TreatmentQueue queue = createTreatmentQueue();
    Patient p1 = new Patient(1, "P1", 1, "Condition1");
    Patient p2 = new Patient(2, "P2", 2, "Condition2");
    Patient p3 = new Patient(3, "P3", 3, "Condition3");
    queue.insert(p1);
    queue.insert(p2);
    queue.insert(p3);
    queue.remove(4);

    compareHeapLevels(queue.toString(), new String[] {"3:3", "1:1 2:2"});
  }

  @Test
  @DisplayName("UpdatePriority bubbles up correctly")
  public void updatePriorityBubbleUp() {
    TreatmentQueue queue = createTreatmentQueue();
    Patient p1 = new Patient(1, "P1", 1, "Condition1");
    Patient p2 = new Patient(2, "P2", 2, "Condition2");
    Patient p3 = new Patient(3, "P3", 3, "Condition3");
    queue.insert(p1);
    queue.insert(p2);
    queue.insert(p3);
    queue.updatePriority(1, 4);

    compareHeapLevels(queue.toString(), new String[] {"4:1", "3:3 2:2"});
  }

  @Test
  @DisplayName("UpdatePriority bubbles down correctly")
  public void updatePriorityBubbleDown() {
    TreatmentQueue queue = createTreatmentQueue();
    Patient p1 = new Patient(1, "P1", 1, "Condition1");
    Patient p2 = new Patient(2, "P2", 2, "Condition2");
    Patient p3 = new Patient(3, "P3", 3, "Condition3");
    queue.insert(p1);
    queue.insert(p2);
    queue.insert(p3);
    queue.updatePriority(3, 1);

    compareHeapLevels(queue.toString(), new String[] {"2:2", "1:1 1:3"});
  }


}
