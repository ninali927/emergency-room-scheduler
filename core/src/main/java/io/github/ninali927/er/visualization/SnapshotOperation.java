package io.github.ninali927.er.visualization;

/**
 * Enumeration representing the types of operations that produce an AVL tree snapshot.
 *
 * Each operation type is used to track and visualize how the AVL tree structure changes
 * over time during insertions and deletions.
 */
public enum SnapshotOperation {
    /**
     * Initial state of the tree (empty or first snapshot).
     */
    INITIAL_STATE,

    /**
     * Patient insertion operation.
     */
    INSERT,

    /**
     * Patient removal operation.
     */
    REMOVE
}
