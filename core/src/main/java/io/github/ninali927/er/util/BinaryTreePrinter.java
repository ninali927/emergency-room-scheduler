package io.github.ninali927.er.util;

import io.github.ninali927.er.registry.BinaryTreeNode;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Utility class that prints binary trees based on a level-order traversal.
 *
 * This provides an easy-to-understand view of the tree structure for testing
 * and debugging purposes. Each existing node is printed via its toString()
 * representation, and non-existing nodes are printed as 'null'.
 *
 * Format: Each node is printed as arrivalTime:patientId (e.g., "102:2" means arrival time 102, patient ID 2)
 */
public class BinaryTreePrinter {

    /**
     * Level-order print of binary tree.
     *
     * Each existing node is printed via its toString() representation.
     * Non-existing nodes are printed as 'null'.
     * There is a space between all nodes at the same level.
     * The levels of the binary tree are separated by new lines.
     * Returns empty string if the root is null.
     *
     * @param root top node of binary tree to start printing from
     * @return binary tree string serialization
     */
    public static String printBinaryTree(BinaryTreeNode root) {
        StringBuilder s = new StringBuilder();
        Queue<BinaryTreeNode> q = new LinkedList<>();

        q.add(root);
        boolean onlyNullChildrenAdded = root == null;
        while (!q.isEmpty() && !onlyNullChildrenAdded) {
            onlyNullChildrenAdded = true;

            int levelSize = q.size();
            while (levelSize-- > 0) {
                boolean nonNullChildAdded = handleNextNodeToString(q, s);
                if (nonNullChildAdded) {
                    onlyNullChildrenAdded = false;
                }
                s.append(" ");
            }

            s.deleteCharAt(s.length() - 1);
            s.append("\n");
        }

        return s.toString();
    }

    /**
     * Helper function for printBinaryTree() to build String for a single node
     * and add its children to the queue.
     *
     * @param q the queue for level-order traversal
     * @param s the StringBuilder to append to
     * @return true if a non-null child was added to the queue, false otherwise
     */
    private static boolean handleNextNodeToString(
            Queue<BinaryTreeNode> q,
            StringBuilder s) {

        BinaryTreeNode n = q.remove();
        if (n != null) {
            s.append(n);

            BinaryTreeNode left = n.getLeftChild();
            BinaryTreeNode right = n.getRightChild();
            q.add(left);
            q.add(right);

            return left != null || right != null;
        } else {
            s.append("null");

            q.add(null);
            q.add(null);

            return false;
        }
    }
}
