package app;

import models.Patient;
import visualization.ISnapshot;
import visualization.SnapshotAVLTree;

import java.util.HashMap;
import java.util.Map;

/**
 * Extension of SnapshotAVLTree that adds JSON serialization capabilities.
 * This class is part of the web application backend and provides methods
 * to convert snapshot tree data into Maps for JSON serialization.
 *
 * This keeps the core SnapshotAVLTree class clean and focused
 * on AVL tree operations while separating web-specific concerns.
 */
public class SerializableSnapshotTree extends SnapshotAVLTree {

    /**
     * Convert a Snapshot to a Map for JSON serialization.
     * This method provides controlled access to internal Node structure.
     */
    public Map<String, Object> snapshotToMap(ISnapshot snapshot, int index) {
        if (snapshot == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("index", index);
        map.put("operation", snapshot.getOperation().toString());
        map.put("description", snapshot.getDescription());

        Node root = (Node) snapshot.getRoot();
        map.put("isEmpty", root == null);

        // Calculate tree properties from root
        if (root != null) {
            map.put("treeSize", root.size);
            map.put("treeHeight", root.height);
            map.put("tree", nodeToMap(root));
        } else {
            map.put("treeSize", 0);
            map.put("treeHeight", 0);
            map.put("tree", null);
        }

        return map;
    }

    /**
     * Convert a Node to a Map for JSON serialization (recursive).
     * This method provides controlled access to internal Node structure.
     */
    private Map<String, Object> nodeToMap(Node node) {
        if (node == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("arrivalTime", node.arrivalTime);
        map.put("patient", patientToMap(node.patient));
        map.put("height", node.height);
        map.put("size", node.size);

        // Calculate balance factor
        Node left = node.left;
        Node right = node.right;
        int leftHeight = left != null ? left.height : -1;
        int rightHeight = right != null ? right.height : -1;
        map.put("balanceFactor", leftHeight - rightHeight);
        map.put("isLeaf", left == null && right == null);

        // Recursively convert children
        map.put("left", nodeToMap(left));
        map.put("right", nodeToMap(right));

        return map;
    }

    /**
     * Convert a Patient to a Map for JSON serialization.
     */
    private Map<String, Object> patientToMap(Patient p) {
        if (p == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getPatientId());
        map.put("name", p.getName());
        map.put("severity", p.getSeverity());
        map.put("condition", p.getChiefComplaint());
        return map;
    }
}
