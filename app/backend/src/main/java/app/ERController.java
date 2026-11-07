package app;

import com.google.gson.Gson;
import io.javalin.websocket.WsContext;
import io.github.ninali927.er.models.Patient;
import io.github.ninali927.er.system.EmergencyRoom;

import java.util.*;

/**
 * Controller for Emergency Room operations.
 * Handles all patient registration, treatment, and query operations.
 */
public class ERController {
    private final EmergencyRoom er;
    private final Gson gson;
    private final Random random;
    private int nextPatientId;
    private int nextArrivalTime;

    // Patient name pools for random generation
    private static final String[] FIRST_NAMES = {
        "Alice", "Bob", "Carol", "David", "Eve", "Frank", "Grace", "Henry",
        "Ivy", "Jack", "Kate", "Leo", "Maria", "Noah", "Olivia", "Peter",
        "Quinn", "Rachel", "Sam", "Tina", "Uma", "Victor", "Wendy", "Xavier",
        "Yara", "Zach", "Emma", "Liam", "Sophia", "Mason", "Isabella", "James"
    };

    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
        "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez",
        "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin",
        "Lee", "Perez", "Thompson", "White", "Harris", "Sanchez", "Clark"
    };

    private static final String[] CONDITIONS = {
        "Minor cut", "Chest pain", "Broken arm", "Cold symptoms", "Severe headache",
        "Sprained ankle", "Migraine", "Deep laceration", "Flu symptoms", "Heart attack",
        "Allergic reaction", "Food poisoning", "Severe burns", "Fracture", "Minor burn",
        "Asthma attack", "Stroke", "Abdominal pain", "Concussion", "High fever",
        "Back pain", "Difficulty breathing", "Dehydration", "Nausea", "Dizziness"
    };

    public ERController() {
        this.er = new EmergencyRoom();
        this.gson = Application.getGson();
        this.random = new Random();
        this.nextPatientId = 1;
        this.nextArrivalTime = 100;
    }

    /**
     * Handle incoming messages from the frontend.
     */
    public void handleMessage(WsContext ctx, AlgorithmMessage message) {
        try {
            switch (message.type) {
                case "register_patient":
                    registerPatient(ctx, message);
                    break;
                case "register_random":
                    registerRandomPatient(ctx, message);
                    break;
                case "treat_patient":
                    treatPatient(ctx);
                    break;
                case "peek_patient":
                    peekPatient(ctx);
                    break;
                case "update_severity":
                    updateSeverity(ctx, message);
                    break;
                case "range_search":
                    rangeSearch(ctx, message);
                    break;
                case "rank_query":
                    rankQuery(ctx, message);
                    break;
                case "kth_by_arrival":
                    kthByArrival(ctx, message);
                    break;
                case "kth_most_severe":
                    kthMostSevere(ctx, message);
                    break;
                case "remove_from_queue":
                    removeFromQueue(ctx, message);
                    break;
                case "get_all_patients":
                    getAllPatients(ctx);
                    break;
                case "get_er_state":
                    sendERState(ctx);
                    break;
                case "clear_all":
                    clearAll(ctx);
                    break;
                default:
                    ctx.send(gson.toJson(new AlgorithmMessage("error", "Unknown command: " + message.type)));
            }
        } catch (Exception e) {
            System.err.println("Error handling message: " + e.getMessage());
            e.printStackTrace();
            ctx.send(gson.toJson(new AlgorithmMessage("error", "Operation failed: " + e.getMessage())));
        }
    }

    /**
     * Register a patient manually provided from the frontend.
     */
    private void registerPatient(WsContext ctx, AlgorithmMessage message) {
        Map<String, Object> data = message.data;

        String name = (String) data.get("name");
        int severity = ((Number) data.get("severity")).intValue();
        String condition = (String) data.get("condition");

        // Use provided arrival time or auto-increment
        int arrivalTime;
        if (data.containsKey("arrivalTime")) {
            arrivalTime = ((Number) data.get("arrivalTime")).intValue();
        } else {
            arrivalTime = nextArrivalTime;
            nextArrivalTime += 5;
        }

        Patient patient = new Patient(nextPatientId++, name, severity, condition);
        er.registerPatient(arrivalTime, patient);

        AlgorithmMessage response = new AlgorithmMessage("patient_registered",
                "Registered: " + patient.getName());
        response.data = Map.of(
            "patient", patientToMap(patient),
            "arrivalTime", arrivalTime,
            "erState", getERStateMap()
        );
        ctx.send(gson.toJson(response));
    }

    /**
     * Register a randomly generated patient.
     */
    private void registerRandomPatient(WsContext ctx, AlgorithmMessage message) {
        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        String name = firstName + " " + lastName;

        int severity = random.nextInt(5) + 1; // 1-5
        String condition = CONDITIONS[random.nextInt(CONDITIONS.length)];

        int arrivalTime = nextArrivalTime;
        nextArrivalTime += 5;

        Patient patient = new Patient(nextPatientId++, name, severity, condition);
        er.registerPatient(arrivalTime, patient);

        AlgorithmMessage response = new AlgorithmMessage("patient_registered",
                "Registered: " + patient.getName() + " (random)");
        response.data = Map.of(
            "patient", patientToMap(patient),
            "arrivalTime", arrivalTime,
            "erState", getERStateMap()
        );
        ctx.send(gson.toJson(response));
    }

    /**
     * Treat the next patient (highest severity).
     */
    private void treatPatient(WsContext ctx) {
        Patient patient = er.treatNextPatient();

        if (patient == null) {
            AlgorithmMessage response = new AlgorithmMessage("no_patients", "No patients waiting");
            response.data = Map.of("erState", getERStateMap());
            ctx.send(gson.toJson(response));
            return;
        }

        AlgorithmMessage response = new AlgorithmMessage("patient_treated",
                "Treated: " + patient.getName());
        response.data = Map.of(
            "patient", patientToMap(patient),
            "erState", getERStateMap()
        );
        ctx.send(gson.toJson(response));
    }

    /**
     * Peek at the next patient without treating.
     */
    private void peekPatient(WsContext ctx) {
        Patient patient = er.peekNextPatient();

        if (patient == null) {
            AlgorithmMessage response = new AlgorithmMessage("no_patients", "No patients waiting");
            ctx.send(gson.toJson(response));
            return;
        }

        AlgorithmMessage response = new AlgorithmMessage("peek_result",
                "Next patient: " + patient.getName());
        response.data = Map.of("patient", patientToMap(patient));
        ctx.send(gson.toJson(response));
    }

    /**
     * Update a patient's severity.
     */
    private void updateSeverity(WsContext ctx, AlgorithmMessage message) {
        int patientId = ((Number) message.data.get("patientId")).intValue();
        int newSeverity = ((Number) message.data.get("severity")).intValue();

        boolean success = er.updatePatientSeverity(patientId, newSeverity);

        if (success) {
            AlgorithmMessage response = new AlgorithmMessage("severity_updated",
                    "Updated patient " + patientId + " to severity " + newSeverity);
            response.data = Map.of("erState", getERStateMap());
            ctx.send(gson.toJson(response));
        } else {
            AlgorithmMessage response = new AlgorithmMessage("error",
                    "Patient " + patientId + " not found in queue");
            ctx.send(gson.toJson(response));
        }
    }

    /**
     * Perform range search by arrival time.
     */
    private void rangeSearch(WsContext ctx, AlgorithmMessage message) {
        int startTime = ((Number) message.data.get("startTime")).intValue();
        int endTime = ((Number) message.data.get("endTime")).intValue();

        List<Patient> patients = er.getPatientsInTimeRange(startTime, endTime);

        List<Map<String, Object>> patientMaps = new ArrayList<>();
        for (Patient p : patients) {
            patientMaps.add(patientToMap(p));
        }

        AlgorithmMessage response = new AlgorithmMessage("range_search_result",
                "Found " + patients.size() + " patients in range [" + startTime + ", " + endTime + "]");
        response.data = Map.of(
            "patients", patientMaps,
            "count", patients.size(),
            "startTime", startTime,
            "endTime", endTime
        );
        ctx.send(gson.toJson(response));
    }

    /**
     * Perform rank query by arrival time.
     */
    private void rankQuery(WsContext ctx, AlgorithmMessage message) {
        int arrivalTime = ((Number) message.data.get("arrivalTime")).intValue();
        int rank = er.rankByArrivalTime(arrivalTime);

        AlgorithmMessage response = new AlgorithmMessage("rank_query_result",
                rank + " patients arrived before time " + arrivalTime);
        response.data = Map.of(
            "rank", rank,
            "arrivalTime", arrivalTime
        );
        ctx.send(gson.toJson(response));
    }

    /**
     * Get k-th patient by arrival time.
     */
    private void kthByArrival(WsContext ctx, AlgorithmMessage message) {
        int k = ((Number) message.data.get("k")).intValue();

        try {
            Patient patient = er.getKthByArrivalTime(k);

            if (patient == null) {
                AlgorithmMessage response = new AlgorithmMessage("error",
                        "No patient at position " + k);
                ctx.send(gson.toJson(response));
                return;
            }

            AlgorithmMessage response = new AlgorithmMessage("kth_arrival_result",
                    "Patient #" + k + " by arrival: " + patient.getName());
            response.data = Map.of(
                "patient", patientToMap(patient),
                "k", k
            );
            ctx.send(gson.toJson(response));
        } catch (IllegalArgumentException e) {
            AlgorithmMessage response = new AlgorithmMessage("error", e.getMessage());
            ctx.send(gson.toJson(response));
        }
    }

    /**
     * Get k-th most severe patient.
     */
    private void kthMostSevere(WsContext ctx, AlgorithmMessage message) {
        int k = ((Number) message.data.get("k")).intValue();

        try {
            Patient patient = er.getKthMostSeverePatient(k);

            if (patient == null) {
                AlgorithmMessage response = new AlgorithmMessage("error",
                        "No patient at position " + k);
                ctx.send(gson.toJson(response));
                return;
            }

            AlgorithmMessage response = new AlgorithmMessage("kth_severe_result",
                    "Patient #" + k + " by severity: " + patient.getName());
            response.data = Map.of(
                "patient", patientToMap(patient),
                "k", k
            );
            ctx.send(gson.toJson(response));
        } catch (IllegalArgumentException e) {
            AlgorithmMessage response = new AlgorithmMessage("error", e.getMessage());
            ctx.send(gson.toJson(response));
        }
    }

    /**
     * Remove a patient from the treatment queue.
     */
    private void removeFromQueue(WsContext ctx, AlgorithmMessage message) {
        int patientId = ((Number) message.data.get("patientId")).intValue();

        Patient removed = er.removePatientFromQueue(patientId);

        if (removed == null) {
            AlgorithmMessage response = new AlgorithmMessage("error",
                    "Patient " + patientId + " not found in queue");
            ctx.send(gson.toJson(response));
            return;
        }

        AlgorithmMessage response = new AlgorithmMessage("patient_removed",
                "Removed from queue: " + removed.getName());
        response.data = Map.of(
            "patient", patientToMap(removed),
            "erState", getERStateMap()
        );
        ctx.send(gson.toJson(response));
    }

    /**
     * Get all patients sorted by arrival time.
     */
    private void getAllPatients(WsContext ctx) {
        // Get all patients sorted by arrival time
        List<Patient> patients = er.getAllPatientsByArrivalTime();

        List<Map<String, Object>> patientMaps = new ArrayList<>();
        for (Patient p : patients) {
            patientMaps.add(patientToMap(p));
        }

        AlgorithmMessage response = new AlgorithmMessage("all_patients_result",
                "Retrieved " + patients.size() + " patients");
        response.data = Map.of(
            "patients", patientMaps,
            "count", patients.size()
        );
        ctx.send(gson.toJson(response));
    }

    /**
     * Clear all patients from the ER.
     */
    private void clearAll(WsContext ctx) {
        er.clearAll();
        nextPatientId = 1;
        nextArrivalTime = 100;

        AlgorithmMessage response = new AlgorithmMessage("er_cleared", "All patients cleared");
        response.data = Map.of("erState", getERStateMap());
        ctx.send(gson.toJson(response));
    }

    /**
     * Send current ER state to the frontend.
     */
    private void sendERState(WsContext ctx) {
        AlgorithmMessage response = new AlgorithmMessage("er_state", "Current ER state");
        response.data = getERStateMap();
        ctx.send(gson.toJson(response));
    }

    /**
     * Convert a Patient to a Map for JSON serialization.
     * Includes arrival time if available in the ER system.
     */
    private Map<String, Object> patientToMap(Patient p) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getPatientId());
        map.put("name", p.getName());
        map.put("severity", p.getSeverity());
        map.put("condition", p.getChiefComplaint());

        // Always include arrival time if available
        Integer arrivalTime = er.getPatientArrivalTime(p.getPatientId());
        if (arrivalTime != null) {
            map.put("arrivalTime", arrivalTime);
        }

        return map;
    }

    /**
     * Get current ER state as a Map.
     */
    private Map<String, Object> getERStateMap() {
        Map<String, Object> state = new HashMap<>();
        state.put("waitingCount", er.getWaitingCount());
        state.put("totalRegistered", er.getTotalPatientsRegistered());
        state.put("isEmpty", er.isEmpty());
        return state;
    }
}
