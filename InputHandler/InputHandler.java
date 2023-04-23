package InputHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import IO.BinaryWriter;
import StorageManager.StorageManager;
import Tree.BPlusTree;
import Util.Util;
import Catalog.BICD;
import Catalog.Catalog;
import Catalog.Char;
import Catalog.Schema;
import Catalog.SchemaAttribute;
import Catalog.Varchar;
import Record.Record;
import Record.RecordAttribute;

public class InputHandler {
    private BinaryWriter writer;
    private StorageManager storageManager;

    public InputHandler(BinaryWriter writer, StorageManager storageManager) {
        this.writer = writer;
        this.storageManager = storageManager;
        return;
    }

    private ArrayList<SchemaAttribute> getAttributeList(String[] attributeList) {
        ArrayList<SchemaAttribute> schemaAttributes = new ArrayList<SchemaAttribute>();

        for (String attributeString : attributeList) {
            attributeString = attributeString.trim();
            String[] attributeProperties = attributeString.split(" ");

            // length must be 2 or 3 or 4 or invalid.
            // attrName char(10) unique notnull
            // attrName integer primaryKey
            // attname double
            if (!(attributeProperties.length == 2 || attributeProperties.length == 3
                    || attributeProperties.length == 4)) {
                System.out.println("---ERROR---");
                System.out.println("Invalid table attributes. (tooManyAttrs)\n");
                return null;
            }

            String attributeName = attributeProperties[0];

            // check if two attributes have the same name
            for (SchemaAttribute schemaAttribute : schemaAttributes) {
                if (attributeName.equals(schemaAttribute.getAttributeName())) {
                    System.out.println("---ERROR---");
                    System.out.println("Two attributes have the same name\n");
                    return null;
                }
            }

            boolean validAttributeName = attributeName.matches("[a-zA-Z0-9]+");

            if (!validAttributeName) {
                System.out.println("---ERROR---");
                System.out.println("Invalid attribute name " + attributeName + "\n");
                return null;
            }

            String attributeType = attributeProperties[1];

            boolean validAttributeType = attributeType
                    .matches("integer|double|boolean|char\\([0-9]+\\)|varchar\\([0-9]+\\)");

            if (!validAttributeType) {
                System.out.println("---ERROR---");
                System.out.println("Invalid attribute type " + attributeType + "\n");
                return null;
            }

            int constraintType1 = 0;
            int constraintType2 = 0;

            boolean isPrimaryKey = false;
            boolean isUnique = false;
            boolean isNotNull = false;
            boolean isDefault = false;
            String defaultValue = null;

            if (attributeProperties.length == 3) {
                constraintType1 = checkAttributeConstraints(attributeProperties[2]);
                switch (constraintType1) {
                    case 1:
                        isPrimaryKey = true;
                        break;
                    case 2:
                        isUnique = true;
                        break;
                    case 3:
                        isNotNull = true;
                        break;
                    case 4:
                        isDefault = true;
                    default:
                        System.out.println("---ERROR---");
                        System.out.println("Constraint " + attributeProperties[2] + " does not exist./n");
                        return null;
                }
            }

            if (attributeProperties.length == 4) {
                constraintType1 = checkAttributeConstraints(attributeProperties[2]);
                switch (constraintType1) {
                    case 1:
                        isPrimaryKey = true;
                        break;
                    case 2:
                        isUnique = true;
                        break;
                    case 3:
                        isNotNull = true;
                        break;
                    case 4:
                        isDefault = true;
                        break;
                    default:
                        System.out.println("---ERROR---");
                        System.out.println("Constraint " + attributeProperties[2] + " does not exist./n");
                        return null;
                }
                constraintType2 = checkAttributeConstraints(attributeProperties[3]);
                switch (constraintType2) {
                    case 1:
                        if (isPrimaryKey) {
                            System.out.println("---ERROR---");
                            System.out.println("Invalid: primarykey constraint entered twice.\n");
                            return null;
                        }
                        isPrimaryKey = true;
                        break;
                    case 2:
                        if (isUnique) {
                            System.out.println("---ERROR---");
                            System.out.println("Invalid: unique constraint entered twice.\n");
                            return null;
                        }
                        isUnique = true;
                        break;
                    case 3:
                        if (isNotNull) {
                            System.out.println("---ERROR---");
                            System.out.println("Invalid: notnull constraint entered twice.\n");
                            return null;
                        }
                        isNotNull = true;
                        break;
                    default:
                        if (isDefault) {
                            defaultValue = attributeProperties[3];
                            if (!Util.doesStringFitType(attributeType, defaultValue)) {
                                System.out.println("---ERROR---");
                                System.out.println("Invalid: default value is not correct type.\n");
                                return null;
                            }
                        }
                        break;
                }
            }

            // format is good to go. Add each to the array

            SchemaAttribute schemaAttribute;
            switch (attributeType) {
                case "integer":
                case "double":
                case "boolean":
                    schemaAttribute = new BICD(
                            attributeName,
                            attributeType,
                            isPrimaryKey,
                            isNotNull,
                            isUnique,
                            Util.convertToType(attributeType, defaultValue));
                    schemaAttributes.add(schemaAttribute);
                    continue;
                default:
                    if (attributeType.matches("char\\([0-9]+\\)")) {
                        int leftIndex = attributeType.indexOf("(");
                        int rightIndex = attributeType.indexOf(")");
                        int length = Integer.parseInt(attributeType.substring(leftIndex + 1, rightIndex));

                        if (defaultValue != null) {
                            defaultValue = defaultValue.substring(1, defaultValue.length() - 1);
                        }

                        if (defaultValue != null && defaultValue.length() > length + 2) {
                            System.out.println("---ERROR---");
                            System.out.println("default value does not fit");
                            return null;
                        }

                        schemaAttribute = new Char(
                                attributeName,
                                length,
                                isPrimaryKey,
                                isNotNull,
                                isUnique,
                                defaultValue);
                        schemaAttributes.add(schemaAttribute);
                    } else if (attributeType.matches("varchar\\([0-9]+\\)")) {
                        int leftIndex = attributeType.indexOf("(");
                        int rightIndex = attributeType.indexOf(")");
                        int length = Integer.parseInt(attributeType.substring(leftIndex + 1, rightIndex));

                        if (defaultValue != null) {
                            defaultValue = defaultValue.substring(1, defaultValue.length() - 1);
                        }

                        if (defaultValue != null && defaultValue.length() > length + 2) {
                            System.out.println("---ERROR---");
                            System.out.println("default value does not fit");
                            return null;
                        }

                        schemaAttribute = new Varchar(
                                attributeName,
                                length,
                                isPrimaryKey,
                                isNotNull,
                                isUnique,
                                defaultValue);
                        schemaAttributes.add(schemaAttribute);
                    }
            }
        }
        return schemaAttributes;
    }

    private int checkAttributeConstraints(String constraint) {
        switch (constraint) {
            case "primarykey":
                return 1;
            case "unique":
                return 2;
            case "notnull":
                return 3;
            case "default":
                return 4;
            default:
                return 0;
        }
    }

    /*
     * Checks to see if string is a key word
     * Used to verify if a table name or attribute is a valid word and is not bad
     */
    private boolean isKeyWord(String word) {
        List<String> keyWords = List.of("integer", "double", "boolean", "char", "varchar", "create", "table", "record",
                "page",
                "drop", "primarykey", "select", "from", "insert", "into", "values", "display", "info",
                "unique", "notnull", "drop", "add", "default", "delete", "where", "and", "or", "update",
                "set", "orderby", "*");
        return keyWords.contains(word);
    }

    private boolean createTableCommand(String input) {
        String[] inputSplitOnSpaces = input.split(" ", 3);

        String command = inputSplitOnSpaces[0]; // already verified
        String tableKeyWord = inputSplitOnSpaces[1]; // should be "table"
        String tableNameAndAttributes = inputSplitOnSpaces[2]; // foo(attr1 x x ....)

        // if it's not table, return
        if (!tableKeyWord.equals("table"))
            return false;

        // match parenthesis
        if (!doParenthesisMatch(tableNameAndAttributes, false)) {
            System.out.println("Invalid parenthesis");
            return false;
        }

        int leftIndex = tableNameAndAttributes.indexOf("(");
        int rightIndex = tableNameAndAttributes.lastIndexOf(")");
        String tableName = tableNameAndAttributes.substring(0, leftIndex).trim();
        String attributes = tableNameAndAttributes.substring(leftIndex + 1, rightIndex);

        // tableName is a resevered word
        if (isKeyWord(tableName)) {
            System.out.println("---ERROR---");
            String tempString = String.format("Table name: %s is a reserved word\n", tableName);
            System.out.println(tempString);
            return false;
        }

        // must have table name
        if (tableName.equals("")) {
            System.out.println("---ERROR---");
            System.out.println("No table name\n");
            return false;
        }
        // tableName contains bad characters
        if (!tableName.matches("[a-zA-Z0-9]+")) {
            System.out.println("---ERROR---");
            System.out.println("Bad table name\n");
            return false;
        }
        // must have attributes
        if (attributes.equals("")) {
            System.out.println("---ERROR---");
            System.out.println("No attributes\n");
            return false;
        }
        // must have exactly 1 primary key
        int primaryKeyIndex = attributes.indexOf("primarykey");
        if (!(primaryKeyIndex != -1 && attributes.indexOf("primarykey", primaryKeyIndex + 1) == -1)) {
            System.out.println("---ERROR---");
            System.out.println("Must be exactly one primary key\n");
            return false;
        }

        // split the comma seperated list of attributes [attrName integer primarykey,
        // attrName2 double, ...]
        String[] attributeList = attributes.split(",");

        // checks each attribute to see if it is a reserved word
        for (int i = 0; i < attributeList.length; i++) {
            if (isKeyWord(attributeList[i])) {
                System.out.println("---ERROR---");
                String tempString = String.format("Table name: %s is a reserved word\n", attributeList[i]);
                System.out.println(tempString);
                return false;
            }
        }

        ArrayList<SchemaAttribute> schemaAttributes = getAttributeList(attributeList);

        if (schemaAttributes == null)
            return false;

        String type = "int";
        for (SchemaAttribute schemaAttribute : schemaAttributes) {
            if (schemaAttribute.isPrimaryKey()) {
                if (schemaAttribute.getTypeAsString().equals("varchar")
                        || schemaAttribute.getTypeAsString().equals("char")) {
                    type = "string";
                } else {
                    type = schemaAttribute.getTypeAsString();
                }
            }
        }

        // TODO FIX
        BPlusTree bpt = new BPlusTree(4, type);
        Schema schema = new Schema(tableName, schemaAttributes, this.storageManager.getCatalog(), bpt);

        if (!storageManager.addSchema(schema))
            return false;

        writer.initDB(schema);

        return true;
    }

    private boolean dropTableCommand(String input) {
        String[] inputSplitOnSpaces = input.split(" ", 3);

        if (inputSplitOnSpaces.length != 3) {
            System.out.println("---ERROR---");
            System.out.println("invalid drop command\n");
            return false;
        }

        String command = inputSplitOnSpaces[0]; // already verified
        String tableKeyWord = inputSplitOnSpaces[1]; // should be "table"
        String tableName = inputSplitOnSpaces[2]; // foo(attr1 x x ....)

        // if it's not table, return
        if (!tableKeyWord.equals("table"))
            return false;

        // must have table name
        if (tableName.equals("")) {
            System.out.println("---ERROR---");
            System.out.println("No table name\n");
            return false;
        }
        // tableName contains bad characters
        if (!tableName.matches("[a-zA-Z0-9]+")) {
            System.out.println("---ERROR---");
            System.out.println("Bad table name\n");
            return false;
        }

        Schema schema = this.storageManager.getCatalog().getSchemaByName(tableName);

        if (!storageManager.dropTable(schema))
            return false;

        return true;
    }

    private boolean alterTableCommand(String input) {
        String[] inputSplitOnSpaces = input.split(" ", 5);

        String command = inputSplitOnSpaces[0]; // already verified
        String tableKeyWord = inputSplitOnSpaces[1]; // should be "table"
        String tableName = inputSplitOnSpaces[2]; // foo
        String actionKeyWord = inputSplitOnSpaces[3]; // add or drop
        String attribute = inputSplitOnSpaces[4]; // attribute name

        // if it's not table, return
        if (!tableKeyWord.equals("table"))
            return false;

        // must have table name
        if (tableName.equals("")) {
            System.out.println("---ERROR---");
            System.out.println("No table name\n");
            return false;
        }
        // tableName contains bad characters
        if (!tableName.matches("[a-zA-Z0-9]+")) {
            System.out.println("---ERROR---");
            System.out.println("Bad table name\n");
            return false;
        }

        Schema schema = this.storageManager.getCatalog().getSchemaByName(tableName); // making sure table exists

        if (schema == null) {
            return false;
        }

        // must have an attribute
        if (attribute.equals("")) {
            System.out.println("---ERROR---");
            System.out.println("No attributes\n");
            return false;
        }

        String[] attList = attribute.split(" ");

        ArrayList<SchemaAttribute> currentAtt = new ArrayList<>(schema.getAttributes());

        switch (actionKeyWord) {
            case "add":
                if (attList.length < 2) // attribute must be defined
                    return false;

                String[] nAttribute = { attribute };
                ArrayList<SchemaAttribute> nSchemaAttribute = getAttributeList(nAttribute);

                if (nSchemaAttribute == null) {
                    return false; // change wasn't valid
                }

                // attribute must not exist in the table
                for (SchemaAttribute schemaAttribute : schema.getAttributes()) {
                    if (nSchemaAttribute.get(0).getAttributeName().equals(schemaAttribute.getAttributeName())) {
                        System.out.println("---ERROR---");
                        System.out.println("Attribute already in table\n");
                        return false;
                    }
                }

                currentAtt.add(nSchemaAttribute.get(0)); // add new schema attribute to list of existing schema
                                                         // attributes

                // TODO FIX
                Schema naSchema = new Schema(tableName, currentAtt, this.storageManager.getCatalog(), null);
                naSchema.setIndex(schema.getIndex());

                if (!storageManager.alterSchema(schema, naSchema))
                    return false;
                break;

            case "drop":
                if (attList.length != 1) {
                    System.out.println("---ERROR---");
                    System.out.println("drop command not valid");
                    return false;
                } // must only contain existing attribute's name

                int idx = -1;
                for (int i = 0; i < currentAtt.size(); i++) {
                    if (currentAtt.get(i).getAttributeName().equals(attList[0])) // attribute exists in the table
                        idx = i;
                }

                if (idx == -1) {
                    System.out.println("---ERROR---");
                    System.out.println("Attribute not found");
                    return false;
                } // attribute does not exist in the table

                if (currentAtt.get(idx).isPrimaryKey()) {
                    System.out.println("---ERROR---");
                    System.out.println("Cannot drop primary key\n");
                    return false;
                }

                currentAtt.remove(idx);
                // TODO FIX
                Schema ndSchema = new Schema(tableName, currentAtt, this.storageManager.getCatalog(), null);
                ndSchema.setIndex(schema.getIndex());

                if (!storageManager.alterSchema(schema, ndSchema))
                    return false;
                break;

            default:
                return false;
        }

        return true;
    }

    private void createTable(String originalString) {
        String input = originalString.substring(0, originalString.length() - 1);

        if (createTableCommand(input)) {
            System.out.println("SUCCESS!");
        }
    }

    private void dropTable(String originalString) {
        String input = originalString.substring(0, originalString.length() - 1);

        if (dropTableCommand(input)) {
            System.out.println("SUCCESS!");
        }
    }

    private void alterTable(String originalString) {
        String input = originalString.substring(0, originalString.length() - 1);

        if (alterTableCommand(input)) {
            System.out.println("SUCCESS!");
        }
    }

    private Record convertListToRecord(List<String> input, String tableName) {
        Schema schema = this.storageManager.getCatalog().getSchemaByName(tableName);

        ArrayList<RecordAttribute> recordData = new ArrayList<RecordAttribute>();
        int index = 0;
        for (String s : input) {
            if (schema.getAttributes().size() <= index) {
                return null;
            }
            if (s.equals("null")) {
                // null
                recordData.add(new RecordAttribute(null, null, 0));
                index++;
                continue;
            } else if (s.startsWith("\"") && s.endsWith("\"")) {
                // a string
                if (schema.getAttributes().get(index).getTypeAsString() == "char") {
                    // char
                    int charLength = schema.getAttributes().get(index).getLength();
                    recordData.add(new RecordAttribute(Character.class, s.substring(1, s.length() - 1), charLength));
                } else if (schema.getAttributes().get(index).getTypeAsString() == "varchar") {
                    // varchar
                    recordData.add(new RecordAttribute(String.class, s.substring(1, s.length() - 1), 0));
                } else {
                    return null;
                    // invalid
                }
            } else if (s.equals("true") || s.equals("false")) {
                // boolean
                boolean b = Boolean.parseBoolean(s);
                recordData.add(new RecordAttribute(boolean.class, b, 0));
            } else {
                if (schema.getAttributes().get(index).getTypeAsString().equals("integer")) {
                    try {
                        // integer
                        int i = Integer.parseInt(s);
                        recordData.add(new RecordAttribute(int.class, i, 0));
                    } catch (NumberFormatException e) {
                        // no data type. Error
                        return null;
                    }
                } else if (schema.getAttributes().get(index).getTypeAsString().equals("double")) {
                    try {
                        // double
                        double d = Double.parseDouble(s);
                        recordData.add(new RecordAttribute(double.class, d, 0));
                    } catch (NumberFormatException e2) {
                        // no data type. Error
                        return null;
                    }
                } else {
                    System.out.println(s + " does not match the schema type defined for that attribute. "
                            + schema.getAttributes().get(index).getTypeAsString());
                    return null;
                }
            }
            index++;
        }

        Record r = new Record(recordData, tableName);
        return r;
    }

    private boolean doParenthesisMatch(String string, boolean allowNested) {
        int countLeftParenthesis = string.length() - string.replace("(", "").length();
        int countRightParenthesis = string.length() - string.replace(")", "").length();
        int leftIndex = string.indexOf("(");
        int rightIndex = string.lastIndexOf(")");

        // match parenthesis
        if (!allowNested)
            return (leftIndex != -1 && rightIndex > leftIndex && countLeftParenthesis == countRightParenthesis);

        Stack<Character> stack = new Stack<>();
        boolean inQuote = false;
        for (char c : string.toCharArray()) {
            if (c == '"') {
                inQuote = !inQuote;
            } else if (c == '(' && !inQuote) {
                if (!stack.isEmpty()) {
                    return false;
                }
                stack.push(c);
            } else if (c == ')' && !inQuote) {
                if (stack.isEmpty() || stack.pop() != '(') {
                    return false;
                }
            }
        }

        return stack.isEmpty()
                && (leftIndex != -1 && rightIndex > leftIndex && countLeftParenthesis == countRightParenthesis);
    }

    private List<String> splitStringByParen(String input) {
        List<String> result = new ArrayList<>();
        int start = 0;
        int openParenCount = 0;
        boolean inQuote = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
            }
            if (!inQuote && c == '(') {
                openParenCount++;
            }
            if (!inQuote && c == ')') {
                openParenCount--;
            }
            if (!inQuote && openParenCount == 0 && c == ',') {
                result.add(input.substring(start, i).trim());
                start = i + 1;
            }
        }
        result.add(input.substring(start).trim());
        return result;
    }

    private List<String> formatStringList(List<String> strings) {
        List<String> formattedStrings = new ArrayList<>();

        for (String s : strings) {
            s = s.trim();

            if (!s.startsWith("(") || !s.endsWith(")")) {
                System.out.println("---ERROR---");
                System.out.println("String does not have matching parentheses.\n");
                return null;
            }

            s = s.substring(1, s.length() - 1);
            s = s.trim();

            formattedStrings.add(s);
        }

        return formattedStrings;
    }

    public List<String> splitStringIgnoringQuotedSpaces(String string) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
                currentToken.append(c);
            } else if (c == ' ' && !inQuotes) {
                tokens.add(currentToken.toString());
                currentToken.setLength(0);
            } else {
                currentToken.append(c);
            }
        }
        tokens.add(currentToken.toString());
        return tokens;
    }

    private boolean insertRecordCommand(String input) {
        String[] inputSplitOnSpaces = input.split(" ", 4);

        if (inputSplitOnSpaces.length != 4) {
            System.out.println("---ERROR---");
            System.out.println("invalid insert command\n");
            return false;
        }

        String command = inputSplitOnSpaces[0]; // already verified
        String intoKeyWord = inputSplitOnSpaces[1]; // should be "into"
        String tableName = inputSplitOnSpaces[2]; // name of table
        String valuesKeyWordAndValues = inputSplitOnSpaces[3]; // should be "values"
        String valuesKeyWord = valuesKeyWordAndValues.substring(0, 6);
        String values = valuesKeyWordAndValues.substring(6, valuesKeyWordAndValues.length()).trim();
        // should be a list of values;

        if (!intoKeyWord.equals("into")) {
            System.out.println("---ERROR---");
            System.out.println("invalid insert command (into)\n");
            return false;
        }
        if (!valuesKeyWord.equals("values")) {
            System.out.println("---ERROR---");
            System.out.println("invalid insert command (values)\n");
            return false;
        }

        // check if table exists.
        if (!this.storageManager.getCatalog().doesTableNameExist(tableName)) {
            System.out.println("---ERROR---");
            System.out.println("Table name " + tableName + " does not exist\n");
            return false;
        }

        // match parenthesis
        if (!doParenthesisMatch(values, true)) {
            System.out.println("---ERROR---");
            System.out.println("Invalid parenthesis\n");
            return false;
        }

        List<String> valuesList = splitStringByParen(values);
        List<String> cleanValuesList = formatStringList(valuesList);
        if (cleanValuesList == null) {
            System.out.println("---ERROR---");
            System.out.println("Not Clean\n");
            return false;
        }

        List<Record> listOfRecords = new ArrayList<>();
        boolean wasErrors = false;
        for (String string : cleanValuesList) {
            List<String> splitStrings = splitStringIgnoringQuotedSpaces(string);

            Record record = convertListToRecord(splitStrings, tableName);

            if (record == null) {
                System.out.println("---ERROR---");
                System.out.println("Parsing error when parsing " + input + "\n");
                wasErrors = true;
                break;
            }

            Schema schema = this.storageManager.getCatalog().getSchemaByName(tableName);
            if (!schema.doesRecordFollowSchema(record)) {
                wasErrors = true;
                break;
            }

            if (!storageManager.doesRecordFollowConstraints(record, tableName)) {
                wasErrors = true;
                break;
            }

            listOfRecords.add(record);
        }

        for (Record record : listOfRecords) {
            this.storageManager.addRecord(record,
                    this.storageManager.getCatalog().getSchemaByName(record.getTableName()));
        }
        return !wasErrors;
    }

    private void insertRecord(String originalString) {
        String input = originalString.substring(0, originalString.length() - 1);

        if (insertRecordCommand(input)) {
            System.out.println("SUCCESS!\n");
        }
    }

    private void select(String originalString) {
        String selectAttr = "";

        String input = originalString.substring(0, originalString.length() - 1);

        // select * from ... where ... orderby ...
        String[] inputSelect = input.split("select");
        // * from ... where ... orderby ...

        if (inputSelect.length == 1) { // [["invalid statement"]]
            System.out.println("---ERROR---");
        }
        if (inputSelect.length == 1) { // [["invalid statement"]]
            System.out.println("---ERROR---");
            System.out.println("Invalid select query\n");
            return;
        }

        // * from ... where ... orderby ...
        String[] inputSelectFrom = inputSelect[1].split("from");
        // ... where ... orderby ...

        if (inputSelectFrom.length == 1) { // ["invalid statement"]
            System.out.println("---ERROR---");
            System.out.println("Invalid select query\n");
            return;
        }

        selectAttr = inputSelectFrom[0]; // selectAttr = "att"
        String[] attributes = stripWhitespace(selectAttr.trim().split(","));

        String reamingInput = inputSelectFrom[1];

        // ... where ... orderby ...
        String[] inputFromOrderby = reamingInput.split("orderby");
        // ... where ...

        String[] order;

        if (inputFromOrderby.length == 1) {
            order = null;
        } else {
            order = stripWhitespace(inputFromOrderby[1].trim().split(","));
            reamingInput = inputFromOrderby[0];
        }

        // ... where ...
        String[] inputFromWhere = reamingInput.split("where");
        // tableNames
        String condition;

        if (inputFromWhere.length == 1) {
            condition = "true";
        } else {
            condition = inputFromWhere[1].trim();
            reamingInput = inputFromWhere[0];
        }

        String[] tableNames = stripWhitespace(reamingInput.trim().split(","));

        if (!validateSelectAttributes(attributes, tableNames, condition, order)) {
            return;
        }

        this.storageManager.select(attributes, tableNames, condition, order);

    }

    private static String[] stripWhitespace(String[] attributes) {
        for (int i = 0; i < attributes.length; i++) {
            attributes[i] = attributes[i].trim();
        }
        return attributes;
    }

    private boolean validateSelectAttributes(String[] sAttributes, String[] tableNames, String conditions,
            String[] oAttributes) {
        for (int i = 0; i < tableNames.length; i++) { // validating table names
            if (!this.storageManager.getCatalog().doesTableNameExist(tableNames[i])) {
                System.out.println("---ERROR---");
                System.out.println("Table " + tableNames[i] + " not found\n");
                return false;
            }
        }

        Schema[] schemasUsed = new Schema[tableNames.length];
        for (int i = 0; i < tableNames.length; i++) { // validating table names
            schemasUsed[i] = this.storageManager.getCatalog().getSchemaByName(tableNames[i]);
        }

        // validate sAttributes
        for (int i = 0; i < sAttributes.length; i++) { // validating column names
            if (sAttributes.length > 1 && sAttributes[i].equals("*")) {
                System.out.println("---ERROR---");
                System.out.println("* attribute cannot be used with other attributes.");
                return false;
            }

            if (sAttributes.length == 1 && sAttributes[i].equals("*")) {
                break;
            }

            String[] splitSAtt = sAttributes[i].split("\\.");
            if (tableNames.length == 1) { // single table
                if (splitSAtt.length == 1) { // splitSAtt = attribute
                    if (schemasUsed[0].getIndexOfAttributeName(sAttributes[i]) == -1) {
                        System.out.println("---ERROR---");
                        System.out.println("select attribute " + sAttributes[i] + " not found\n");
                        return false;
                    }
                } else { // splitSAtt = tabename.attribute
                    if (!splitSAtt[0].equals(tableNames[0])) {
                        System.out.println("---ERROR---");
                        System.out.println("table " + splitSAtt[0] + " in select attribute not found\n");
                        return false;
                    }
                }
            } else { // multitables
                Schema checkSchema = this.storageManager.getCatalog().getSchemaByName(splitSAtt[0]);
                if (checkSchema == null) {
                    break;
                }
                if (checkSchema.getIndexOfAttributeName(splitSAtt[1]) == -1) {
                    System.out.println("---ERROR---");
                    System.out.println("select attribute " + sAttributes[i] + " not found\n");
                    return false;
                }
            }
        }

        // validate where
        if (!conditions.equals("true")) {
            for (int i = 0; i < schemasUsed.length; i++) {
                if (tableNames.length == 1) { // single table
                    if (!isValidCondition(conditions.toString(), schemasUsed[0])) {
                        System.out.println("---ERROR---");
                        System.out.println("Invalid format of where statement\n");
                        return false;
                    }
                } else { // multitables
                    if (!isValidConditionMultiTable(conditions.toString())) {
                        System.out.println("---ERROR---");
                        System.out.println("Invalid format of where statement\n");
                        return false;
                    }
                }
            }
        }

        // validate orderby (multitables)
        String[] splitOAtt;

        if (oAttributes != null) {
            for (int i = 0; i < oAttributes.length; i++) {
                splitOAtt = oAttributes[i].split(".");

                if (schemasUsed.length == 1) { // single table
                    if (splitOAtt.length == 1) { // no . operator used - only one table
                        if (schemasUsed[0].getIndexOfAttributeName(splitOAtt[0]) != -1)
                            continue;
                        else {
                            System.out.println("---ERROR---");
                            System.out.println("orderby attribute " + oAttributes[i] + " not found \n");
                            return false;
                        }
                    } else {
                        if (!splitOAtt[0].equals(tableNames[0])) {
                            System.out.println("---ERROR---");
                            System.out.println("table " + splitOAtt[0] + " not found \n");
                            return false;
                        }
                        if (schemasUsed[0].getIndexOfAttributeName(splitOAtt[1]) != -1)
                            continue;
                        else {
                            System.out.println("---ERROR---");
                            System.out.println("orderby attribute " + oAttributes[i] + " not found \n");
                            return false;
                        }
                    }
                } else { // multitables
                    if (splitOAtt.length == 1) {
                        System.out.println("---ERROR---");
                        System.out.println("orderby table name for " + oAttributes[i] + " not specified \n");
                        return false;
                    } else {
                        Schema schema = this.storageManager.getCatalog().getSchemaByName(splitOAtt[0]);
                        if (schema.getIndexOfAttributeName(splitOAtt[1]) != -1)
                            continue;
                        else {
                            System.out.println("---ERROR---");
                            System.out.println("orderby attribute " + oAttributes[i] + " not found \n");
                            return false;
                        }
                    }
                }

            }
        }
        return true;
    }

    private void delete(String originalString) {
        String input = originalString.substring(0, originalString.length() - 1);

        String[] inputSplitOnSpaces = input.split(" ", 5);

        if (inputSplitOnSpaces.length < 3) {
            return;
        }

        String command = inputSplitOnSpaces[0]; // already verified
        String from = inputSplitOnSpaces[1]; // should be "from"
        String tableName = inputSplitOnSpaces[2]; // name of table

        if (!from.equals("from")) {
            System.out.println("---ERROR---");
            System.out.println("Bad delete command format\n");
            return;
        }

        if (inputSplitOnSpaces.length != 3 && inputSplitOnSpaces.length != 5) {
            System.out.println("---ERROR---");
            System.out.println("Bad delete command format\n");
            return;
        }

        Schema schema = this.storageManager.getCatalog().getSchemaByName(tableName);

        if (schema == null) {
            return;
        }

        if (inputSplitOnSpaces.length == 3) {
            // delete all items from db

            if (this.storageManager.delete(tableName, "true")) {
                System.out.println("SUCCESS!");
            }
            return;
        }

        String where = inputSplitOnSpaces[3]; // should be "where"
        String logic = inputSplitOnSpaces[4]; // condition

        if (!where.equals("where")) {
            System.out.println("---ERROR---");
            System.out.println("Bad delete command format\n");
            return;
        }

        if (!isValidCondition(logic, schema)) {
            System.out.println("Invalid format of where statement\n");
            return;
        }

        if (this.storageManager.delete(tableName, logic)) {
            System.out.println("SUCCESS!");
        }
    }

    private void update(String originalString) {
        String input = originalString.substring(0, originalString.length() - 1);

        String[] inputSplitOnSpaces = input.split(" ", 8);

        if (inputSplitOnSpaces.length < 6) {
            System.out.println("---ERROR---");
            System.out.println("Bad update command format\n");
            return;
        }

        String command = inputSplitOnSpaces[0]; // already verified
        String tableName = inputSplitOnSpaces[1]; // table name
        String set = inputSplitOnSpaces[2]; // should be set
        String col = inputSplitOnSpaces[3]; // col name
        String equals = inputSplitOnSpaces[4]; // =
        String val = inputSplitOnSpaces[5]; // value

        Schema schema = this.storageManager.getCatalog().getSchemaByName(tableName);
        if (schema == null)
            return;

        if ((!set.equals("set")) || (!equals.equals("="))) {
            System.out.println("---ERROR---");
            System.out.println("Bad update command format\n");
            return;
        }

        if (inputSplitOnSpaces.length == 6) {
            // update all items from db
            if (this.storageManager.update(tableName, col, val, "true"))
                System.out.println("SUCCESS!");
            return;
        }

        if (inputSplitOnSpaces.length != 8) {
            System.out.println("---ERROR---");
            System.out.println("Bad update command format\n");
            return;
        }

        String where = inputSplitOnSpaces[6]; // should be "where"
        String logic = inputSplitOnSpaces[7]; // condition

        if (!where.equals("where")) {
            System.out.println("---ERROR---");
            System.out.println("Bad delete command format\n");
            return;
        }

        if (!isValidCondition(logic, schema)) {
            System.out.println("Invalid format of where statement\n");
            return;
        }

        if (this.storageManager.update(tableName, col, val, logic))
            System.out.println("SUCCESS!");
    }

    private boolean isValidCondition(String condition, Schema schema) {
        String[] conditions = condition.split("(?i)\\s+(and|or)\\s+");
        for (String cond : conditions) {
            String[] parts = cond.split("\\s+");
            if (parts.length != 3) {
                return false;
            }
            String left = parts[0];
            String op = parts[1];
            String right = parts[2];

            if (!isValidValue(left, schema) || !isValidOperator(op) || !isValidValue(right, schema)) {
                System.out.println("---ERROR---");
                if (!isValidValue(left, schema)) {
                    System.out.println(left + " is not a valid attribute name or value");
                }
                if (!isValidOperator(op)) {
                    System.out.println(op + " is not a valid operator");
                }
                if (!isValidValue(right, schema)) {
                    System.out.println(right + " is not a valid attribute name or value");
                }
                return false;
            }
        }
        return true;
    }

    private boolean isValidConditionMultiTable(String condition) {
        String[] conditions = condition.split("(?i)\\s+(and|or)\\s+");
        for (String cond : conditions) {
            String[] parts = cond.split(" ");
            if (parts.length != 3) {
                return false;
            }
            String[] left = parts[0].split("\\.");
            String op = parts[1];
            String[] right = parts[2].split("\\.");

            boolean wasError = false;
            if (left.length > 1) {
                wasError = !isValidValue(left[1], this.storageManager.getCatalog().getSchemaByName(left[0]));
            } else {
                wasError = !isValidValue(left[0], null);
            }

            wasError = !isValidOperator(op);

            if (right.length > 1) {
                wasError = !isValidValue(right[1], this.storageManager.getCatalog().getSchemaByName(right[0]));
            } else {
                wasError = !isValidValue(right[0], null);
            }

            if (wasError) {
                System.out.println("---ERROR---");
                System.out.println("error with condition '" + condition + "'");
                return false;
            }

            // }
        }
        return true;
    }

    private boolean isValidOperator(String op) {
        return op.matches("[<>!=]=?");
    }

    private boolean isValidValue(String val, Schema schema) {
        if (val.matches("true|false")) {
            return true;
        }
        if (val.matches("[a-zA-Z]\\w*")) {
            return schema.isAtributeInSchema(val);
        }
        return val.matches("(\\d+|\\d+\\.\\d+|\"[^\"]*\")");
    }

    private void display(String originalString) {
        String input = originalString.substring(0, originalString.length() - 1);

        String[] inputArray = input.split(" ");

        // display catalog
        if (inputArray.length == 2 && inputArray[1].equals("schema")) {
            Catalog catalog = storageManager.getCatalog();
            catalog.printCatalog(storageManager);
            return;
        } else if (inputArray.length == 3 && inputArray[1].equals("info")) {
            if (storageManager.printTableInfo(inputArray[2]))
                System.out.println("SUCCESS!");
            return;
        }

        displayHelp();
        return;
    }

    private void displayHelp() {
        System.out.println("Create table command:");
        System.out.println("create table <name>(\n" +
                "   <attr_name1> <attr_typ['e1> primarykey,\n" +
                "   <attr_name2> <attr_type2>,\n" +
                "   ....\n" +
                "   <attr_nameN> <attr_typeN>\n" +
                ");\n");

        System.out.println("Select command:");
        System.out.println("select * " +
                "from <name>;\n");

        System.out.println("Insert into command:");
        System.out.println("insert into <name> values <tuples>;\n");

        System.out.println("Display catalog command:");
        System.out.println("display catalog;\n");

        System.out.println("Display info command:");
        System.out.println("display info <name>;\n");

        System.out.println("Drop table command:");
        System.out.println("drop table <name>;\n");

        System.out.println("Alter table command:");
        System.out.println("alter table <name> add <a_name> <a_type>;");
        System.out.println("alter table <name> add <a_name> <a_type> default <value>;\n");
    }

    public boolean handleInput(String originalString) throws IOException {
        System.out.println();
        String input = originalString.substring(0, originalString.length() - 1);
        String command = input.split(" ")[0];

        switch (command) {
            case "create":
                createTable(originalString);
                break;
            case "drop":
                dropTable(originalString);
                break;
            case "alter":
                alterTable(originalString);
                break;
            case "select":
                select(originalString);
                break;
            case "delete":
                delete(originalString);
                break;
            case "update":
                update(originalString);
                break;
            case "insert":
                insertRecord(originalString);
                break;
            case "write":
                storageManager.writeBuffer();
                break;
            case "quit":
                storageManager.writeBuffer();
                storageManager.writeBPlusTrees();
                return false;
            case "display":
                display(originalString);
                return true;
            case "help":
                displayHelp();
                break;
            default:
                System.out.println("---ERROR---");
                System.out.println("Unrecognized command. Type 'help;' for a list of commands.\n");
                break;
        }

        return true;
    }
}
