package Tree;
import java.util.ArrayList;

public class TreeNode {
    
    private ArrayList<Integer> keys;
    private ArrayList<TreeNode> children;
    private TreeNode parent;
    private int maxSize;

    public TreeNode(TreeNode parentNode, int n) {
        this.keys = new ArrayList<Integer>();
        this.children = new ArrayList<TreeNode>();
        this.parent = parentNode;
        this.maxSize = n;
    }

    public void addChild(TreeNode child) {
        this.children.add(child);
    }

    public void removeChild(int index) {
        this.children.remove(index);
    }

    public TreeNode getChild(int searchKey) {
        for(int i = 0; i < this.keys.size(); i++) {
            if(searchKey < this.keys.get(i)) {
                return this.children.get(i);
            }
        }
        return this.children.get(this.children.size() - 1);
    }

    public TreeNode getParent() {
        return this.parent;
    }

    public int getMaxSize() {
        return this.maxSize;
    }

    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    public boolean isRoot() {
        return this.parent == null;
    }

}
