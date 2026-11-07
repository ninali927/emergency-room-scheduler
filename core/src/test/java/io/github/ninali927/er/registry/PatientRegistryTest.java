package io.github.ninali927.er.registry;

import io.github.ninali927.er.models.Patient;
import io.github.ninali927.er.registry.PatientRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGenerator.Simple;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;

public abstract class PatientRegistryTest {

    private PatientRegistry registry;

    public abstract PatientRegistry createPatientRegistry();

    @BeforeEach
    void setUp() {
        registry = createPatientRegistry();
    }

    @Test
    @DisplayName("Registry is empty after construction")
    public void emptyAfterConstruction() {
        assertTrue(registry.isEmpty());
        assertEquals(0, registry.size());
    }

    @Test
    @DisplayName("empty registry returns null")
    public void emptyRegistryReturnsNull() {
        PatientRegistry registry = createPatientRegistry();
        assertNull(registry.get(100));
        assertNull(registry.remove(100));
        assertNull(registry.findMin());
        assertNull(registry.findMax());
    }

    @Test
    @DisplayName("Put, put with duplicate key, put with invalid arguments")
    public void putTest() {
        PatientRegistry registry = createPatientRegistry();
        //put
        registry.put(100, new Patient(1, "P1", 1, "Condition1"));
        assertEquals(1, registry.size());

        //duplicate key
        registry.put(100, new Patient(2, "P2", 1, "Condition2"));
        assertEquals(1, registry.size());

        //invalid arguments
        assertThrows(IllegalArgumentException.class,
                () -> registry.put(-100, new Patient(1, "P1", 1, "Condition1")));
        assertThrows(IllegalArgumentException.class, () -> registry.put(10, null));
    }

    @Test
    @DisplayName("Get, get with nonexistent key, get with invalid argument")
    public void getTest() {
        PatientRegistry registry = createPatientRegistry();
        //get
        registry.put(100, new Patient(1, "P1", 1, "Condition1"));
        Patient retrieved = registry.get(100);
        assertNotNull(retrieved);
        assertEquals("P1", retrieved.getName());

        //nonexistnet key
        assertNull(registry.get(200));

        //invalid argument
        assertThrows(IllegalArgumentException.class, () -> registry.get(-100));
    }

    @Test
    @DisplayName("Remove, remove with nonexistent key, remove with invalid argument")
    public void removeTest() {
        PatientRegistry registry = createPatientRegistry();
        registry.put(100, new Patient(1, "P1", 1, "Condition1"));
        registry.put(200, new Patient(2, "P2", 1, "Condition2"));

        //remove
        registry.remove(100);
        assertEquals(1, registry.size());

        //nonexistent key
        Patient removed2 = registry.remove(300);
        assertNull(removed2);
        assertEquals(1, registry.size());

        //invalid argument
        assertThrows(IllegalArgumentException.class, () -> registry.remove(-100));
    }


    @Test
    @DisplayName("Contains, contains with invalid argument")
    public void containsTest() {
        PatientRegistry registry = createPatientRegistry();

        //contains
        registry.put(100, new Patient(1, "P1", 1, "Condition1"));
        assertTrue(registry.containsKey(100));
        assertFalse(registry.containsKey(200));

        //invalid argument
        assertThrows(IllegalArgumentException.class, () -> registry.containsKey(-100));
    }


    //rangeSearch
    @Test
    @DisplayName("rangeSearch, rangeSearch with invalid argument")
    public void rangeSearchTest() {
        PatientRegistry registry = createPatientRegistry();
        registry.put(100, new Patient(1, "P1", 1, "Condition1"));
        registry.put(200, new Patient(2, "P2", 1, "Condition2"));
        registry.put(300, new Patient(3, "P3", 1, "Condition3"));
        registry.put(400, new Patient(4, "P4", 1, "Condition4"));
        registry.put(500, new Patient(5, "P5", 1, "Condition5"));
        List<Patient> result = registry.rangeSearch(150, 450);
        //range search
        assertEquals(3, result.size());
        assertEquals(2, result.get(0).getPatientId());
        assertEquals(3, result.get(1).getPatientId());
        assertEquals(4, result.get(2).getPatientId());
        assertTrue(registry.rangeSearch(1, 5).isEmpty());

        //invalid argument
        assertThrows(IllegalArgumentException.class, () -> registry.rangeSearch(-150, 450));
        assertThrows(IllegalArgumentException.class, () -> registry.rangeSearch(150, -450));
        assertThrows(IllegalArgumentException.class, () -> registry.rangeSearch(450, 150));
    }


    //findMin & findMax
    @Test
    @DisplayName("findMin and findMax return correct patients")
    public void findMinFindMaxTest() {
        PatientRegistry registry = createPatientRegistry();
        registry.put(100, new Patient(1, "P1", 1, "Condition1"));
        registry.put(200, new Patient(2, "P2", 1, "Condition2"));
        registry.put(300, new Patient(3, "P3", 1, "Condition3"));
        registry.put(400, new Patient(4, "P4", 1, "Condition4"));
        registry.put(500, new Patient(5, "P5", 1, "Condition5"));

        assertEquals(1, registry.findMin().getPatientId());
        assertEquals(5, registry.findMax().getPatientId());
    }

    //countPatientsBeforeTime
    @Test
    @DisplayName("countPatientsBeforeTime, counts with invalid argument")
    public void countPatientsBeforeTimeTest() {
        PatientRegistry registry = createPatientRegistry();
        registry.put(100, new Patient(1, "P1", 1, "Condition1"));
        registry.put(200, new Patient(2, "P2", 1, "Condition2"));
        registry.put(300, new Patient(3, "P3", 1, "Condition3"));
        registry.put(400, new Patient(4, "P4", 1, "Condition4"));
        registry.put(500, new Patient(5, "P5", 1, "Condition5"));

        //count patients
        assertEquals(0, registry.countPatientsBeforeTime(100));
        assertEquals(2, registry.countPatientsBeforeTime(250));
        assertEquals(4, registry.countPatientsBeforeTime(500));

        //invalid argument
        assertThrows(IllegalArgumentException.class, () -> registry.countPatientsBeforeTime(-1));
    }

    //kthByArrival
    @Test
    @DisplayName("kthByArrival, kth with invalid argument")
    public void kthByArrivalTest() {
        PatientRegistry registry = createPatientRegistry();
        registry.put(100, new Patient(1, "P1", 1, "Condition1"));
        registry.put(200, new Patient(2, "P2", 1, "Condition2"));
        registry.put(300, new Patient(3, "P3", 1, "Condition3"));
        registry.put(400, new Patient(4, "P4", 1, "Condition4"));
        registry.put(500, new Patient(5, "P5", 1, "Condition5"));

        //kth by arrival
        assertEquals(1, registry.kthByArrival(1).getPatientId());
        assertEquals(3, registry.kthByArrival(3).getPatientId());
        assertEquals(5, registry.kthByArrival(5).getPatientId());

        // k<1
        assertThrows(IllegalArgumentException.class, () -> registry.kthByArrival(-100));

        //k>size
        assertNull(registry.kthByArrival(100));
    }

    //clear
    @Test
    @DisplayName("clear empties the tree")
    public void clearEmpties() {
        PatientRegistry registry = createPatientRegistry();
        registry.put(100, new Patient(1, "P1", 1, "Condition1"));
        registry.put(200, new Patient(2, "P2", 1, "Condition2"));
        registry.put(300, new Patient(3, "P3", 1, "Condition3"));
        registry.put(400, new Patient(4, "P4", 1, "Condition4"));
        registry.put(500, new Patient(5, "P5", 1, "Condition5"));
        registry.clear();
        assertTrue(registry.isEmpty());
    }

    //iterator
    @Test
    @DisplayName("iterator yields patients in ascending arrival-time order")
    public void iteratorTest() {
        PatientRegistry registry = createPatientRegistry();
        registry.put(100, new Patient(1, "P1", 1, "Condition1"));
        registry.put(200, new Patient(2, "P2", 1, "Condition2"));
        registry.put(300, new Patient(3, "P3", 1, "Condition3"));
        registry.put(400, new Patient(4, "P4", 1, "Condition4"));
        registry.put(500, new Patient(5, "P5", 1, "Condition5"));

        List<Integer> iterOrder = new ArrayList<>();
        for (Patient p : registry) {
            iterOrder.add(p.getPatientId());
        }
        assertEquals(List.of(1, 2, 3, 4, 5), iterOrder);
    }

    @Test
    @DisplayName("iterator for empty registry has no next")
    public void iteratorEmpty() {
        assertFalse(registry.iterator().hasNext());
    }

}
