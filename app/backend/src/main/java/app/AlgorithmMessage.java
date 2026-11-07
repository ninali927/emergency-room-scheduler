package app;

import java.util.Map;

/**
 * Standard message format for WebSocket communication between frontend and backend.
 * This provides a consistent structure for all ER system visualization updates.
 */
public class AlgorithmMessage {
    /**
     * Message type (e.g., "register_patient", "treat_patient", "update_severity", "data_update")
     */
    public String type;

    /**
     * Human-readable message content
     */
    public String message;

    /**
     * Additional data payload (operation-specific)
     * Examples:
     * - Patient data for registration
     * - Query results for range/rank/kth operations
     * - Current ER state
     */
    public Map<String, Object> data;

    /**
     * Timestamp for message ordering
     */
    public long timestamp;

    public AlgorithmMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public AlgorithmMessage(String type, String message) {
        this();
        this.type = type;
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("AlgorithmMessage{type='%s', message='%s', data=%s}",
                type, message, data);
    }
}
