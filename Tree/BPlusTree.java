package Tree;

import java.util.*;

import Buffer.Page;
import Catalog.Schema;
import Record.Record;

public class BPlusTree {
    int m;
    InternalNode root;
    LeafNode firstLeaf;
    String type;

    public String getType() {
        return type;
    }

    public class NodeInfo {
        public PageInfo pageInfo;
        public Object key;

        public NodeInfo(Object key, PageInfo pageInfo) {
            this.pageInfo = pageInfo;
            this.key = key;
        }

        public Object getKey() {
            return key;
        }

        public PageInfo getPageInfo() {
            return pageInfo;
        }
    }

    public ArrayList<NodeInfo> getAllLeafs() {
        ArrayList<NodeInfo> leafs = new ArrayList<>();

        if (this.root == null) {
            LeafNode n = this.firstLeaf;
            for (DictionaryPair dp : n.dictionary) {
                if (dp != null)
                    leafs.add(new NodeInfo(dp.key, dp.value));
            }
            return leafs;
        }

        Node node = this.root;

        while (true) {
            if (node instanceof LeafNode) {
                break;
            }
            InternalNode in = (InternalNode) node;
            node = in.childPointers[0];
        }

        LeafNode ln = (LeafNode) node;

        for (DictionaryPair dp : ln.dictionary) {
            if (dp != null)
                leafs.add(new NodeInfo(dp.key, dp.value));
        }

        while (ln.rightSibling != null) {
            ln = ln.rightSibling;
            for (DictionaryPair dp : ln.dictionary) {
                if (dp != null)
                    leafs.add(new NodeInfo(dp.key, dp.value));
            }
        }

        return leafs;
    }

    // Binary search program
    private int binarySearch(DictionaryPair[] dps, int numPairs, Object t) {
        Comparator<DictionaryPair> c = new Comparator<DictionaryPair>() {
            @Override
            public int compare(DictionaryPair o1, DictionaryPair o2) {

                if (o1.type.equals("integer")) {
                    Integer a = Integer.valueOf((Integer) o1.key);
                    Integer b = Integer.valueOf((Integer) o2.key);
                    return a.compareTo(b);
                }

                return 0;
            }
        };
        return Arrays.binarySearch(dps, 0, numPairs, new DictionaryPair(t, new PageInfo(0, 0), this.type), c);
    }

    public void updateAllPagesPastAndIncluding(int pageIndex, Page page, Schema schema) {

        ArrayList<Object> keysToSkip = new ArrayList<>();
        int indOfPRim = schema.getIndexOfPrimaryKey();
        for (Record record : page.getRecords()) {
            keysToSkip.add(record.getData().get(indOfPRim).getAttribute());
        }

        if (this.root == null) {
            LeafNode n = this.firstLeaf;
            for (DictionaryPair dp : n.dictionary) {
                if (dp != null) {
                    if (dp.value.pageIndex >= pageIndex) {
                        if (!keysToSkip.contains(dp.key))
                            dp.value.pageIndex++;
                    }
                }
            }
            return;
        }

        Node node = this.root;

        while (true) {
            if (node instanceof LeafNode) {
                break;
            }
            InternalNode in = (InternalNode) node;
            node = in.childPointers[0];
        }

        LeafNode ln = (LeafNode) node;

        for (DictionaryPair dp : ln.dictionary) {
            if (dp != null){
                if (dp.value.pageIndex >= pageIndex) {
                    if (!keysToSkip.contains(dp.key)) 
                        dp.value.pageIndex++;
                }
            }
        }

        while (ln.rightSibling != null) {
            ln = ln.rightSibling;
            for (DictionaryPair dp : ln.dictionary) {
                if (dp != null) {
                    if (dp.value.pageIndex >= pageIndex) {
                        if (!keysToSkip.contains(dp.key))
                            dp.value.pageIndex++;
                    }
                }
            }
        }

        return;
    }

    public void printAllLeafNodes() {

        if (this.root == null) {
            LeafNode n = this.firstLeaf;
            for (DictionaryPair dp : n.dictionary) {
                if (dp != null)
                    System.out.println(dp.value.pageIndex);
            }
            return;
        }

        Node node = this.root;

        while (true) {
            if (node instanceof LeafNode) {
                break;
            }
            InternalNode in = (InternalNode) node;
            node = in.childPointers[0];
        }

        LeafNode ln = (LeafNode) node;

        for (DictionaryPair dp : ln.dictionary) {
            if (dp != null)
                System.out.println(dp.value.pageIndex);
        }

        while (ln.rightSibling != null) {
            ln = ln.rightSibling;
            for (DictionaryPair dp : ln.dictionary) {
                if (dp != null)
                    System.out.println(dp.value.pageIndex);
            }
        }

        return;
    }

    public void updatePageInfo(Object key, PageInfo pi) {
        search(key).pageIndex = pi.pageIndex;
        search(key).positionIndex = pi.positionIndex;
    }

    // Find the leaf node
    private LeafNode findLeafNode(Object key) {

        if (this.type.equals("integer")) {

            Object[] objectKeys = this.root.keys;
            Integer[] keys = new Integer[objectKeys.length];
            for (int i = 0; i < objectKeys.length; i++) {
                keys[i] = (Integer) objectKeys[i];
            }
            int i;

            for (i = 0; i < this.root.degree - 1; i++) {
                if ((Integer) key < keys[i]) {
                    break;
                }
            }

            Node child = this.root.childPointers[i];
            if (child instanceof LeafNode) {
                return (LeafNode) child;
            } else {
                return findLeafNode((InternalNode) child, key);
            }
        }

        // else if (this.type.equals("string")) {
        // String[] keys = (String[]) this.root.keys;
        // int i;

        // for (i = 0; i < this.root.degree - 1; i++) {
        // if (((String) key).compareTo(keys[i]) < 0) {
        // break;
        // }
        // }

        // Node child = this.root.childPointers[i];
        // if (child instanceof LeafNode) {
        // return (LeafNode) child;
        // } else {
        // return findLeafNode((InternalNode) child, key);
        // }
        // }

        return null;
    }

    // Find the leaf node
    private LeafNode findLeafNode(InternalNode node, Object key) {
        if (this.type.equals("integer")) {

            Integer[] keys = new Integer[node.keys.length];
            for (int i = 0; i < node.keys.length; i++) {
                keys[i] = (Integer) node.keys[i];
            }

            int i;

            for (i = 0; i < node.degree - 1; i++) {
                if ((Integer) key < keys[i]) {
                    break;
                }
            }

            Node childNode = node.childPointers[i];
            if (childNode instanceof LeafNode) {
                return (LeafNode) childNode;
            } else {
                return findLeafNode((InternalNode) node.childPointers[i], key);
            }
        }

        // add other types

        return null;
    }

    // Finding the index of the pointer
    private int findIndexOfPointer(Node[] pointers, LeafNode node) {
        int i;
        for (i = 0; i < pointers.length; i++) {
            if (pointers[i] == node) {
                break;
            }
        }
        return i;
    }

    // Get the mid point
    private int getMidpoint() {
        return (int) Math.ceil((this.m + 1) / 2.0) - 1;
    }

    // Balance the tree
    private void handleDeficiency(InternalNode in) {

        InternalNode sibling;
        InternalNode parent = in.parent;

        if (this.root == in) {
            for (int i = 0; i < in.childPointers.length; i++) {
                if (in.childPointers[i] != null) {
                    if (in.childPointers[i] instanceof InternalNode) {
                        this.root = (InternalNode) in.childPointers[i];
                        this.root.parent = null;
                    } else if (in.childPointers[i] instanceof LeafNode) {
                        this.root = null;
                    }
                }
            }
        }

        else if (in.leftSibling != null && in.leftSibling.isLendable()) {
            sibling = in.leftSibling;
        } else if (in.rightSibling != null && in.rightSibling.isLendable()) {
            sibling = in.rightSibling;

            Object borrowedKey = sibling.keys[0];
            Node pointer = sibling.childPointers[0];

            in.keys[in.degree - 1] = parent.keys[0];
            in.childPointers[in.degree] = pointer;

            parent.keys[0] = borrowedKey;

            sibling.removePointer(0);
            Arrays.sort(sibling.keys);
            sibling.removePointer(0);
            shiftDown(in.childPointers, 1);
        } else if (in.leftSibling != null && in.leftSibling.isMergeable()) {

        } else if (in.rightSibling != null && in.rightSibling.isMergeable()) {
            sibling = in.rightSibling;
            sibling.keys[sibling.degree - 1] = parent.keys[parent.degree - 2];
            Arrays.sort(sibling.keys, 0, sibling.degree);
            parent.keys[parent.degree - 2] = null;

            for (int i = 0; i < in.childPointers.length; i++) {
                if (in.childPointers[i] != null) {
                    sibling.prependChildPointer(in.childPointers[i]);
                    in.childPointers[i].parent = sibling;
                    in.removePointer(i);
                }
            }

            parent.removePointer(in);

            sibling.leftSibling = in.leftSibling;
        }

        if (parent != null && parent.isDeficient()) {
            handleDeficiency(parent);
        }
    }

    private boolean isEmpty() {
        return firstLeaf == null;
    }

    private int linearNullSearch(DictionaryPair[] dps) {
        for (int i = 0; i < dps.length; i++) {
            if (dps[i] == null) {
                return i;
            }
        }
        return -1;
    }

    private int linearNullSearch(Node[] pointers) {
        for (int i = 0; i < pointers.length; i++) {
            if (pointers[i] == null) {
                return i;
            }
        }
        return -1;
    }

    private void shiftDown(Node[] pointers, int amount) {
        Node[] newPointers = new Node[this.m + 1];
        for (int i = amount; i < pointers.length; i++) {
            newPointers[i - amount] = pointers[i];
        }
        pointers = newPointers;
    }

    private void sortDictionary(DictionaryPair[] dictionary) {
        Arrays.sort(dictionary, new Comparator<DictionaryPair>() {
            @Override
            public int compare(DictionaryPair o1, DictionaryPair o2) {
                if (o1 == null && o2 == null) {
                    return 0;
                }
                if (o1 == null) {
                    return 1;
                }
                if (o2 == null) {
                    return -1;
                }
                return o1.compareTo(o2);
            }
        });
    }

    private Node[] splitChildPointers(InternalNode in, int split) {

        Node[] pointers = in.childPointers;
        Node[] halfPointers = new Node[this.m + 1];

        for (int i = split + 1; i < pointers.length; i++) {
            halfPointers[i - split - 1] = pointers[i];
            in.removePointer(i);
        }

        return halfPointers;
    }

    private DictionaryPair[] splitDictionary(LeafNode ln, int split) {

        DictionaryPair[] dictionary = ln.dictionary;

        DictionaryPair[] halfDict = new DictionaryPair[this.m];

        for (int i = split; i < dictionary.length; i++) {
            halfDict[i - split] = dictionary[i];
            ln.delete(i);
        }

        return halfDict;
    }

    private void splitInternalNode(InternalNode in) {

        InternalNode parent = in.parent;

        int midpoint = getMidpoint();
        Object newParentKey = in.keys[midpoint];
        Object[] halfKeys = splitKeys(in.keys, midpoint);
        Node[] halfPointers = splitChildPointers(in, midpoint);

        in.degree = linearNullSearch(in.childPointers);

        InternalNode sibling = new InternalNode(this.m, halfKeys, halfPointers);
        for (Node pointer : halfPointers) {
            if (pointer != null) {
                pointer.parent = sibling;
            }
        }

        sibling.rightSibling = in.rightSibling;
        if (sibling.rightSibling != null) {
            sibling.rightSibling.leftSibling = sibling;
        }
        in.rightSibling = sibling;
        sibling.leftSibling = in;

        if (parent == null) {

            Object[] keys = new Object[this.m];
            keys[0] = newParentKey;
            InternalNode newRoot = new InternalNode(this.m, keys);
            newRoot.appendChildPointer(in);
            newRoot.appendChildPointer(sibling);
            this.root = newRoot;

            in.parent = newRoot;
            sibling.parent = newRoot;

        } else {

            parent.keys[parent.degree - 1] = newParentKey;
            Arrays.sort(parent.keys, 0, parent.degree);

            int pointerIndex = parent.findIndexOfPointer(in) + 1;
            parent.insertChildPointer(sibling, pointerIndex);
            sibling.parent = parent;
        }
    }

    private Object[] splitKeys(Object[] keys, int split) {

        Object[] halfKeys = new Integer[this.m];

        keys[split] = null;

        for (int i = split + 1; i < keys.length; i++) {
            halfKeys[i - split - 1] = keys[i];
            keys[i] = null;
        }

        return halfKeys;
    }

    public void insert(Object key, PageInfo pageInfo) {
        if (isEmpty()) {

            LeafNode ln = new LeafNode(this.m, new DictionaryPair(key, pageInfo, this.type), null);

            this.firstLeaf = ln;

        } else {
            LeafNode ln = (this.root == null) ? this.firstLeaf : findLeafNode(key);

            if (!ln.insert(new DictionaryPair(key, pageInfo, this.type))) {

                ln.dictionary[ln.numPairs] = new DictionaryPair(key, pageInfo, this.type);
                ln.numPairs++;
                sortDictionary(ln.dictionary);

                int midpoint = getMidpoint();
                DictionaryPair[] halfDict = splitDictionary(ln, midpoint);

                if (ln.parent == null) {

                    Object[] parent_keys = new Object[this.m];
                    parent_keys[0] = halfDict[0].key;
                    InternalNode parent = new InternalNode(this.m, parent_keys);
                    ln.parent = parent;
                    parent.appendChildPointer(ln);

                } else {
                    Object newParentKey = halfDict[0].key;
                    ln.parent.keys[ln.parent.degree - 1] = newParentKey;
                    Arrays.sort(ln.parent.keys, 0, ln.parent.degree);
                }

                LeafNode newLeafNode = new LeafNode(this.m, halfDict, ln.parent);

                int pointerIndex = ln.parent.findIndexOfPointer(ln) + 1;
                ln.parent.insertChildPointer(newLeafNode, pointerIndex);

                newLeafNode.rightSibling = ln.rightSibling;
                if (newLeafNode.rightSibling != null) {
                    newLeafNode.rightSibling.leftSibling = newLeafNode;
                }
                ln.rightSibling = newLeafNode;
                newLeafNode.leftSibling = ln;

                if (this.root == null) {

                    this.root = ln.parent;

                } else {
                    InternalNode in = ln.parent;
                    while (in != null) {
                        if (in.isOverfull()) {
                            splitInternalNode(in);
                        } else {
                            break;
                        }
                        in = in.parent;
                    }
                }
            }
        }
    }

    public void delete(Object key) {
        if (isEmpty()) {
            // the tree is empty, so there's nothing to delete
            return;
        }

        // find the leaf node containing the key
        LeafNode leafNode = findLeafNode(key);

        // check if the key exists in the leaf node
        int index = leafNode.indexOf(key);
        if (index == -1) {
            // the key doesn't exist in the tree, so there's nothing to delete
            return;
        }

        // delete the key from the leaf node
        leafNode.delete(index);

        // check if the leaf node is underfull
        if (leafNode.isDeficient()) {
            // try to redistribute keys with a sibling leaf node
            if (leafNode.redistribute()) {
                return;
            }

            // try to merge with a sibling leaf node
            LeafNode sibling = leafNode.findSibling();
            if (sibling != null && sibling.isMergeable()) {
                leafNode.merge(sibling);
                if (this.root.isDeficient()) {
                    // if the root node is underfull after the merge, remove the root node
                    this.root = (InternalNode) this.root.getChildren()[0];
                }
                return;
            }
        }
    }

    public PageInfo search(Object key) {

        if (isEmpty()) {
            return null;
        }

        LeafNode ln = (this.root == null) ? this.firstLeaf : findLeafNode(key);

        DictionaryPair[] dps = ln.dictionary;
        int index = binarySearch(dps, ln.numPairs, key);

        if (index < 0) {
            return null;
        } else {
            return dps[index].value;
        }
    }

    public PageInfo getPositionToInsert(Object key) {

        if (isEmpty()) {
            return null;
        }

        LeafNode ln = (this.root == null) ? this.firstLeaf : findLeafNode(key);

        DictionaryPair[] dps = ln.dictionary;
        int index = binarySearch(dps, ln.numPairs, key);

        if (index < 0) {
            return null;
        } else if (index < 1) {
            return new PageInfo(0, 0);
        } else {
            return dps[index - 1].value;
        }
    }

    public ArrayList<PageInfo> search(Object lowerBound, Object upperBound) {

        ArrayList<PageInfo> values = new ArrayList<PageInfo>();

        LeafNode currNode = this.firstLeaf;
        while (currNode != null) {

            DictionaryPair dps[] = currNode.dictionary;
            for (DictionaryPair dp : dps) {

                if (dp == null) {
                    break;
                }

                if (this.type.equals("integer")) {
                    if ((Integer) lowerBound <= (Integer) dp.key && (Integer) dp.key <= (Integer) upperBound) {
                        values.add(dp.value);
                    }
                }
                // add
                // if (lowerBound <= dp.key && dp.key <= upperBound) {
                // values.add(dp.value);
                // }
            }
            currNode = currNode.rightSibling;

        }

        return values;
    }

    public BPlusTree(int m, String type) {
        this.m = m;
        this.root = null;
        this.type = type;
    }

    public class Node {
        public Node leftSibling;
        public Node rightSibling;
        InternalNode parent;

        public InternalNode getParent() {
            return parent;
        }
    }

    private class InternalNode extends Node {
        int maxDegree;
        int minDegree;
        int degree;
        InternalNode leftSibling;
        InternalNode rightSibling;
        Object[] keys;
        Node[] childPointers;

        private Node[] getChildren() {
            return childPointers;
        }

        private void appendChildPointer(Node pointer) {
            this.childPointers[degree] = pointer;
            this.degree++;
        }

        private int findIndexOfPointer(Node pointer) {
            for (int i = 0; i < childPointers.length; i++) {
                if (childPointers[i] == pointer) {
                    return i;
                }
            }
            return -1;
        }

        private void insertChildPointer(Node pointer, int index) {
            for (int i = degree - 1; i >= index; i--) {
                childPointers[i + 1] = childPointers[i];
            }
            this.childPointers[index] = pointer;
            this.degree++;
        }

        private boolean isDeficient() {
            return this.degree < this.minDegree;
        }

        private boolean isLendable() {
            return this.degree > this.minDegree;
        }

        private boolean isMergeable() {
            return this.degree == this.minDegree;
        }

        private boolean isOverfull() {
            return this.degree == maxDegree + 1;
        }

        private void prependChildPointer(Node pointer) {
            for (int i = degree - 1; i >= 0; i--) {
                childPointers[i + 1] = childPointers[i];
            }
            this.childPointers[0] = pointer;
            this.degree++;
        }

        private void removeKey(int index) {
            this.keys[index] = null;
        }

        private void removePointer(int index) {
            this.childPointers[index] = null;
            this.degree--;
        }

        private void removePointer(Node pointer) {
            for (int i = 0; i < childPointers.length; i++) {
                if (childPointers[i] == pointer) {
                    this.childPointers[i] = null;
                }
            }
            this.degree--;
        }

        private InternalNode(int m, Object[] keys) {
            this.maxDegree = m;
            this.minDegree = (int) Math.ceil(m / 2.0);
            this.degree = 0;
            this.keys = keys;
            this.childPointers = new Node[this.maxDegree + 1];
        }

        private InternalNode(int m, Object[] keys, Node[] pointers) {
            this.maxDegree = m;
            this.minDegree = (int) Math.ceil(m / 2.0);
            this.degree = linearNullSearch(pointers);
            this.keys = keys;
            this.childPointers = pointers;
        }

        public int indexOfChild(Node leafNode) {
            for (int i = 0; i < childPointers.length; i++) {
                if (childPointers[i].equals(leafNode)) {
                    return i;
                }
            }
            return -1;
        }

        public void setKey(int index, DictionaryPair dictionaryPair) {
            this.keys[index] = dictionaryPair;
        }
    }

    public class PageInfo {
        public int pageIndex;
        public int positionIndex;

        public PageInfo(int pageIndex, int positionIndex) {
            this.pageIndex = pageIndex;
            this.positionIndex = positionIndex;
        }
    }

    public class LeafNode extends Node {
        int maxNumPairs;
        int minNumPairs;
        int numPairs;
        LeafNode leftSibling;
        LeafNode rightSibling;
        DictionaryPair[] dictionary;

        public void merge(LeafNode sibling) {
            // add all keys from the sibling node to this node
            for (int i = 0; i < sibling.dictionary.length; i++) {
                insert(sibling.dictionary[i]);
            }

            // update sibling pointers
            if (sibling.rightSibling != null) {
                sibling.rightSibling.leftSibling = this;
            }
            this.rightSibling = sibling.rightSibling;

            // remove sibling node from parent
            int index = getParent().findIndexOfPointer(sibling);
            getParent().removePointer(index);
        }

        public DictionaryPair[] getKeys() {
            return dictionary;
        }

        public LeafNode findSibling() {
            if (this.parent == null) {
                // the root node has no siblings
                return null;
            }

            InternalNode parentNode = this.parent;
            int index = parentNode.indexOfChild(this);

            // check left sibling
            if (this.leftSibling != null) {
                if (this.leftSibling.getKeys().length > this.minNumPairs) {
                    return this.leftSibling;
                }
            }

            // check right sibling
            if (this.rightSibling != null) {
                if (this.rightSibling.getKeys().length > this.minNumPairs) {
                    return this.rightSibling;
                }
            }

            // no suitable sibling found
            return null;
        }

        public void delete(int index) {
            this.dictionary[index] = null;
            numPairs--;
        }

        public boolean insert(DictionaryPair dp) {
            if (this.isFull()) {
                return false;
            } else {
                this.dictionary[numPairs] = dp;
                numPairs++;
                Arrays.sort(this.dictionary, 0, numPairs);

                return true;
            }
        }

        public boolean isDeficient() {
            return numPairs < minNumPairs;
        }

        public boolean isFull() {
            return numPairs == maxNumPairs;
        }

        public boolean isLendable() {
            return numPairs > minNumPairs;
        }

        public boolean isMergeable() {
            return numPairs == minNumPairs;
        }

        public LeafNode(int m, DictionaryPair dp, InternalNode parent) {
            this.maxNumPairs = m - 1;
            this.minNumPairs = (int) (Math.ceil(m / 2) - 1);
            this.dictionary = new DictionaryPair[m];
            this.numPairs = 0;
            this.insert(dp);
            this.parent = parent;
        }

        public LeafNode(int m, DictionaryPair[] dps, InternalNode parent) {
            this.maxNumPairs = m - 1;
            this.minNumPairs = (int) (Math.ceil(m / 2) - 1);
            this.dictionary = dps;
            this.numPairs = linearNullSearch(dps);
            this.parent = parent;
        }

        public int indexOf(Object key) {
            for (int i = 0; i < dictionary.length; i++) {
                if (key.equals(dictionary[i].key)) {
                    return i;
                }
            }
            return -1;
        }

        public boolean redistribute() {
            // Check if there are any siblings
            LeafNode sibling = findSibling();
            if (sibling == null) {
                // No siblings, redistribution is not possible
                return false;
            }

            // Find the index of the current node in the parent's children array
            int index = getParent().indexOfChild(this);

            // Determine which sibling has more keys
            LeafNode leftSibling, rightSibling;
            if (index > 0) {
                leftSibling = (LeafNode) getParent().getChildren()[index - 1];
                rightSibling = this;
            } else {
                leftSibling = this;
                rightSibling = (LeafNode) getParent().getChildren()[index + 1];
            }

            // Check if redistribution is possible
            if (leftSibling.getKeys().length > minNumPairs && rightSibling.getKeys().length > minNumPairs) {
                // Both siblings have enough keys, redistribute
                Object key = rightSibling.dictionary[0].key;
                PageInfo value = rightSibling.dictionary[0].value;
                String type = rightSibling.dictionary[0].type;
                rightSibling.delete(0);
                leftSibling.insert(new DictionaryPair(key, value, type));
                getParent().setKey(index, rightSibling.dictionary[0]);
                return true;
            } else if (leftSibling.getKeys().length > minNumPairs) {
                // Left sibling has enough keys, redistribute from left to right
                Object key = leftSibling.dictionary[leftSibling.getKeys().length - 1].key;
                PageInfo value = leftSibling.dictionary[leftSibling.getKeys().length - 1].value;
                String type = rightSibling.dictionary[leftSibling.getKeys().length - 1].type;
                leftSibling.delete(leftSibling.getKeys().length - 1);
                rightSibling.insert(new DictionaryPair(key, value, type));
                getParent().setKey(index, rightSibling.dictionary[0]);
                return true;
            } else if (rightSibling.getKeys().length > minNumPairs) {
                // Right sibling has enough keys, redistribute from right to left
                Object key = rightSibling.dictionary[0].key;
                PageInfo value = rightSibling.dictionary[0].value;
                String type = rightSibling.dictionary[0].type;
                rightSibling.delete(0);
                leftSibling.insert(new DictionaryPair(key, value, type));
                getParent().setKey(index, rightSibling.dictionary[0]);
                return true;
            } else {
                // Both siblings have the minimum number of keys, redistribution is not possible
                return false;
            }
        }
    }

    public class DictionaryPair implements Comparable<DictionaryPair> {
        Object key;
        PageInfo value;
        String type;

        public DictionaryPair(Object key, PageInfo value, String type) {
            this.key = key;
            this.value = value;
            this.type = type;
        }

        public int compareTo(DictionaryPair o) {

            if (this.type.equals("integer")) {
                int i = (int) key;
                int ok = (int) o.key;

                if (i == ok) {
                    return 0;
                } else if (i > ok) {
                    return 1;
                } else {
                    return -1;
                }
            }
            return 0;

            // if (key == o.key) {
            // return 0;
            // } else if (key > o.key) {
            // return 1;
            // } else {
            // return -1;
            // }
        }

    }

    public static void main(String[] args) {
        BPlusTree bpt = null;
        bpt = new BPlusTree(3, "integer");

        boolean programRunning = true;
        Scanner scan = new Scanner(System.in);

        while (programRunning) {
            StringBuilder input = new StringBuilder();
            System.out.println("Enter command: ");
            String line = scan.nextLine();

            String[] splitLine = line.split(" ");

            if (splitLine[0].equals("i")) {
                PageInfo pi = bpt.new PageInfo(Integer.parseInt(splitLine[2]), Integer.parseInt(splitLine[2]));
                bpt.insert(Integer.parseInt(splitLine[1]), pi);
            } else {
                System.out.println(bpt.search(Integer.parseInt(splitLine[1])).pageIndex);
            }
        }
    }
}