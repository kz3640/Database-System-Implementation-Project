package Tree;

public class Tree {
    
    public Node root;
    
    public Tree(int n) {
        // TODO: should keys and bucketpointers length be n or n+1?

        this.root = new LeafNode(n, new Integer[n], new Pair[n], null); 
        this.root.minSize = 0;
        this.root.currentSize = 0;
        this.root.isRoot = true;
    }

    public void insert(Tree tree, int key) {
        // TODO
        // if (isRoot)
            // if empty, add keys and buckets
        // else if isLeaf
            // else if not full
                // search(keys) { update(buckets)}to add in the right index,
            // else full, convert to internal(max size of root, keys, pointers change) and add leafnode/s and pointers)

        // else (internal)
            //search(keys) and insert(nodepointers) to add in the right idx
        // if already exists, ERROR
        // found idx, call SM to insert- gives back bucket, shiftup if needed,  update() all the pointers in that node, 
    }

    public void delete(Tree tree, int key) {
        //TODO:  change node type if needed, add bucketpointers
        // merge/shiftup if needed
    }

    public void update(int key, Pair bucket) {

    }

    public void shiftUp() {

    }

    public void merge() {
        
    }

}
