package Tree;

public class Tree {
    
    public Node root;
    
    public Tree(int n) {
        // TODO: Is this error message in the right place?
        if(n < 2) {
            System.out.println("---ERROR---");
            System.out.println("invalid tree limit given. Maximum tree limit must be greater than 1.\n");
            return;
        }

        // TODO: should keys and bucketpointers length be n or n+1?
        this.root = new LeafNode(n, new Integer[n], new Pair[n], null); 
        this.root.minSize = 0;
        this.root.currentSize = 0;
        this.root.isRoot = true;
    }

    public void insert(LeafNode node, int key) {
        // TODO
        if (node.currentSize == 0 && node.isRoot) {
            Pair res = storagemanager.insert((null, Pair(0,0)), key);
            node.insert(0, key, res);
            // update pointers
        }
        else {
            if (!node.isFull()) {
                int idx = compareKeys(node.keys, key, node.currentSize);
                if (idx == -1) {
                    System.out.println("---ERROR---");
                    System.out.println("Search key value " + key + " already exists .\n");
                    return;
                }
                Pair res = storagemanager.insert((node.keys[idx], node.bucketPointers[idx]), key);
                node.insert(idx, key, res);
                // update pointers
            }
            else { // node is full
                // check root, convert to internal if needed
                // TODO: shift up
            }
        }
    }

    public void insert(InternalNode node, int key) {
        int idx = compareKeys(node.keys, key, node.currentSize);
        if (idx == -1) {
            System.out.println("---ERROR---");
            System.out.println("Search key value " + key + " already exists .\n");
            return;
        }
        Node n = node.nodePointers[idx];
        if (n instanceof LeafNode ) {
            insert((LeafNode) n, key);
        }
        else {
            insert((InternalNode) n, key);
        }
    }

    public int compareKeys(Integer[] keys, int key, int currentSize) {
        if (key < keys[0]){
            return 0;
       }
       for (int i = 0; i <= currentSize-2; i++) {
            if (key == keys[i] || key == keys[i + 1]) {
                return -1;
            }
            if (key > keys[i] && key < keys[i + 1]){
                return i+1;
            }
        }
        return currentSize;
    }

    public void delete(Tree tree, int key) {
        //TODO:  change node type if needed, add bucketpointers
        // merge/shiftup if needed
    }

    public void update(int key, Pair bucket) {

    }

    public void shiftUp() {

    }

    public void search(){

    }

    public void merge() {
        
    }

}
