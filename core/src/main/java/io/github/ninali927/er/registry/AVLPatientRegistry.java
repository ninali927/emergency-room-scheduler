package io.github.ninali927.er.registry;

import io.github.ninali927.er.models.Patient;
import io.github.ninali927.er.util.BinaryTreePrinter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * AVL Tree implementation of the PatientRegistry interface.
 *
 * This class maintains a mapping from arrival timestamps (keys) to Patient objects
 * (values) in a balanced binary search tree (AVL tree). The AVL tree guarantees
 * O(log n) time complexity for insertion, deletion, and search operations by
 * maintaining a balance condition: the heights of the left and right subtrees of
 * any node differ by at most 1.
 */
public class AVLPatientRegistry implements PatientRegistry {
    protected Node root;
    protected int size;

    // Inner class representing a node in the AVL tree.
    // Each node stores a key-value pair: arrival time (key) and Patient (value).
    // NOTE: Made protected to allow subclasses (like SnapshotAVLTree) to access.
    protected static class Node implements BinaryTreeNode {
        public int arrivalTime; // Key
        public Patient patient; // Value
        public Node left;
        public Node right;
        // Augmented fields: subtree height (for balancing) and subtree size (for rank/select).
        public int height;
        public int size;

        public Node(int arrivalTime, Patient patient) {
            this.arrivalTime = arrivalTime;
            this.patient = patient;
            this.left = null;
            this.right = null;
            this.height = 0;
            this.size = 1;
        }

        @Override
        public BinaryTreeNode getLeftChild() {
            return left;
        }

        @Override
        public BinaryTreeNode getRightChild() {
            return right;
        }

        @Override
        public String toString() {
            return arrivalTime + ":" + patient.getPatientId();
        }
    }

    /**
     * Constructs an empty AVL Patient Registry.
     */
    public AVLPatientRegistry() {
        this.root = null;
        this.size = 0;
    }

    @Override
    public void put(int arrivalTime, Patient patient) {
        checkInvalidArrivalTime(arrivalTime);
        checkInvalidPatient(patient);
        root = putHelper(root, arrivalTime, patient);
        this.size = getSize(root);
    }

    private Node putHelper(Node node, int arrivalTime, Patient patient) {
        if (node == null) {
            return new Node(arrivalTime, patient);
        }

        if (arrivalTime < node.arrivalTime) {
            node.left = putHelper(node.left, arrivalTime, patient);
        } else if (arrivalTime > node.arrivalTime) {
            node.right = putHelper(node.right, arrivalTime, patient);
        } else {
            node.patient = patient;
            return node;
        }

        updateHeight(node);
        updateSize(node);
        int bf = balanceFactor(node);
        return rebalance(node, bf);
    }

    @Override
    public Patient get(int arrivalTime) {
        checkInvalidArrivalTime(arrivalTime);
        return getHelper(root, arrivalTime);
    }

    private Patient getHelper(Node node, int arrivalTime) {
        if (node == null) {
            return null;
        }

        if (arrivalTime == node.arrivalTime) {
            return node.patient;
        } else if (arrivalTime < node.arrivalTime) {
            return getHelper(node.left, arrivalTime);
        } else {
            return getHelper(node.right, arrivalTime);
        }
    }

    @Override
    public Patient remove(int arrivalTime) {
        checkInvalidArrivalTime(arrivalTime);
        Patient removed[] = new Patient[1];
        root = removeHelper(root, arrivalTime, removed);
        if (removed[0] != null) {
            size--;
        }
        return removed[0];
    }

    private Node removeHelper(Node node, int arrivalTime, Patient[] removed) {
        if (node == null) {
            return null;
        }

        if (arrivalTime < node.arrivalTime) {
            node.left = removeHelper(node.left, arrivalTime, removed);
        } else if (arrivalTime > node.arrivalTime) {
            node.right = removeHelper(node.right, arrivalTime, removed);
        } else {
            removed[0] = node.patient;

            //left
            if (node.left == null && node.right == null) {
                return null;
            }
            // one child
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }

            Node successor = findMinNode(node.right);
            node.arrivalTime = successor.arrivalTime;
            node.patient = successor.patient;
            node.right = removeHelper(node.right, successor.arrivalTime, new Patient[1]);
        }
        updateSize(node);
        updateHeight(node);
        int bf = balanceFactor(node);
        return rebalance(node, bf);
    }


    @Override
    public boolean containsKey(int arrivalTime) {
        checkInvalidArrivalTime(arrivalTime);
        return get(arrivalTime) != null;
    }

    @Override
    public List<Patient> rangeSearch(int startTime, int endTime) {
        List<Patient> result = new ArrayList<>();
        if (startTime < 0 || endTime < 0) {
            throw new IllegalArgumentException("Times cannot be negative");
        }
        if (startTime > endTime) {
            throw new IllegalArgumentException("Start time is later than end time");
        }
        rangeSearchHelper(root, startTime, endTime, result);
        return result;
    }

    private void rangeSearchHelper(Node node, int startTime, int endTime, List<Patient> result) {
        if (node == null) {
            return;
        }

        if (node.arrivalTime < startTime) {
            rangeSearchHelper(node.right, startTime, endTime, result);
            return;
        }
        if (node.arrivalTime > endTime) {
            rangeSearchHelper(node.left, startTime, endTime, result);
            return;
        }
        rangeSearchHelper(node.left, startTime, endTime, result);
        result.add(node.patient);
        rangeSearchHelper(node.right, startTime, endTime, result);
    }

    @Override
    public Patient findMin() {
        if (root == null) {
            return null;
        }
        return findMinHelper(root);
    }

    private Patient findMinHelper(Node node) {
        if (node.left == null) {
            return node.patient;
        }
        return findMinHelper(node.left);
    }


    @Override
    public Patient findMax() {
        if (root == null) {
            return null;
        }
        return findMaxHelper(root);
    }

    private Patient findMaxHelper(Node node) {
        if (node.right == null) {
            return node.patient;
        }
        return findMaxHelper(node.right);
    }

    @Override
    public int countPatientsBeforeTime(int arrivalTime) {
        checkInvalidArrivalTime(arrivalTime);
        return countPatientsBeforeTime(root, arrivalTime);
    }

    private int countPatientsBeforeTime(Node node, int arrivalTime) {
        if (node == null) {
            return 0;
        }

        if (arrivalTime <= node.arrivalTime) {
            return countPatientsBeforeTime(node.left, arrivalTime);
        } else {
            return 1 + getSize(node.left) + countPatientsBeforeTime(node.right, arrivalTime);
        }
    }

    @Override
    public Patient kthByArrival(int k) {
        if (k < 1) {
            throw new IllegalArgumentException("k must be greater than 0");
        }
        if (k > size) {
            return null;
        }
        return kthByArrivalHelper(root, k);
    }

    private Patient kthByArrivalHelper(Node node, int k) {
        if (node == null) {
            return null;
        }

        int sizeLeft = getSize(node.left);

        if (sizeLeft + 1 == k) {
            return node.patient;
        } else if (sizeLeft < k) {
            return kthByArrivalHelper(node.right, k - sizeLeft - 1);
        } else {
            return kthByArrivalHelper(node.left, k);
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public Iterator<Patient> iterator() {
        return new AVLIterator();
    }

    // Inner class implementing Iterator for in-order traversal.
    private class AVLIterator implements Iterator<Patient> {
        private Stack<Node> stack;

        public AVLIterator() {
            stack = new java.util.Stack<>();
            pushLeftPath(root);
        }

        private void pushLeftPath(Node node) {
            while (node != null) {
                stack.push(node);
                node = node.left;
            }
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public Patient next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more elements");
            }
            Node current = stack.pop();
            if (current.right != null) {
                pushLeftPath(current.right);
            }
            return current.patient;
        }
    }

    @Override
    public String toString() {
        return BinaryTreePrinter.printBinaryTree(root);
    }

    // ------------ Helper methods --------------------

    // Performs a right rotation around the given node.
    protected Node rotateRight(Node y) {
        Node z = y.left;
        Node B = z.right;

        z.right = y;
        y.left = B;

        updateHeight(y);
        updateSize(y);
        updateHeight(z);
        updateSize(z);

        return z;
    }

    // Performs a left rotation around the given node.
    protected Node rotateLeft(Node x) {
        Node y = x.right;
        Node B = y.left;

        y.left = x;
        x.right = B;

        updateHeight(x);
        updateSize(x);
        updateHeight(y);
        updateSize(y);

        return y;
    }

    private int getHeight(Node node) {
        return (node == null) ? -1 : node.height;
    }

    private void updateHeight(Node node) {
        int leftHeight = getHeight(node.left);
        int rightHeight = getHeight(node.right);
        node.height = 1 + Math.max(leftHeight, rightHeight);
    }

    private int getSize(Node node) {
        return (node == null) ? 0 : node.size;
    }

    private void updateSize(Node node) {
        int leftSize = getSize(node.left);
        int rightSize = getSize(node.right);
        node.size = 1 + leftSize + rightSize;
    }

    private int balanceFactor(Node node) {
        if (node == null) {
            return 0;
        }
        return getHeight(node.left) - getHeight(node.right);
    }


    private Node rebalance(Node node, int bf) {
        if (bf > 1) {
            int leftBF = balanceFactor(node.left);
            if (leftBF < 0) {
                node.left = rotateLeft(node.left);
            }
            return rotateRight(node);
        }

        if (bf < -1) {
            int rightBF = balanceFactor(node.right);
            if (rightBF > 0) {
                node.right = rotateRight(node.right);
            }
            return rotateLeft(node);
        }
        return node;
    }

    private Node findMinNode(Node node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }



    private void checkInvalidArrivalTime(int arrivalTime) {
        if (arrivalTime < 0) {
            throw new IllegalArgumentException("Invalid arrival time.");
        }
    }

    private void checkInvalidPatient(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Invalid patient.");
        }
    }
}
