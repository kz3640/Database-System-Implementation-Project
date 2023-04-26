package Tree;

import Buffer.Page;
import Catalog.Schema;

import java.util.List;
import java.util.stream.Collectors;

import Tree.BPlusTree.LeafNode;
import Tree.BPlusTree.InternalNode;
import Tree.BPlusTree.Node;
import Tree.BPlusTree.DictionaryPair;

public class TreeUpdater {

    public static void updateNext(int pageIndex, Page page, Schema schema, BPlusTree tree) {
        List<Object> keysToSkip = page.getRecords().stream()
                .map(record -> record.getData().get(schema.getIndexOfPrimaryKey()).getAttribute())
                .collect(Collectors.toList());

        Node node = tree.getRoot();
        while (node instanceof InternalNode) {
            node = ((InternalNode) node).getChildPointers()[0];
        }

        LeafNode ln = (LeafNode) node;
        while (ln != null) {
            for (DictionaryPair dp : ln.getDictionary()) {
                if (dp != null && dp.getValue().getPageIndex() >= pageIndex && !keysToSkip.contains(dp.getKey())) {
                    dp.getValue().setPageIndex(dp.getValue().getPageIndex() + 1);
                }
            }
            ln = ln.getRightSibling();
        }
    }

    public void updateTree(BPlusTree tree, Schema schema) {
        // Start at the first leaf node
        LeafNode node = tree.getFirstLeaf();

        while (node != null) {
            // Loop through all the dictionary pairs in the leaf node
            for (DictionaryPair dp : node.getDictionary()) {
                if (dp != null) {
                    // Get the record's primary key attribute value
                    Object primaryKey = dp.getKey();

                    // Get the page index of the record
                    int pageIndex = dp.getValue().getPageIndex();

                    // Check if the page at the given index is still valid
                    if (!isPageValid(pageIndex)) {
                        // Delete the record from the tree
                        tree.delete(primaryKey);
                    } else {
                        // Update the page index of the record
                        dp.getValue().setPageIndex(pageIndex);
                    }
                }
            }

            // Move to the next leaf node
            node = node.getRightSibling();
        }
    }

    private boolean isPageValid(int pageIndex) {
        // Logic to check if the page at the given index is still valid
        // Return true if it is, false otherwise
        //TODO
        return false;
    }
}
