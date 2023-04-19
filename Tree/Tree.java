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
        //TODO:  change node type if children added, add nodepointers
    }



    public void delete(Tree tree, int key) {

    }

    public void update(int key, Pair bucket) {

    }

    public void split() {

    }

    public void merge() {
        //TODO:  change node type if needed, add bucketpointers
    }

}
