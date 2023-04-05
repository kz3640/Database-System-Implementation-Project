package OrderBy;
import Catalog.Table;
import Record.Record;
import Record.RecordAttribute;


import java.util.ArrayList;
import java.util.Comparator;

public class OrderBy {

    public static ArrayList<OrderHandler> makeOrderHandler(ArrayList<String> in) {
        return null;
    }
    public Table orderBy(Table in, ArrayList<OrderHandler> indexes) {
        ArrayList<Record> records = new ArrayList<>(in.getRecords()); // create a copy of the input records
        records.sort(new RecordComparator(indexes)); // sort the records using the given indexes
        return new Table(records, in.getTableName(), in.getSchema()); // create and return a new table with the sorted records
    }

    //Schema.getattribute(i).getattributename(i)
    private class RecordComparator implements Comparator<Record> {
        private final ArrayList<Integer> indexes;
        private final ArrayList<Integer> asc;

        public RecordComparator(ArrayList<OrderHandler> indexes) {
            ArrayList<Integer> orderIndexes = new ArrayList<>();
            for (OrderHandler i : indexes) {
                orderIndexes.add(i.getIndex());
            }
            this.indexes = orderIndexes;
            
            ArrayList<Integer> orderAsc = new ArrayList<>();
            for (OrderHandler i : indexes) {
                if (i.asc())
                    orderAsc.add(1);
                else 
                    orderAsc.add(-1);
            }
            this.asc = orderAsc;
        }

        @Override
        public int compare(Record r1, Record r2) {
            for (int i = 0; i != indexes.size(); i++) {
                RecordAttribute attr1 = r1.getData().get(indexes.get(i));
                RecordAttribute attr2 = r2.getData().get(indexes.get(i));

                if (attr1.getType() == int.class) {
                    int val1 = (Integer) attr1.getAttribute();
                    int val2 = (Integer) attr2.getAttribute();
                    int cmp = Integer.compare(val1, val2);
                    if (cmp != 0) {
                        return cmp * asc.get(i);
                    }
                } else if (attr1.getType() == boolean.class) {
                    boolean val1 = (Boolean) attr1.getAttribute();
                    boolean val2 = (Boolean) attr2.getAttribute();
                    int cmp = Boolean.compare(val1, val2);
                    if (cmp != 0) {
                        return cmp * asc.get(i);
                    }
                } else if (attr1.getType() == Character.class) {
                    String val1 = (String) attr1.getAttribute();
                    String val2 = (String) attr2.getAttribute();
                    int cmp = val1.compareTo(val2);
                    if (cmp != 0) {
                        return cmp * asc.get(i);
                    }
                } else if (attr1.getType() == String.class) {
                    String val1 = (String) attr1.getAttribute();
                    String val2 = (String) attr2.getAttribute();
                    int cmp = val1.compareTo(val2);
                    if (cmp != 0) {
                        return cmp * asc.get(i);
                    }
                } else if (attr1.getType() == double.class) {
                    double val1 = (Double) attr1.getAttribute();
                    double val2 = (Double) attr2.getAttribute();
                    int cmp = Double.compare(val1, val2);
                    if (cmp != 0) {
                        return cmp * asc.get(i);
                    }
                }
            }
            return 0;
        }
    }
    private class OrderHandler {
        private final int index;
        private final boolean asc;

        public OrderHandler(int i, boolean j) {
            this.index = i;
            this.asc = j;
        }

        public int getIndex() {
            return index;
        }

        public boolean asc() {
            return asc;
        }
    }
}
