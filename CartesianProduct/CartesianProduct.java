package CartesianProduct;

import java.util.*;
import Record.Record;
import Record.RecordAttribute;
import Catalog.Schema;
import Catalog.SchemaAttribute;
import Catalog.Table;
import InputHandler.BooleanExpressionEvaluator;



public class CartesianProduct {

    // main cartesianProduct
    public static Table cartesianProduct(ArrayList<Table> allTables, String logic, String[] attribtues) {
        Table superTable = null;

        // combine all tables
        for (Table table : allTables) {
            if (allTables.size() == 1) {
                superTable = table;
                break;
            }
            superTable = joinTables(superTable, table);
        }

        // final records
        ArrayList<Record> newRecords = new ArrayList<>();

        // get the index of the attributes that we are sellecting on
        ArrayList<Integer> indexesOfAttributes = getIndexesOfAttributesToKeep(attribtues, superTable.getSchema());
        Schema finalSchema = reduceSchema(superTable.getSchema(), indexesOfAttributes);

        // loop over each record and validate where
        for (Record record : superTable.getRecords()) {
            if (BooleanExpressionEvaluator.evaluate(logic, record, superTable.getSchema())) {
                newRecords.add(reduceAttributes(record, indexesOfAttributes));
            }
        }

        Table finalTable = new Table(newRecords, superTable.getTableName(),finalSchema);

        return finalTable;
    }

    // get index of items that are in the select
    private static ArrayList<Integer> getIndexesOfAttributesToKeep(String[] attributes, Schema schema) {

        // if select all then null
        if (attributes.length == 1 && attributes[0].equals("*")) {
            return null;
        }

        ArrayList<Integer> indexesOfAttributes = new ArrayList<>();

        // check with the schema and get the index of the lable
        int indexOfAttribute = 0;
        for (SchemaAttribute attribute : schema.getAttributes()) {
            for (String attributeString : attributes) {
                if (attribute.getAttributeName().equals(attributeString)) {
                    indexesOfAttributes.add(indexOfAttribute);
                }
                else if(attributeString.indexOf(".") > 0) {
                    if (attribute.getAttributeName().equals(getStringAfterLastPeriod(attributeString))) {
                        indexesOfAttributes.add(indexOfAttribute);
                    }
                }
            }
            indexOfAttribute++;
        }

        return indexesOfAttributes;
    }

    public static String getStringAfterLastPeriod(String input) {
        int lastPeriodIndex = input.lastIndexOf(".");
        if (lastPeriodIndex != -1) {
            return input.substring(lastPeriodIndex + 1);
        } else {
            // handle case where no period is found in the input
            return input;
        }
    }

    // remove all attributes that aren't in the select
    private static Record reduceAttributes(Record record, ArrayList<Integer> indexesOfAttributes) {

        if (indexesOfAttributes == null) {
            return record;
        }

        ArrayList<RecordAttribute> newAttributes = new ArrayList<>();
        for (Integer index : indexesOfAttributes) {
            newAttributes.add(record.getData().get(index));
        }

        return new Record(newAttributes, record.getTableName());
    }

     // remove all cols that aren't in the select
    private static Schema reduceSchema(Schema schema, ArrayList<Integer> indexesOfAttributes) {
        if (indexesOfAttributes == null) {
            return schema;
        }

        ArrayList<SchemaAttribute> newAttributes = new ArrayList<>();
        for (Integer index : indexesOfAttributes) {
            newAttributes.add(schema.getAttributes().get(index));
        }

        return new Schema(schema.getTableName(), newAttributes, null);
    }

    // join two tables
    private static Table joinTables(Table table1, Table table2) {
        ArrayList<Record> newRecords= new ArrayList<>();


        // if the first table is null then just return table 2
        if (table1 == null) {
            Schema newSchema = joinSchemas(null, table2.getSchema());

            return new Table(table2.getRecords(), newSchema.getTableName(), newSchema);
        }

        Schema newSchema = joinSchemas(table1.getSchema(), table2.getSchema());

        for (Record recordFromTable1 : table1.getRecords()) {
            for (Record recordFromTable2 : table2.getRecords()) {
                ArrayList<RecordAttribute> newAttributes = new ArrayList<>();
                newAttributes.addAll(recordFromTable1.getData());
                newAttributes.addAll(recordFromTable2.getData());
                newRecords.add(new Record(newAttributes, recordFromTable2.getTableName()));
            }
        }

        return new Table(newRecords, newSchema.getTableName(), newSchema);
    }

    // combine the schema and change the attribute name to how you'd see it 
    private static Schema joinSchemas(Schema schema1, Schema schema2) {
        ArrayList<SchemaAttribute> newAttributes =  new ArrayList<>();

        if (schema1 == null) {
            newAttributes.addAll(schema2.getJoinedAttributeName());

            return new Schema(schema2.getTableName(), newAttributes, null);
        }
        newAttributes.addAll(schema1.getAttributes());
        newAttributes.addAll(schema2.getJoinedAttributeName());

        return new Schema(schema1.getTableName() + " x " +  schema2.getTableName(), newAttributes, null);
    }

    public static void sort2DArray(String[][] arr, Comparator<String[]> sortingFunction) {
        Arrays.sort(arr, sortingFunction);
    }

    public static String[][] toAry(ArrayList<ArrayList<String>> in) {
        return in.stream().map(u -> u.toArray(new String[0])).toArray(String[][]::new);
    }

}
