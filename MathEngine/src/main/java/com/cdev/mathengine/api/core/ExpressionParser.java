package com.cdev.mathengine.api.core;

import com.cdev.mathengine.api.core.exceptions.InvalidExpressionException;

public class ExpressionParser {
    int index;
    StringBuilder expr;

    public ExpressionParser(String expr) {
        this.expr = new StringBuilder(expr.trim());
        this.index = 0;
    }

    /*
            [' '(' ')*]         // space
            [0-9]               // numbers
            [a-zA-Z0-9]         // variables
            [a-zA-Z0-9(]        // functions
            (                   // opening bracket
            )                   // closing bracket
            [+ - * / ^ !]     // operators

            function terminates with ')' and variable terminates with ' ' or operators
     */

    public String next() throws Exception {
        StringBuilder term = new StringBuilder();
        boolean foundBeginning = false, beginsWithBrackets = false, isFunction = false;
        int start = index;
        int bracketsWithinBracket = 0;

        while (true) {
            if (index >= expr.length()) {
                System.out.println("R: " + term);
                return term.toString();
            }

            char charAtIndex = expr.charAt(index);

            if (foundBeginning) {

                if (beginsWithBrackets) {
                    if (charAtIndex == ')') {
                        term.append(charAtIndex);
                        System.out.println(term);
                        bracketsWithinBracket--;
                        index++;
                        if (bracketsWithinBracket == -1) {
                            System.out.println("R: " + term);
                            return term.toString();
                        }
                        if (index >= expr.length())
                            throw new InvalidExpressionException("Bracket not closed properly.");

                    } else if (charAtIndex == '(') {
                        term.append(charAtIndex);
                        System.out.println(term);
                        bracketsWithinBracket++;
                        index++;
                    } else {
                        term.append(charAtIndex);
                        System.out.println(term);
                        index++;
                    }
                } else {
                    char charAtStart = expr.charAt(start);
                    if (isNumber(charAtStart)) {
                        if (isNumber(charAtIndex) || charAtIndex == '.') {
                            term.append(charAtIndex);
                            System.out.println(term);
                            index++;
                        } else {
                            System.out.println("R: " + term);
                            return term.toString();
                        }
                    }
                        /*
                                    abs4(-2*2)4
                                    abd4            // function terminates with ')' and variable terminates with ' ' and operators
                         */
                    else if (isAlphabet(charAtStart)) {
                        if (isAlphabet(charAtIndex) || isNumber(charAtIndex)) {
                            term.append(charAtIndex);
                            System.out.println(term);
                            index++;
                        } else if (charAtIndex == '(' && !isFunction) {
                            term.append(charAtIndex);
                            System.out.println(term);
                            index++;
                            isFunction = true;
                        } else if (isFunction) {
                            if (charAtIndex == '(') {
                                bracketsWithinBracket++;
                                index++;
                                term.append(charAtIndex);
                                System.out.println(term);
                            } else if (charAtIndex == ')') {
                                term.append(charAtIndex);
                                System.out.println(term);
                                bracketsWithinBracket--;
                                index++;
                                if (bracketsWithinBracket == -1) {
                                    System.out.println("R: " + term);
                                    return term.toString();
                                }
                            } else {
                                if (isNumber(charAtIndex) || isAlphabet(charAtIndex) || isOperator(charAtIndex) || charAtIndex == '.' || charAtIndex == ',') {
                                    term.append(charAtIndex);
                                    System.out.println(term);
                                    index++;
                                } else if (charAtIndex == ' ') index++;
                            }
                        } else {
                            if (charAtIndex == ' ' || isOperator(charAtIndex)) {
                                System.out.println("R: " + term);
                                return term.toString();
                            } else if (charAtIndex == ')') {
                                // Test this.
                                throw new InvalidExpressionException("Bracket not closed properly.");
                            }
                        }
                    } else if (isOperator(charAtStart)) {
                        term.append(charAtIndex);
                        index++;
                        System.out.println(term);
                        System.out.println("R: " + term);
                        return term.toString();
                    }
                }


            } else {
                if (charAtIndex == ' ') index++;
                else if (charAtIndex == '(') {
                    term.append(charAtIndex);
                    start = index;
                    beginsWithBrackets = true;
                    foundBeginning = true;
                    index++;
                } else if (isNumber(charAtIndex)) {
                    start = index;
                    foundBeginning = true;
                } else if (isAlphabet(charAtIndex)) {
                    start = index;
                    foundBeginning = true;
                } else if (isOperator(charAtIndex)) {
                    start = index;
                    foundBeginning = true;
                } else if (charAtIndex == ')') throw new InvalidExpressionException("Bracket not closed properly.");
                else throw new InvalidExpressionException("Not a valid expression.");
            }
        }

    }

    public boolean hasNext() {
        return index < expr.length();
    }

    private boolean isNumber(char character) {
        return character >= '0' & character <= '9';
    }

    private boolean isAlphabet(char character) {
        return character >= 'a' & character <= 'z' || character >= 'A' & character <= 'Z';
    }

    private boolean isOperator(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '^' || ch == '!';
    }
}
