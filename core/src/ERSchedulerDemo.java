import models.Patient;
import system.EmergencyRoom;
import java.util.List;

/**
 * Demonstration program for the Emergency Room Scheduler system.
 *
 * This class showcases the functionality of the ER system including:
 * - Patient registration
 * - Treatment prioritization based on severity
 * - Dynamic severity updates (adaptive priority queue)
 * - Time-based range queries (AVL tree range search)
 */
public class ERSchedulerDemo {

    public static void main(String[] args) {
        System.out.println("=== Emergency Room Scheduler Demo ===\n");

        EmergencyRoom er = new EmergencyRoom();

        // Scenario: Patients arrive throughout the day
        System.out.println("--- Patient Arrivals ---");

        Patient p1 = new Patient(1, "Alice Johnson", 2, "Minor cut");
        Patient p2 = new Patient(2, "Bob Smith", 5, "Chest pain");
        Patient p3 = new Patient(3, "Carol White", 3, "Broken arm");
        Patient p4 = new Patient(4, "David Brown", 1, "Cold symptoms");
        Patient p5 = new Patient(5, "Eve Davis", 4, "Severe headache");

        er.registerPatient(100, p1);
        System.out.println("Registered at time 100: " + p1);

        er.registerPatient(105, p2);
        System.out.println("Registered at time 105: " + p2);

        er.registerPatient(110, p3);
        System.out.println("Registered at time 110: " + p3);

        er.registerPatient(115, p4);
        System.out.println("Registered at time 115: " + p4);

        er.registerPatient(120, p5);
        System.out.println("Registered at time 120: " + p5);

        System.out.println("\n" + er);

        // Demonstrate priority queue functionality
        System.out.println("\n--- Treatment Priority Queue ---");
        System.out.println("Next patient to treat: " + er.peekNextPatient());

        // Demonstrate adaptive priority update
        System.out.println("\n--- Severity Update (Adaptive Priority Queue) ---");
        System.out.println("Alice's condition worsens! Updating severity from 2 to 5");
        er.updatePatientSeverity(1, 5);
        System.out.println("Next patient to treat: " + er.peekNextPatient());

        // Demonstrate range search
        System.out.println("\n--- Range Search (AVL Tree) ---");
        System.out.println("Finding all patients who arrived between time 105 and 115:");
        List<Patient> patientsInRange = er.getPatientsInTimeRange(105, 115);
        for (Patient p : patientsInRange) {
            System.out.println("  " + p);
        }

        // Treat patients in priority order
        System.out.println("\n--- Treating Patients (Priority Order) ---");
        int count = 1;
        while (!er.isEmpty()) {
            Patient treated = er.treatNextPatient();
            System.out.println(count + ". Treated: " + treated);
            count++;
        }

        System.out.println("\n" + er);
        System.out.println("\nAll patients have been treated!");

        // Demonstrate more complex scenario
        demonstrateComplexScenario();
    }

    /**
     * Demonstrates a more complex ER scenario with multiple priority updates.
     */
    private static void demonstrateComplexScenario() {
        System.out.println("\n\n=== Complex Scenario Demo ===\n");

        EmergencyRoom er = new EmergencyRoom();

        System.out.println("--- Busy Friday Night in the ER ---");

        // Simulate multiple patients arriving
        // Note: arrival times are tracked separately from Patient objects
        int[] arrivalTimes = {200, 205, 210, 215, 220, 225, 230, 235};
        Patient[] patients = {
            new Patient(101, "John Doe", 3, "Sprained ankle"),
            new Patient(102, "Jane Roe", 2, "Migraine"),
            new Patient(103, "Mike Lee", 4, "Deep laceration"),
            new Patient(104, "Sarah Kim", 1, "Flu symptoms"),
            new Patient(105, "Tom Chen", 5, "Heart attack"),
            new Patient(106, "Lisa Park", 3, "Allergic reaction"),
            new Patient(107, "Kevin Wu", 2, "Food poisoning"),
            new Patient(108, "Amy Liu", 4, "Severe burns")
        };

        for (int i = 0; i < patients.length; i++) {
            er.registerPatient(arrivalTimes[i], patients[i]);
        }

        System.out.println("Registered " + patients.length + " patients");
        System.out.println(er);

        // Demonstrate range query
        System.out.println("\n--- Patients who arrived between 210 and 230 ---");
        List<Patient> range = er.getPatientsInTimeRange(210, 230);
        for (Patient p : range) {
            System.out.println("  " + p);
        }

        // Simulate dynamic severity changes
        System.out.println("\n--- Dynamic Severity Updates ---");
        System.out.println("Jane Roe's migraine worsens (severity 2 → 5)");
        er.updatePatientSeverity(102, 5);

        System.out.println("Mike Lee's condition stabilizes (severity 4 → 2)");
        er.updatePatientSeverity(103, 2);

        // Treat first few patients
        System.out.println("\n--- Treating First 3 Patients ---");
        for (int i = 0; i < 3; i++) {
            Patient treated = er.treatNextPatient();
            System.out.println((i + 1) + ". " + treated);
        }

        System.out.println("\nRemaining patients: " + er.getWaitingCount());

        // Show all remaining patients by arrival time
        System.out.println("\n--- Remaining Patients (by arrival time) ---");
        List<Patient> remaining = er.getAllPatientsByArrivalTime();
        for (Patient p : remaining) {
            System.out.println("  " + p);
        }

        System.out.println("\n--- ER Statistics ---");
        System.out.println("Total patients registered: " + er.getTotalPatientsRegistered());
        System.out.println("Patients waiting: " + er.getWaitingCount());

        // Demonstrate new augmented operations
        demonstrateAugmentedOperations();
    }

    /**
     * Demonstrates the new augmented AVL and priority queue operations.
     */
    private static void demonstrateAugmentedOperations() {
        System.out.println("\n\n=== Augmented Operations Demo ===\n");

        EmergencyRoom er = new EmergencyRoom();

        System.out.println("--- Registering Patients ---");
        er.registerPatient(100, new Patient(201, "Alex Chen", 3, "Fracture"));
        er.registerPatient(105, new Patient(202, "Maria Garcia", 5, "Heart attack"));
        er.registerPatient(110, new Patient(203, "James Wilson", 2, "Minor burn"));
        er.registerPatient(115, new Patient(204, "Sarah Taylor", 4, "Severe bleeding"));
        er.registerPatient(120, new Patient(205, "Michael Brown", 1, "Flu"));
        er.registerPatient(125, new Patient(206, "Emma Johnson", 4, "Asthma attack"));
        er.registerPatient(130, new Patient(207, "David Lee", 3, "Sprained ankle"));
        er.registerPatient(135, new Patient(208, "Sophia Martinez", 5, "Stroke"));

        System.out.println("Registered 8 patients\n");

        // Demonstrate rank query
        System.out.println("--- Rank Query (AVL Tree) ---");
        System.out.println("How many patients arrived before time 118?");
        int rank = er.rankByArrivalTime(118);
        System.out.println("Answer: " + rank + " patients");

        System.out.println("\nHow many patients arrived before time 130?");
        rank = er.rankByArrivalTime(130);
        System.out.println("Answer: " + rank + " patients\n");

        // Demonstrate k-th by arrival
        System.out.println("--- K-th Patient by Arrival Time (AVL Tree) ---");
        System.out.println("Who was the 3rd patient to arrive?");
        Patient third = er.getKthByArrivalTime(3);
        System.out.println("Answer: " + third);

        System.out.println("\nWho was the 5th patient to arrive?");
        Patient fifth = er.getKthByArrivalTime(5);
        System.out.println("Answer: " + fifth + "\n");

        // Demonstrate k-th most severe
        System.out.println("--- K-th Most Severe Patient (Priority Queue) ---");
        System.out.println("Who is the most severe patient (k=1)?");
        Patient mostSevere = er.getKthMostSeverePatient(1);
        System.out.println("Answer: " + mostSevere);

        System.out.println("\nWho is the 2nd most severe patient?");
        Patient secondMostSevere = er.getKthMostSeverePatient(2);
        System.out.println("Answer: " + secondMostSevere);

        System.out.println("\nWho is the 4th most severe patient?");
        Patient fourthMostSevere = er.getKthMostSeverePatient(4);
        System.out.println("Answer: " + fourthMostSevere + "\n");

        // Demonstrate arbitrary removal from queue
        System.out.println("--- Arbitrary Removal from Queue ---");
        System.out.println("Patient 203 (James Wilson) decides to leave AMA (Against Medical Advice)");
        Patient removed = er.removePatientFromQueue(203);
        System.out.println("Removed from queue: " + removed);

        System.out.println("\nWho is now the 4th most severe patient (after removal)?");
        Patient newFourth = er.getKthMostSeverePatient(4);
        System.out.println("Answer: " + newFourth);

        System.out.println("\n--- Final Treatment Queue ---");
        System.out.println("Treating remaining patients by severity:");
        int count = 1;
        while (!er.isEmpty()) {
            Patient treated = er.treatNextPatient();
            System.out.println(count + ". " + treated);
            count++;
        }

        System.out.println("\n=== Demo Complete ===");
    }
}
