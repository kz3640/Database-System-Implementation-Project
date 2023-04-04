package InputHandler;

import java.util.*;

import Catalog.Schema;
import Catalog.SchemaAttribute;
import Record.Record;

public class BooleanExpressionEvaluator {
    public static boolean evaluate(String expression, Record record, Schema schema) {

        // Split the expression into its individual terms
        expression = expression.replaceAll(" or ", "|");
        expression = expression.replaceAll(" and ", "&");
        String[] terms = expression.split("((?<=\\|)|(?=\\|))|((?<=&)|(?=&))");
        // ((?<=x)|(?=x)) evaluates to select an empty character before or behind x
        // the big regex thing splits on empty characters before or behind | and &

        for (int i = 0; i < terms.length; i++) {
            // change to char operators, remove whitespace
            terms[i] = terms[i].replaceAll("\\s+", "");
        }

        // Create stacks to hold the operands and operators
        Stack<Boolean> operandStack = new Stack<>();
        Stack<Character> operatorStack = new Stack<>();

        // Process each term in the expression
        for (String term : terms) {
            if (term.length() == 0) {
                // Skip empty terms
                continue;
            } else if (term.equals("(")) {
                // Push opening parentheses onto the operator stack
                operatorStack.push('(');
            } else if (term.equals(")")) {
                // Process everything in parentheses
                while (operatorStack.peek() != '(') {
                    applyOperator(operandStack, operatorStack);
                }
                operatorStack.pop(); // Pop the opening parenthesis
            } else if (term.equals("|") || term.equals("&")) {
                // Process any operators with higher or equal precedence
                while (!operatorStack.isEmpty() && hasHigherPrecedence(operatorStack.peek(), term.charAt(0))) {
                    applyOperator(operandStack, operatorStack);
                }
                operatorStack.push(term.charAt(0)); // Push the new operator onto the stack
            } else {
                // Parse the operand and push it onto the operand stack
                boolean operand = parseOperand(term, record, schema);
                operandStack.push(operand);
            }
        }

        // Process any remaining operators
        while (!operatorStack.isEmpty()) {
            applyOperator(operandStack, operatorStack);
        }

        // The final result should be on the top of the operand stack
        return operandStack.pop();
    }

    private static String replaceWithAttributeValue(String attributeOrValue, Record record, Schema schema) {
        if (attributeOrValue.matches("true|false")) {
            return attributeOrValue;
        }

        // multiTable
        if (attributeOrValue.contains(".")) {
            int indexOfAttribute = 0;
            for (SchemaAttribute attribute : schema.getAttributes()) {
                if (attribute.getAttributeName().equals(attributeOrValue)) {
                    return record.getData().get(indexOfAttribute).getAttribute().toString();
                }
                indexOfAttribute++;
            }
        }

        if (attributeOrValue.matches("[a-zA-Z]\\w*")) {
            int indexOfAttribute = schema.getIndexOfAttributeName(attributeOrValue);
            return record.getData().get(indexOfAttribute).getAttribute().toString();
        }
        if (attributeOrValue.startsWith("\"") && attributeOrValue.endsWith("\"")) {
            return attributeOrValue.substring(1, attributeOrValue.length() - 1);
        }
        return attributeOrValue;
    }

    private static boolean parseOperand(String term, Record record, Schema schema) {
        // TODO
        if (term.equals("true")) {
            return true;
        } else if (term.equals("false")) {
            return false;
        } // else {
          // throw new IllegalArgumentException("Invalid operand: " + term);
          // }

        String[] vars = term.split(
                "((?<=>=)|(?=>=))|((?<=<=)|(?=<=))|((?<=!=)|(?=!=))|((?<==)|(?==))|((?<=<)|(?=<))|((?<=>)|(?=>))");
        if (vars.length == 4) {
            vars[1] = vars[1] + vars[2];
            vars[2] = vars[3];
        }

        String operan1 = replaceWithAttributeValue(vars[0], record, schema);
        String operan2 = replaceWithAttributeValue(vars[2], record, schema);

        switch (vars[1]) {
            case ">=":
                return Double.parseDouble(operan1) >= Double.parseDouble(operan2);
            case "<=":
                return Double.parseDouble(operan1) <= Double.parseDouble(operan2);
            case "!=":
                return Double.parseDouble(operan1) != Double.parseDouble(operan2);
            case "=":
                return operan1.equals(operan2);
            case ">":
                return Double.parseDouble(operan1) > Double.parseDouble(operan2);
            case "<":
                return Double.parseDouble(operan1) < Double.parseDouble(operan2);
        }
        throw new IllegalArgumentException("Invalid operand: " + term);
    }

    private static void applyOperator(Stack<Boolean> operandStack, Stack<Character> operatorStack) {
        char operator = operatorStack.pop();
        boolean operand2 = operandStack.pop();
        boolean operand1 = operandStack.pop();
        boolean result = evaluateBinaryOperator(operand1, operand2, operator);
        operandStack.push(result);
    }

    private static boolean evaluateBinaryOperator(boolean operand1, boolean operand2, char operator) {
        switch (operator) {
            case '|':
                return operand1 || operand2;
            case '&':
                return operand1 && operand2;
            default:
                throw new IllegalArgumentException("Invalid operator: " + operator);
            // Should never be thrown but you never know
        }
    }

    private static boolean hasHigherPrecedence(char operator1, char operator2) {
        // Evaluate operators with equal precedence from left to right
        if (operator1 == operator2) {
            return true;
        } else
            return operator1 == '|' && operator2 == '&';
    }
}
