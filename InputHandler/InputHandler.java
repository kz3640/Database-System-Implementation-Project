package InputHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import IO.BinaryWriter;
import StorageManager.StorageManager;
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

            // length must be 2 or 3 or invalid.
            // attrName integer primaryKey
            // attname double
            if (!(attributeProperties.length == 2 || attributeProperties.length == 3)) {
                System.out.println("---ERROR---");
                System.out.println("Invalid table attributes. (tooManyAttrs)\n");
                return null;
            }

            // make sure primaryKey is in 3rd positio
            if (attributeProperties.length == 3 && !attributeProperties[2].equals("primarykey")) {
                System.out.println("---ERROR---");
                System.out.println("Invalid table attributes. (invalidPrimaryKey)\n");
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


            String attributeType = attributeProperties[1];
            boolean validAttributeName = attributeName.matches("[a-zA-Z0-9]+");
            boolean validAttributeType = attributeType
                    .matches("integer|double|boolean|char\\([0-9]+\\)|varchar\\([0-9]+\\)");
            boolean isPrimaryKey = attributeProperties.length == 3;

            if (!validAttributeName) {
                System.out.println("---ERROR---");
                System.out.println("Invalid attribute name " + attributeName + "\n");
                return null;
            }
            if (!validAttributeType) {
                System.out.println("---ERROR---");
                System.out.println("Invalid attribute type " + attributeType + "\n");
                return null;
            }

            // format is good to go. Add each to the array

            SchemaAttribute schemaAttribute;
            switch (attributeType) {
                case "integer":
                case "double":
                case "boolean":
                    schemaAttribute = new BICD(attributeName, attributeType, isPrimaryKey, false);
                    schemaAttributes.add(schemaAttribute);
                    continue;
                default:
                    if (attributeType.matches("char\\([0-9]+\\)")) {
                        int leftIndex = attributeType.indexOf("(");
                        int rightIndex = attributeType.indexOf(")");
                        int length = Integer.parseInt(attributeType.substring(leftIndex + 1, rightIndex));
                        schemaAttribute = new Char(attributeName, length, isPrimaryKey, false);
                        schemaAttributes.add(schemaAttribute);
                    } else if (attributeType.matches("varchar\\([0-9]+\\)")) {
                        int leftIndex = attributeType.indexOf("(");
                        int rightIndex = attributeType.indexOf(")");
                        int length = Integer.parseInt(attributeType.substring(leftIndex + 1, rightIndex));
                        schemaAttribute = new Varchar(attributeName, length, isPrimaryKey, false);
                        schemaAttributes.add(schemaAttribute);
                    }
            }
        }
        return schemaAttributes;
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

        ArrayList<SchemaAttribute> schemaAttributes = getAttributeList(attributeList);

        if (schemaAttributes == null)
            return false;

        Schema schema = new Schema(tableName, schemaAttributes, this.storageManager.getCatalog());

        if (!storageManager.addSchema(schema))
            return false;

        writer.initDB(schema);

        return true;
    }

    private void createTable(String originalString) {
        String input = originalString.substring(0, originalString.length() - 1).toLowerCase();

        if (createTableCommand(input)) {
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
                System.out.println("---ERROR---");
                System.out.println("Null values not handled\n");
                return null;
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
                System.out.println("---ERROR---");
                System.out.println("Record to insert does not fit into schema. \n");
                wasErrors = true;
                break;
            }

            if (storageManager.isPrimaryKeyUsed(record)) {
                System.out.println("---ERROR---");
                System.out.println("Primary key is already in use\n");
                wasErrors = true;
                break;
            }

            listOfRecords.add(record);
        }

        for (Record record : listOfRecords) {
            this.storageManager.addRecord(record);
        }
        return !wasErrors;
    }

    private void insertRecord(String originalString) {
        String input = originalString.substring(0, originalString.length() - 1).toLowerCase();

        if (insertRecordCommand(input)) {
            System.out.println("SUCCESS!\n");
        }
    }

    private void select(String originalString) {
        String input = originalString.substring(0, originalString.length() - 1).toLowerCase();

        String[] inputSplitOnSpaces = input.split(" ");

        if (inputSplitOnSpaces.length != 4) {
            System.out.println("---ERROR---");
            System.out.println("Invalid select command\n");
            return;
        }

        if (!inputSplitOnSpaces[2].equals("from")) {
            System.out.println("---ERROR---");
            System.out.println("Invalid select command\n");
            return;
        }

        String args = inputSplitOnSpaces[1];
        String tableName = inputSplitOnSpaces[3];

        if (!this.storageManager.getCatalog().doesTableNameExist(tableName)) {
            System.out.println("---ERROR---");
            System.out.println("Table " + tableName + " not found\n");
            return;
        }

        // TODO: split attributes
        if (!args.equals("*")) {
            System.out.println("---ERROR---");
            System.out.println("Only * attributes can be fetched\n");
            return;
        }

        // TODO: will need to list of attributes
        this.storageManager.select(args, tableName);

    }

    private void display(String originalString) {
        String input = originalString.substring(0, originalString.length() - 1);

        String[] inputArray = input.split(" ");

        // display catalog
        if (inputArray.length == 2 && inputArray[1].equals("schema")) {
            Catalog catalog = storageManager.getCatalog();
            catalog.printCatalog();
            return;
        } else if (inputArray.length == 3 && inputArray[1].equals("info")) {
            storageManager.printTableInfo(inputArray[2]);
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
    }

    public boolean handleInput(String originalString) throws IOException {
        System.out.println();
        String input = originalString.substring(0, originalString.length() - 1);
        String command = input.split(" ")[0];

        switch (command) {
            case "create":
                createTable(originalString);
                break;
            case "select":
                select(originalString);
                break;
            case "insert":
                insertRecord(originalString);
                break;
            case "write":
                storageManager.writeBuffer();
                break;
            case "quit":
                storageManager.writeBuffer();
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
