package io.github.ninali927.er.registry;

import io.github.ninali927.er.models.Patient;
import io.github.ninali927.er.registry.AVLPatientRegistry;
import io.github.ninali927.er.registry.PatientRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AVL-specific tests that verify tree structure and rotation behavior.
 * These tests complement the general PatientRegistry tests with
 * implementation-specific verification.
 */
public class AVLPatientRegistryTest extends PatientRegistryTest {

  @Override
  public PatientRegistry createPatientRegistry() {
    return new AVLPatientRegistry();
  }

  private void compareLevelOrderStrings(String actual, String[] expectedLevels) {
    assertEquals(String.join("\n", expectedLevels) + "\n", actual);
  }

  @Test
  @DisplayName("Insert causes left rotation")
  public void insertLeftRotation() {
    PatientRegistry registry = createPatientRegistry();
    registry.put(100, new Patient(1, "P1", 1, "Condition1"));
    compareLevelOrderStrings(registry.toString(), new String[] {"100:1"});

    registry.put(200, new Patient(2, "P2", 1, "Condition2"));
    compareLevelOrderStrings(registry.toString(), new String[] {"100:1", "null 200:2"});

    registry.put(300, new Patient(3, "P3", 1, "Condition3"));
    compareLevelOrderStrings(registry.toString(), new String[] {"200:2", "100:1 300:3"});
  }

  @Test
  @DisplayName("Insert causes right rotation")
  public void insertRightRotation() {
    PatientRegistry registry = createPatientRegistry();
    registry.put(300, new Patient(1, "P1", 1, "Condition1"));
    compareLevelOrderStrings(registry.toString(), new String[] {"300:1"});

    registry.put(200, new Patient(2, "P2", 1, "Condition2"));
    compareLevelOrderStrings(registry.toString(), new String[] {"300:1", "200:2 null"});

    registry.put(100, new Patient(3, "P3", 1, "Condition3"));
    compareLevelOrderStrings(registry.toString(), new String[] {"200:2", "100:3 300:1"});
  }

  @Test
  @DisplayName("Insert causes left-right rotation")
  public void insertLeftRightRotation() {
    PatientRegistry registry = createPatientRegistry();
    registry.put(300, new Patient(1, "P1", 1, "Condition1"));
    compareLevelOrderStrings(registry.toString(), new String[] {"300:1"});

    registry.put(100, new Patient(2, "P2", 1, "Condition2"));
    compareLevelOrderStrings(registry.toString(), new String[] {"300:1", "100:2 null"});

    registry.put(200, new Patient(3, "P3", 1, "Condition3"));
    compareLevelOrderStrings(registry.toString(), new String[] {"200:3", "100:2 300:1"});
  }

  @Test
  @DisplayName("Insert causes right-left rotation")
  public void insertRightLeftRotation() {
    PatientRegistry registry = createPatientRegistry();
    registry.put(100, new Patient(1, "P1", 1, "Condition1"));
    compareLevelOrderStrings(registry.toString(), new String[] {"100:1"});

    registry.put(300, new Patient(2, "P2", 1, "Condition2"));
    compareLevelOrderStrings(registry.toString(), new String[] {"100:1", "null 300:2"});

    registry.put(200, new Patient(3, "P3", 1, "Condition3"));
    compareLevelOrderStrings(registry.toString(), new String[] {"200:3", "100:1 300:2"});
  }

  @Test
  @DisplayName("check if balanced after ascending insertion")
  public void checkBalancedInsertionSmalltoLarge() {
    PatientRegistry registry = createPatientRegistry();
    for (int i = 1; i <= 5; i++) {
      registry.put(i * 10, new Patient(i, "P" + i, 1, "Condition" + i));
    }

    compareLevelOrderStrings(registry.toString(),
        new String[] {"20:2", "10:1 40:4", "null null 30:3 50:5"});
  }

  @Test
  @DisplayName("check if balanced after decending insertion")
  public void checkBalancedInsertionLargetoSmall() {
    PatientRegistry registry = createPatientRegistry();
    registry.put(50, new Patient(1, "P1", 1, "Condition1"));
    registry.put(40, new Patient(2, "P2", 1, "Condition2"));
    registry.put(30, new Patient(3, "P3", 1, "Condition3"));
    registry.put(20, new Patient(4, "P4", 1, "Condition4"));
    registry.put(10, new Patient(5, "P5", 1, "Condition5"));
    compareLevelOrderStrings(registry.toString(),
        new String[] {"40:2", "20:4 50:1", "10:5 30:3 null null"});
  }

  @Test
  @DisplayName("check if balanced after inserting in random order")
  public void checkBalancedInsertionRandomly() {
    PatientRegistry registry = createPatientRegistry();
    registry.put(40, new Patient(1, "P1", 1, "Condition1"));
    registry.put(10, new Patient(2, "P2", 1, "Condition2"));
    registry.put(50, new Patient(3, "P3", 1, "Condition3"));
    registry.put(20, new Patient(4, "P4", 1, "Condition4"));
    registry.put(30, new Patient(5, "P5", 1, "Condition5"));
    compareLevelOrderStrings(registry.toString(),
        new String[] {"40:1", "20:4 50:3", "10:2 30:5 null null"});
  }

  @Test
  @DisplayName("check if balanced after deleting root")
  public void checkBalancedDeletingRoot() {
    PatientRegistry registry = createPatientRegistry();
    registry.put(40, new Patient(1, "Patient1", 1, "Condition1"));
    registry.put(20, new Patient(2, "Patient2", 2, "Condition2"));
    registry.put(50, new Patient(3, "Patient3", 3, "Condition3"));
    registry.put(10, new Patient(4, "Patient4", 4, "Condition4"));
    registry.put(30, new Patient(5, "Patient5", 5, "Condition5"));

    compareLevelOrderStrings(registry.toString(),
        new String[] {"40:1", "20:2 50:3", "10:4 30:5 null null"});
    registry.remove(40);
    compareLevelOrderStrings(registry.toString(),
        new String[] {"20:2", "10:4 50:3", "null null 30:5 null"});
  }

  @Test
  @DisplayName("check if balanced after deleting middle node")
  public void checkBalancedDeletingMiddleNode() {
    PatientRegistry registry = createPatientRegistry();
    registry.put(40, new Patient(1, "P1", 1, "Condition1"));
    registry.put(20, new Patient(2, "P2", 1, "Condition2"));
    registry.put(50, new Patient(3, "P3", 1, "Condition3"));
    registry.put(10, new Patient(4, "P4", 1, "Condition4"));
    registry.put(30, new Patient(5, "P5", 1, "Condition5"));
    compareLevelOrderStrings(registry.toString(),
        new String[] {"40:1", "20:2 50:3", "10:4 30:5 null null"});
    registry.remove(20);
    compareLevelOrderStrings(registry.toString(),
        new String[] {"40:1", "30:5 50:3", "10:4 null null null"});
  }

  @Test
  @DisplayName("check if balanced after deleting leaf")
  public void checkBalancedDeletingLeaf() {
    PatientRegistry registry = createPatientRegistry();
    registry.put(40, new Patient(1, "P1", 1, "Condition1"));
    registry.put(20, new Patient(2, "P2", 1, "Condition2"));
    registry.put(50, new Patient(3, "P3", 1, "Condition3"));
    registry.put(10, new Patient(4, "P4", 1, "Condition4"));
    registry.put(30, new Patient(5, "P5", 1, "Condition5"));
    compareLevelOrderStrings(registry.toString(),
        new String[] {"40:1", "20:2 50:3", "10:4 30:5 null null"});
    registry.remove(50);
    compareLevelOrderStrings(registry.toString(),
        new String[] {"20:2", "10:4 40:1", "null null 30:5 null"});
  }

  @Test
  @DisplayName("Remove nonexistent key will not change tree structure")
  public void checkTreeRemoveNonexistentKey() {
    PatientRegistry registry = createPatientRegistry();
    registry.put(40, new Patient(1, "P1", 1, "Condition1"));
    registry.put(20, new Patient(2, "P2", 1, "Condition2"));
    registry.put(50, new Patient(3, "P3", 1, "Condition3"));
    registry.put(10, new Patient(4, "P4", 1, "Condition4"));
    registry.put(30, new Patient(5, "P5", 1, "Condition5"));
    int sizeBefore = registry.size();
    Patient removed = registry.remove(100);
    assertNull(removed);
    assertEquals(sizeBefore, registry.size());
    compareLevelOrderStrings(registry.toString(),
        new String[] {"40:1", "20:2 50:3", "10:4 30:5 null null"});
  }
}
