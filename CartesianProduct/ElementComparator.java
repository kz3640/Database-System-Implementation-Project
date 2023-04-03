package CartesianProduct;

import java.util.Comparator;

public class ElementComparator implements Comparator<String[]> {
    private int indexToCompare;

    public ElementComparator(int indexToCompare) {
        this.indexToCompare = indexToCompare;
    }

    public int compare(String[] a, String[] b) {
        // Cast the Strings to their expected types for comparison
        Comparable objA = (Comparable) a[indexToCompare];
        Comparable objB = (Comparable) b[indexToCompare];
        return objA.compareTo(objB);
    }
}