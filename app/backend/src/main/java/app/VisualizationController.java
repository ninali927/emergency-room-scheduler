package app;

import com.google.gson.Gson;
import io.javalin.websocket.WsContext;
import io.github.ninali927.er.models.Patient;
import io.github.ninali927.er.visualization.ISnapshot;

import java.util.*;

/**
 * Controller for AVL Tree visualization operations.
 * Handles WebSocket messages for tree snapshot visualization.
 */
public class VisualizationController {
    private final SerializableSnapshotTree snapshotTree;
    private final Gson gson;
    private int nextPatientId;
    private int nextArrivalTime;

    public VisualizationController() {
        this.snapshotTree = new SerializableSnapshotTree();
        this.gson = Application.getGson();
        this.nextPatientId = 1;
        this.nextArrivalTime = 100;
    }

    /**
     * Handle incoming messages from the frontend.
     */
    public void handleMessage(WsContext ctx, AlgorithmMessage message) {
        try {
            switch (message.type) {
                case "viz_insert":
                    insertPatient(ctx, message);
                    break;
                case "viz_remove":
                    removePatient(ctx, message);
                    break;
                case "viz_get_snapshots":
                    getAllSnapshots(ctx);
                    break;
                case "viz_get_snapshot":
                    getSnapshotAt(ctx, message);
                    break;
                case "viz_get_current":
                    getCurrentSnapshot(ctx);
                    break;
                case "viz_reset":
                    resetTree(ctx, message);
                    break;
                case "viz_clear":
                    clearTree(ctx);
                    break;
                default:
                    ctx.send(gson.toJson(new AlgorithmMessage("error", "Unknown visualization command: " + message.type)));
            }
        } catch (Exception e) {
            System.err.println("Error handling visualization message: " + e.getMessage());
            e.printStackTrace();
            ctx.send(gson.toJson(new AlgorithmMessage("error", "Operation failed: " + e.getMessage())));
        }
    }

    /**
     * Insert a patient into the snapshot tree.
     */
    private void insertPatient(WsContext ctx, AlgorithmMessage message) {
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
            nextArrivalTime += 10;
        }

        Patient patient = new Patient(nextPatientId++, name, severity, condition);
        snapshotTree.put(arrivalTime, patient);

        ISnapshot current = snapshotTree.getCurrentSnapshot();
        int currentIndex = snapshotTree.getSnapshotCount() - 1;
        AlgorithmMessage response = new AlgorithmMessage("viz_insert_success",
                "Inserted patient at time " + arrivalTime);
        response.data = Map.of(
            "arrivalTime", arrivalTime,
            "patient", Map.of(
                "id", patient.getPatientId(),
                "name", patient.getName(),
                "severity", patient.getSeverity(),
                "condition", patient.getChiefComplaint()
            ),
            "snapshotCount", snapshotTree.getSnapshotCount(),
            "currentSnapshot", snapshotTree.snapshotToMap(current, currentIndex)
        );
        ctx.send(gson.toJson(response));
    }

    /**
     * Remove a patient from the snapshot tree.
     */
    private void removePatient(WsContext ctx, AlgorithmMessage message) {
        int arrivalTime = ((Number) message.data.get("arrivalTime")).intValue();

        Patient removed = snapshotTree.remove(arrivalTime);

        if (removed == null) {
            AlgorithmMessage response = new AlgorithmMessage("viz_remove_failed",
                    "No patient found at time " + arrivalTime);
            ctx.send(gson.toJson(response));
            return;
        }

        ISnapshot current = snapshotTree.getCurrentSnapshot();
        int currentIndex = snapshotTree.getSnapshotCount() - 1;
        AlgorithmMessage response = new AlgorithmMessage("viz_remove_success",
                "Removed patient at time " + arrivalTime);
        response.data = Map.of(
            "arrivalTime", arrivalTime,
            "patient", Map.of(
                "id", removed.getPatientId(),
                "name", removed.getName(),
                "severity", removed.getSeverity(),
                "condition", removed.getChiefComplaint()
            ),
            "snapshotCount", snapshotTree.getSnapshotCount(),
            "currentSnapshot", snapshotTree.snapshotToMap(current, currentIndex)
        );
        ctx.send(gson.toJson(response));
    }

    /**
     * Get all snapshots from the tree history.
     */
    private void getAllSnapshots(WsContext ctx) {
        List<ISnapshot> snapshots = snapshotTree.getHistory();
        List<Map<String, Object>> snapshotMaps = new ArrayList<>();

        for (int i = 0; i < snapshots.size(); i++) {
            snapshotMaps.add(snapshotTree.snapshotToMap(snapshots.get(i), i));
        }

        AlgorithmMessage response = new AlgorithmMessage("viz_snapshots",
                "Retrieved " + snapshots.size() + " snapshots");
        response.data = Map.of(
            "snapshots", snapshotMaps,
            "count", snapshots.size()
        );
        ctx.send(gson.toJson(response));
    }

    /**
     * Get a specific snapshot by index.
     */
    private void getSnapshotAt(WsContext ctx, AlgorithmMessage message) {
        int index = ((Number) message.data.get("index")).intValue();

        try {
            ISnapshot snapshot = snapshotTree.getSnapshot(index);
            AlgorithmMessage response = new AlgorithmMessage("viz_snapshot",
                    "Retrieved snapshot #" + index);
            response.data = Map.of(
                "snapshot", snapshotTree.snapshotToMap(snapshot, index),
                "index", index
            );
            ctx.send(gson.toJson(response));
        } catch (IndexOutOfBoundsException e) {
            AlgorithmMessage response = new AlgorithmMessage("error",
                    "Snapshot index " + index + " out of bounds");
            ctx.send(gson.toJson(response));
        }
    }

    /**
     * Get the current (most recent) snapshot.
     */
    private void getCurrentSnapshot(WsContext ctx) {
        ISnapshot current = snapshotTree.getCurrentSnapshot();

        if (current == null) {
            AlgorithmMessage response = new AlgorithmMessage("error", "No snapshots available");
            ctx.send(gson.toJson(response));
            return;
        }

        int currentIndex = snapshotTree.getSnapshotCount() - 1;
        AlgorithmMessage response = new AlgorithmMessage("viz_current_snapshot",
                "Current tree state");
        response.data = Map.of(
            "snapshot", snapshotTree.snapshotToMap(current, currentIndex),
            "snapshotCount", snapshotTree.getSnapshotCount()
        );
        ctx.send(gson.toJson(response));
    }

    /**
     * Reset snapshot history while keeping current tree.
     */
    private void resetTree(WsContext ctx, AlgorithmMessage message) {
        snapshotTree.resetSnapshots();

        ISnapshot current = snapshotTree.getCurrentSnapshot();
        AlgorithmMessage response = new AlgorithmMessage("viz_reset_success",
                "Snapshot history reset");
        response.data = Map.of(
            "snapshotCount", snapshotTree.getSnapshotCount(),
            "currentSnapshot", snapshotTree.snapshotToMap(current, 0)
        );
        ctx.send(gson.toJson(response));
    }

    /**
     * Clear the tree completely.
     */
    private void clearTree(WsContext ctx) {
        snapshotTree.clear();
        nextPatientId = 1;
        nextArrivalTime = 100;

        AlgorithmMessage response = new AlgorithmMessage("viz_clear_success",
                "Tree cleared");
        response.data = Map.of(
            "snapshotCount", snapshotTree.getSnapshotCount()
        );
        ctx.send(gson.toJson(response));
    }
}
