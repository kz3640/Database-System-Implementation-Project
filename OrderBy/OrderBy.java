package OrderBy;
import Catalog.Schema;
import Catalog.SchemaAttribute;
import Catalog.Table;
import Record.Record;
import Record.RecordAttribute;


import java.util.ArrayList;
import java.util.Comparator;

public class OrderBy {
    public static Table orderBy(Table in, ArrayList<String> attributes) {
        Schema schema = in.getSchema();
        ArrayList<Integer> indexesOfAttributes = new ArrayList<>();
        int indexOfAttribute = 0;
        for (SchemaAttribute attribute : schema.getAttributes()) {
            for (String attributeString : attributes) {
                if (attribute.getAttributeName().equals(attributeString)) {
                    indexesOfAttributes.add(indexOfAttribute);
                }
            }
            indexOfAttribute++;
        }
        ArrayList<Record> records = new ArrayList<>(in.getRecords()); // create a copy of the input records
        records.sort(new RecordComparator(indexesOfAttributes)); // sort the records using the given indexes
        return new Table(records, in.getTableName(), in.getSchema()); // create and return a new table with the sorted records
    }

    //Schema.getattribute(i).getattributename(i)
    private static class RecordComparator implements Comparator<Record> {
        private final ArrayList<Integer> indexes;

        public RecordComparator(ArrayList<Integer> indexes) {
            this.indexes = indexes;
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
                        return cmp;
                    }
                } else if (attr1.getType() == boolean.class) {
                    boolean val1 = (Boolean) attr1.getAttribute();
                    boolean val2 = (Boolean) attr2.getAttribute();
                    int cmp = Boolean.compare(val1, val2);
                    if (cmp != 0) {
                        return cmp;
                    }
                } else if (attr1.getType() == Character.class) {
                    String val1 = (String) attr1.getAttribute();
                    String val2 = (String) attr2.getAttribute();
                    int cmp = val1.compareTo(val2);
                    if (cmp != 0) {
                        return cmp;
                    }
                } else if (attr1.getType() == String.class) {
                    String val1 = (String) attr1.getAttribute();
                    String val2 = (String) attr2.getAttribute();
                    int cmp = val1.compareTo(val2);
                    if (cmp != 0) {
                        return cmp;
                    }
                } else if (attr1.getType() == double.class) {
                    double val1 = (Double) attr1.getAttribute();
                    double val2 = (Double) attr2.getAttribute();
                    int cmp = Double.compare(val1, val2);
                    if (cmp != 0) {
                        return cmp;
                    }
                }
            }
            return 0;
        }
    }
}
