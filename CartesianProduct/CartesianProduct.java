package CartesianProduct;

import java.util.*;


public class CartesianProduct {
    public static ArrayList<ArrayList<String>> cartesianProduct(ArrayList<String[]> arrays) {
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        cartesianProductHelper(arrays, 0, new ArrayList<>(), result);
        return result;
    }

    private static void cartesianProductHelper(ArrayList<String[]> arrays, int currentIndex, ArrayList<String> currentProduct, ArrayList<ArrayList<String>> result) {
        if (currentIndex == arrays.size()) {
            result.add(new ArrayList<>(currentProduct));
            return;
        }

        for (String element : arrays.get(currentIndex)) {
            currentProduct.add(element);
            cartesianProductHelper(arrays, currentIndex + 1, currentProduct, result);
            currentProduct.remove(currentProduct.size() - 1);
        }
    }
    public static void sort2DArray(String[][] arr, Comparator<String[]> sortingFunction) {
        Arrays.sort(arr, sortingFunction);
    }

    public static String[][] toAry(ArrayList<ArrayList<String>> in) {
        return in.stream().map(u -> u.toArray(new String[0])).toArray(String[][]::new);
    }

}
