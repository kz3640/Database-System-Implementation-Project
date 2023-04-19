package Tree;

import java.util.Arrays;

public class LeafNode extends Node {
    LeafNode left;
    LeafNode right;
    Pair[] bucketPointers;

    // public LeafNode(int n, Integer[] keys, Pair[] bucketPointers) {
    //     this.maxSize = n - 1;
    //     this.minSize = (int) Math.ceil((n-1)/2.0);
    //     this.currentSize = 0;
    //     this.keys = keys;
    //     this.bucketPointers = bucketPointers;

    //     this.isLeaf = true;
    // }

    public LeafNode(int n, Integer[] keys, Pair[] bucketPointers, InternalNode parent) {
        this.maxSize = n - 1;
        this.minSize = (int) Math.ceil((n-1)/2.0);
        this.currentSize = firstNullIndex(bucketPointers);
        this.keys = keys;
        this.bucketPointers = bucketPointers;
        this.parent = parent;

        this.isLeaf = true;
    }

    private int firstNullIndex(Pair[] bucketPointers) {
        for (int i = 0; i < bucketPointers.length; i++) {
            if (bucketPointers[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public boolean isFull() {
        return this.currentSize == maxSize;
    }

    private boolean isDeficient() {
        return this.currentSize < minSize;
    }

    public void insert(int idx, int key, Pair bucket) {
        for (int i = this.currentSize -1; i >= idx; i--) {
            this.bucketPointers[i + 1] = this.bucketPointers[i];
            this.keys[i + 1] = this.keys[i];
        }
        this.bucketPointers[idx] = bucket;
        this.keys[idx] = key;
        this.currentSize++;
    }

    //   public void delete(int index) {
    //     this.bucketPointers[index] = null;
    //     currentSize--;
    //   }
}