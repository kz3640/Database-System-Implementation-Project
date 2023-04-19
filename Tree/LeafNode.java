package Tree;

import java.util.Arrays;

public class LeafNode extends Node {
    LeafNode left;
    LeafNode right;
    Pair[] bucketPointers;

    public LeafNode(int n, Integer[] keys, Pair bucket) {
        this.maxSize = n - 1;
        this.minSize = (int) Math.ceil((n-1)/2.0);
        this.currentSize = 0;
        this.keys = keys;
        this.bucketPointers = new Pair[n];
        this.insert(bucket);

        this.isLeaf = true;
    }

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

    private boolean isFull() {
        return this.currentSize == maxSize;
    }

    private boolean isDeficient() {
        return this.currentSize < minSize;
    }

    // NOT SURE IF RIGHT LOGIC
    public boolean insert(Pair bucket) {
        if (this.isFull()) {
          return false;
        } else {
          this.bucketPointers[currentSize] = bucket;
          currentSize++;
          Arrays.sort(this.bucketPointers, 0, currentSize);
  
          return true;
        }
      }

    // NOT SURE IF RIGHT LOGIC
      public void delete(int index) {
        this.bucketPointers[index] = null;
        currentSize--;
      }
}