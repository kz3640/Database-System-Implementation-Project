package InputHandler;
import java.util.*;

public class BooleanExpressionEvaluator {
    public static boolean evaluate(String expression) {

        //Split the expression into its individual terms
        expression = expression.replaceAll(" or ", "|");
        expression = expression.replaceAll(" and ", "&");
        String[] terms = expression.split("((?<=\\|)|(?=\\|))|((?<=&)|(?=&))");
        //((?<=x)|(?=x)) evaluates to select an empty character before or behind x
        //the big regex thing splits on empty characters before or behind | and &

        for(int i = 0; i < terms.length; i++) {
            //change to char operators, remove whitespace
            terms[i] = terms[i].replaceAll("\\s+", "");
        }

        //TESTER CODE
        for (int i = 0; i != terms.length; i++) {
            System.out.println(terms[i]);
        }
        //TESTER CODE

        //Create stacks to hold the operands and operators
        Stack<Boolean> operandStack = new Stack<>();
        Stack<Character> operatorStack = new Stack<>();

        //Process each term in the expression
        for (String term : terms) {
            if (term.length() == 0) {
                //Skip empty terms
                continue;
            } else if (term.equals("(")) {
                //Push opening parentheses onto the operator stack
                operatorStack.push('(');
            } else if (term.equals(")")) {
                //Process everything in parentheses
                while (operatorStack.peek() != '(') {
                    applyOperator(operandStack, operatorStack);
                }
                operatorStack.pop(); //Pop the opening parenthesis
            } else if (term.equals("|") || term.equals("&")) {
                //Process any operators with higher or equal precedence
                while (!operatorStack.isEmpty() && hasHigherPrecedence(operatorStack.peek(), term.charAt(0))) {
                    applyOperator(operandStack, operatorStack);
                }
                operatorStack.push(term.charAt(0)); //Push the new operator onto the stack
            } else {
                //Parse the operand and push it onto the operand stack
                boolean operand = parseOperand(term);
                operandStack.push(operand);
            }
        }

        //Process any remaining operators
        while (!operatorStack.isEmpty()) {
            applyOperator(operandStack, operatorStack);
        }

        //The final result should be on the top of the operand stack
        return operandStack.pop();
    }

    private static boolean parseOperand(String term) {
        //TODO
        if (term.equals("true")) {
            return true;
        } else if (term.equals("false")) {
            return false;
        } else {
            throw new IllegalArgumentException("Invalid operand: " + term);
        }
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
                //should never be thrown but you never know
        }
    }

    private static boolean hasHigherPrecedence(char operator1, char operator2) {
        //Evaluate operators with equal precedence from left to right
        if (operator1 == operator2) {
            return true;
        } else if (operator1 == '|' && operator2 == '&') {
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) {
        String expression = "false or true and true or true and true or true";
        boolean result = BooleanExpressionEvaluator.evaluate(expression);
        System.out.println(result); // Output: TRUE
    }
}
