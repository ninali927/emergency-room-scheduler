package visualization;

import registry.BinaryTreeNode;

/**
 * Defines the public contract for a tree snapshot.
 */
public interface ISnapshot {

    /**
     * Gets a human-readable description of the snapshot.
     *
     * @return A string description of the snapshot.
     */
    String getDescription();

    /**
     * Gets the operation that produced this snapshot.
     *
     * @return The snapshot operation.
     */
    SnapshotOperation getOperation();

    /**
     * Gets the root node of the binary tree representing this snapshot.
     *
     * @return The root binary tree node.
     */
    BinaryTreeNode getRoot();
}
