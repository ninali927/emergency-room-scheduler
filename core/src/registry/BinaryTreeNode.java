package registry;

/**
 * Interface for any binary tree node.
 *
 * In a binary tree, all nodes can have left and right child nodes.
 * This interface enables generic tree traversal and printing utilities.
 *
 */
public interface BinaryTreeNode {
    /**
     * Gets left child node.
     *
     * @return The left child node (or null if there is no left child).
     */
    BinaryTreeNode getLeftChild();

    /**
     * Gets right child node.
     *
     * @return The right child node (or null if there is no right child).
     */
    BinaryTreeNode getRightChild();
}
