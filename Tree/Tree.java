package Tree;

public class Tree {
    
    public Node root;
    
    public Tree(int n) {
        // TODO: should keys length be n or n+1?

        this.root = new LeafNode(n, new Integer[n], null); 
        this.root.isRoot = true;
    }

    public void insert() {
        //TODO:  change node type if children added, add nodepointers
    }



    public void delete() {

    }

    public void update() {

    }

    public void split() {

    }

    public void merge() {
        //TODO:  change node type if needed, add bucketpointers
    }

}
