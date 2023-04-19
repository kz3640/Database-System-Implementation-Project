package Tree;

public class InternalNode extends Node {
    InternalNode left;
    InternalNode right;
    Node[] nodePointers;

    private InternalNode(int n, Integer[] keys) {
        this.maxSize = n;
        this.minSize = (int) Math.ceil(n/2.0);
        this.currentSize = 0;
        this.keys = keys;
        this.nodePointers = new Node[this.maxSize + 1];

        this.isLeaf = false;
    }

    private InternalNode(int n, Integer[] keys, Node[] nodePointers) {
        this.maxSize = n;
        this.minSize = (int) Math.ceil(n/2.0);
        this.currentSize = firstNullIndex(nodePointers);
        this.keys = keys;
        this.nodePointers = nodePointers;

        this.isLeaf = false;
    }

    private int firstNullIndex(Node[] nodePointers) {
        for (int i = 0; i < nodePointers.length; i++) {
            if (nodePointers[i] == null) {
                return i;
            }
        }
        return -1;
    }

    private boolean isFull() {
        return this.currentSize == maxSize;
    }

    private boolean isDeficient() {
        return this.currentSize < minSize;
    }

    // uhhhh NOT SURE IF RIGHT LOGIC
    private void removeNodePointer(Node pointer) {
        for (int i = 0; i < nodePointers.length; i++) {
            if (nodePointers[i] == pointer) {
                this.nodePointers[i] = null;
            }
        }
        this.currentSize--;
    }
    // NOT SURE IF RIGHT LOGIC
    private void removeNodePointer(int idx) {
        this.nodePointers[idx] = null;
        this.currentSize--;
    }
    // NOT SURE IF RIGHT LOGIC
    private void removeKey(int idx) {
        this.keys[idx] = null;
    }
    // NOT SURE IF RIGHT LOGIC
    private void insertNodePointer(Node pointer, int idx) {
        for (int i = this.currentSize -1; i >= idx; i--) {
            nodePointers[i + 1] = nodePointers[i];
        }
        this.nodePointers[idx] = pointer;
        this.currentSize--;
    }
}

