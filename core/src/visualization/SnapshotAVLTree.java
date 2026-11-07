package visualization;

import models.Patient;
import registry.AVLPatientRegistry;
import registry.BinaryTreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * AVL tree that records snapshots of its structure after each operation.
 * Simple implementation: just clones the root after each structural change.
 *
 * Known limitation: rotations are not recorded as separate snapshots. Rotations happen
 * during the recursive unwind of a put/remove, before the parent of the rotated subtree
 * has been updated, so cloning from {@code root} at that point would give an inconsistent
 * view. Recording them would require an iterative put/remove implementation.
 */
public class SnapshotAVLTree extends AVLPatientRegistry {

    // Internal snapshot implementation
    private static class Snapshot implements ISnapshot {
        private final Node root;
        public final String description;
        public final SnapshotOperation operation;

        /**
         * Constructs a new Snapshot.
         *
         * @param root the root node of the snapshot
         * @param description a human-readable description of the snapshot
         * @param operation the operation that produced this snapshot
         */
        public Snapshot(Node root, String description, SnapshotOperation operation) {
            this.root = root;
            this.description = description;
            this.operation = operation;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public SnapshotOperation getOperation() {
            return operation;
        }

        @Override
        public BinaryTreeNode getRoot() {
            return root;
        }
    }

    private final List<ISnapshot> history;

    /**
     * Constructs a new SnapshotAVLTree.
     */
    public SnapshotAVLTree() {
        super();
        this.history = new ArrayList<>();
        recordSnapshot("Initial empty tree", SnapshotOperation.INITIAL_STATE);
    }

    @Override
    public void put(int arrivalTime, Patient patient) {
        super.put(arrivalTime, patient);
        recordSnapshot("Insert patient at time " + arrivalTime, SnapshotOperation.INSERT);
    }

    @Override
    public Patient remove(int arrivalTime) {
        Patient removed = super.remove(arrivalTime);
        if (removed != null) {
            recordSnapshot("Remove patient at time " + arrivalTime, SnapshotOperation.REMOVE);
        }
        return removed;
    }

    @Override
    public void clear() {
        super.clear();
        history.clear();
        recordSnapshot("Cleared", SnapshotOperation.INITIAL_STATE);
    }

    /**
     * Record a snapshot by cloning the current tree.
     */
    private void recordSnapshot(String description, SnapshotOperation operation) {
        Node clonedRoot = cloneNode(root);
        history.add(new Snapshot(clonedRoot, description, operation));
    }

    /**
     * Deep clone a node and all its children.
     */
    private Node cloneNode(Node node) {
        if (node == null) {
            return null;
        }
        Node cloned = new Node(node.arrivalTime, node.patient);
        cloned.left = cloneNode(node.left);
        cloned.right = cloneNode(node.right);
        cloned.height = node.height;
        cloned.size = node.size;
        return cloned;
    }

    /**
     * Get all snapshots.
     */
    public List<ISnapshot> getHistory() {
        return new ArrayList<>(history);
    }

    /**
     * Get snapshot at index.
     */
    public ISnapshot getSnapshot(int index) {
        return history.get(index);
    }

    /**
     * Get number of snapshots.
     */
    public int getSnapshotCount() {
        return history.size();
    }

    /**
     * Get the most recent snapshot.
     */
    public ISnapshot getCurrentSnapshot() {
        if (history.isEmpty()) {
            return null;
        }
        return history.get(history.size() - 1);
    }

    /**
     * Reset snapshot history while keeping current tree.
     * Creates a new initial snapshot of the current tree state.
     */
    public void resetSnapshots() {
        history.clear();
        recordSnapshot("Reset to current state", SnapshotOperation.INITIAL_STATE);
    }
}
