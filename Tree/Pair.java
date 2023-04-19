package Tree;

public class Pair implements Comparable<Pair> {
    int pageNumber;
    int index;

public Pair (int pageNumber, int index) {
    this.pageNumber = pageNumber;
    this.index = index;
}

    public int compareTo (Pair o) {
        if (pageNumber == o.pageNumber) {
            return 0;
        }
        else if (pageNumber > o.pageNumber) {
            return 1;
        }
        else {
            return -1;
        }
    }
}