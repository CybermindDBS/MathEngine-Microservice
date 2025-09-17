package com.cdev.mathengine.api.core;

import com.cdev.mathengine.api.annotation.MathAssistEnabled;
import com.cdev.mathengine.api.core.exceptions.InvalidExpressionException;
import com.cdev.mathengine.api.core.utils.UserFunctionLogger;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;

import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ExpressionEvaluator {

    /*
                Example Input (formula for calculating 1000 digits of Pi):
                digits = 1000
                precision = digits + pct(30,digits)         // calculation setting

                trunc(digits, main)         // Calculate this expression to get pi.
                main = 426880 * sqrt(10005) / sum(k,0,(digits/14)),1,var1),
                part1 = (-1)^k * (6*k)! * (545140134*k + 13591409) / part2,
                part2 = (3*k)! * (k!)^3 * 640320^(3*k)
     */

    /*
                Operators:-

                Add: +
                Subtract: -
                Multiply: *
                Divide: /
                Power: ^
                Factorial: !
                Absolute: abs(x)
                Square root: sqrt(x)
                Cube root: cubrt(x)
                Nth root: nthroot(n,x)
                Percentage: pct(n,x)
                Reciprocal: recip(x)
                LogN: logn(n,x)
                Exp: exp(x)

                Functions:-

                Truncate: trunc(n,x)
                Summation: sum(k,0,9,1,x)
                Fibonacci: fib(x)

                Variables:-
                Pi: pi                                // no. of digits as per math context.
                User defined: [a-zA-Z]([0-9])*        // if it follows this regex.

     */

    public static int getNoOfOpenBrackets(String line) {
        int counter = 0;
        String[] characters = line.split("");
        for (String character : characters) {
            if (character.equals("{")) counter++;
            else if (character.equals("}")) counter--;
        }
        return counter;
    }

    public static HashMap<String, Object> parseVariablesAndClasses(String input) {
        HashMap<String, String> userDefinedFunctionClass = new HashMap<>();
        HashMap<String, String> userDefinedFunctions = new HashMap<>();
        HashMap<String, String> variables = new HashMap<>();
        ArrayList<String> imports = new ArrayList<>();

        String[] lines = input.split("\n");
        StringBuilder stringBuilder = null;
        boolean functionBegin = false;
        String functionName = "";
        int openBrackets = 0;
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (functionBegin) {
                stringBuilder.append(line);
                openBrackets += getNoOfOpenBrackets(line);

                if (openBrackets == 0) {
                    if (!userDefinedFunctions.containsKey(functionName))
                        userDefinedFunctions.put(functionName, stringBuilder.toString());
                    functionBegin = false;
                }

            }
            // line matches a function declaration
            else if (line.matches("^.*[(].*[)].*\\{.*$")) {
                String[] tmpArr = line.substring(0, line.indexOf("(")).split(" ");
                functionName = tmpArr[tmpArr.length - 1].trim();
                stringBuilder = new StringBuilder();
                openBrackets = getNoOfOpenBrackets(line);
                stringBuilder.append(line);

                if (line.contains("{") && openBrackets == 0) {
                    if (!userDefinedFunctions.containsKey(functionName))
                        userDefinedFunctions.put(functionName, stringBuilder.toString());
                } else {
                    functionBegin = true;
                }
            } else if (line.contains("=")) {
                String[] equation = line.split("\\s*=\\s*");
                String variableName = equation[0];
                String expression = equation[1];
                variables.put(variableName, expression);
            } else if (line.contains("import")) imports.add(line);
        }

        String className = "UserClass" + new Random().nextInt(1, Integer.MAX_VALUE);
        String importsString = imports.stream().reduce("", (a, b) -> a + "\n" + b);
        userDefinedFunctionClass.put(className, importsString + "\npublic class " + className + " {");
        userDefinedFunctions.values().forEach((functionDefinition -> userDefinedFunctionClass.put(className, userDefinedFunctionClass.get(className) + "\n" + functionDefinition)));
        userDefinedFunctionClass.put(className, userDefinedFunctionClass.get(className) + "\n" + "}");

        HashMap<String, Object> parsedMap = new HashMap<>();
        parsedMap.put("Variables", variables);
        parsedMap.put("UserDefinedClass", userDefinedFunctionClass);
        parsedMap.put("UserDefinedFunctions", userDefinedFunctions);
        parsedMap.put("Imports", imports);
        return parsedMap;
    }

    public HashMap<String, Object> evaluationFallback(String input, MathContext[] mathContext, Throwable ex) throws Throwable {
        throw ex;
    }


    // Entry point.
    @SuppressWarnings("unchecked")
    @MathAssistEnabled
    @CircuitBreaker(name = "evaluation", fallbackMethod = "evaluationFallback")
    public HashMap<String, Object> evaluate(String input, MathContext... mathContext) throws Exception {
        if (input == null || input.isBlank()) throw new InvalidExpressionException("Empty input.");

        System.out.println("Input: >>>" + input + "<<<");

        LocalDateTime calcStartTime = LocalDateTime.now();

        String mainExpr = input.split("\n")[0].trim();
        HashMap<String, Object> parsedMap = parseVariablesAndClasses(input);
        HashMap<String, String> variables = (HashMap<String, String>) parsedMap.get("Variables");
        // Preserves initial expression.
        HashMap<String, String> variablesCopy = new HashMap<>();
        // This way it preserves order.
        variables.entrySet().forEach(entry -> variablesCopy.put(entry.getKey(), entry.getValue()));
        HashMap<String, String> userDefinedClass = (HashMap<String, String>) parsedMap.get("UserDefinedClass");
        HashMap<String, String> userDefinedFunctions = (HashMap<String, String>) parsedMap.get("UserDefinedFunctions");
        ArrayList<String> imports = (ArrayList<String>) parsedMap.get("Imports");

        System.out.println();
        System.out.println("Expr: " + mainExpr);
        System.out.println("Variables: " + variables);
        System.out.println("Imports: " + imports);
        System.out.println("UserDefinedClass: ");
        userDefinedClass.forEach((key, value) -> System.out.println(key + " : " + value));

        // User defined class compilation.
        UserDefinedFunctionClassHandler userDefinedFunctionClassHandler = new UserDefinedFunctionClassHandler();
        String className = null;
        if (userDefinedClass.size() > 0) {
            className = userDefinedClass.keySet().stream().toList().get(0);
            userDefinedFunctionClassHandler.compile(userDefinedClass.get(className));
            System.out.println("User defined class successfully compiled: " + className);
        }
        System.out.println();

        UserFunctionLogger userFunctionLogger = new UserFunctionLogger();
        BigNumber result = evaluate(mainExpr, variables, className, userFunctionLogger, userDefinedFunctionClassHandler, mathContext);
        LocalDateTime calcEndTime = LocalDateTime.now();
        double timeDifference = Duration.between(calcStartTime, calcEndTime).toMillis() / 1000d;
        System.out.println();
        System.out.println(userFunctionLogger);
        System.out.println("Calculation time: " + timeDifference + "s");
        System.out.println("Ans. = " + result);

        HashMap<String, Object> output = new HashMap<>();
        output.put("mainExpr", mainExpr);
        output.put("variables", variablesCopy.entrySet().stream().map((entry) -> entry.getKey() + " = " + entry.getValue()).collect(Collectors.toList()));
        output.put("functions", userDefinedFunctions.entrySet().stream().filter(entry -> userDefinedFunctionClassHandler.calledFunctions.stream().sorted().toList().contains(entry.getKey())).map(Map.Entry::getValue).collect(Collectors.toList()));
        output.put("imports", imports);
        output.put("result", result.toString());
        output.put("logs", userFunctionLogger.toString());
        output.put("timeElapsed", timeDifference + "s");
        return output;
    }

    public BigNumber evaluate(String expr, HashMap<String, String> variables, String userDefinedFunctionClassName, UserFunctionLogger userFunctionLogger, UserDefinedFunctionClassHandler userDefinedFunctionClassHandler, MathContext... mathContext) throws Exception {

        if (variables.containsKey("precision")) {
            String matchContextExpr = variables.get("precision");
            variables.remove("precision");
            mathContext = new MathContext[]{new MathContext((int) Double.parseDouble(evaluate(matchContextExpr, variables, userDefinedFunctionClassName, userFunctionLogger, userDefinedFunctionClassHandler, mathContext).toString()), RoundingMode.HALF_UP)};
        }

        LinkedList<String> terms = new LinkedList<>();

        ExpressionParser ep = new ExpressionParser(expr);
        while (ep.hasNext()) terms.add(ep.next());

        System.out.println("Input parse completed.");

        // Handling Variables
        ListIterator<String> varItr = terms.listIterator();
        while (varItr.hasNext()) {
            String term = varItr.next();
            if (isVariable(term)) {
                if (variables.containsKey(term)) {
                    variables.put(term, evaluate(variables.get(term).trim(), variables, userDefinedFunctionClassName, userFunctionLogger, userDefinedFunctionClassHandler, mathContext).toString());
                    varItr.set(variables.get(term));
                } else if (term.equals("pi"))
                    varItr.set(BigNumber.pi(mathContext).toString());
                else throw new InvalidExpressionException("Variable not defined.");
            }
        }


        // Handling brackets
        ListIterator<String> brcItr = terms.listIterator();
        while (brcItr.hasNext()) {
            String term = brcItr.next();
            if (isWithinBrackets(term)) {
                brcItr.set(evaluate(term.substring(1, term.length() - 1), variables, userDefinedFunctionClassName, userFunctionLogger, userDefinedFunctionClassHandler, mathContext).toString());
            }
        }

        // Handling Factorials
        ListIterator<String> factItr = terms.listIterator();
        while (factItr.hasNext()) {
            String term = factItr.next();
            if (isOperator(term, '!')) {
                String var1;
                factItr.remove();
                var1 = factItr.previous();
                factItr.set(BigNumber.of(var1, mathContext).factorial().toString());
            }
        }

        // Handling Functions
        ListIterator<String> funItr = terms.listIterator();
        while (funItr.hasNext()) {
            String term = funItr.next();
            if (isFunction(term)) {
                String functionName = getFunctionName(term);
                ArrayList<String> args = getFunctionArgs(term);

                ListIterator<String> argItr = args.listIterator();
                if (functionName.equals("sum")) {
                    while (argItr.hasNext()) {
                        if (argItr.nextIndex() == 0 || argItr.nextIndex() == 4)
                            argItr.next();
                        if (argItr.hasNext()) {
                            String arg = argItr.next();
                            String output = evaluate(arg, variables, userDefinedFunctionClassName, userFunctionLogger, userDefinedFunctionClassHandler, mathContext).toString();
                            argItr.set(output);
                        }
                    }
                } else {
                    while (argItr.hasNext()) {
                        String arg = argItr.next();
                        String output = evaluate(arg, variables, userDefinedFunctionClassName, userFunctionLogger, userDefinedFunctionClassHandler, mathContext).toString();
                        argItr.set(output);
                    }
                }

                if (functionName.equals("sqrt")) {
                    funItr.set(BigNumber.of(args.get(0), mathContext).squareRoot().toString());
                } else if (functionName.equals("cubrt")) {
                    funItr.set(BigNumber.of(args.get(0), mathContext).cubeRoot().toString());
                } else if (functionName.equals("nthroot")) {
                    funItr.set(BigNumber.of(args.get(1), mathContext).nthRoot(args.get(0)).toString());
                } else if (functionName.equals("abs")) {
                    funItr.set(BigNumber.of(args.get(0), mathContext).absolute().toString());
                } else if (functionName.equals("recip")) {
                    funItr.set(BigNumber.of(args.get(0), mathContext).reciprocal().toString());
                } else if (functionName.equals("logn")) {
                    funItr.set(BigNumber.of(args.get(1), mathContext).logN(args.get(0)).toString());
                } else if (functionName.equals("exp")) {
                    funItr.set(BigNumber.of(args.get(0), mathContext).exp().toString());
                } else if (functionName.equals("pct")) {
                    funItr.set(BigNumber.of(args.get(1), mathContext).percentage(args.get(0)).toString());
                }

                // Special Functions:
                else if (functionName.equals("trunc")) {
                    funItr.set(Functions.truncate(BigNumber.of(args.get(1), mathContext), Integer.parseInt(args.get(0))).toString());
                } else if (functionName.equals("sum") || functionName.equals("summation")) {
                    funItr.set(Functions.summation(args.get(0), (long) Double.parseDouble(args.get(1)), (long) Double.parseDouble(args.get(2)), (long) Double.parseDouble(args.get(3)), args.get(4), variables, userDefinedFunctionClassName, userFunctionLogger, userDefinedFunctionClassHandler, mathContext).toString());
                } else if (functionName.equals("fib")) {
                    funItr.set(Functions.fibonacci(args.get(0)).toString());
                }

                // User defined functions:
                else {
                    funItr.set(userDefinedFunctionClassHandler.executeUserDefinedFunction(userDefinedFunctionClassName, functionName, args, userFunctionLogger).toString());
                }

            }
        }

        // Handling Power
        ListIterator<String> powItr = terms.listIterator();
        while (powItr.hasNext()) powItr.next();
        while (powItr.hasPrevious()) {
            String term = powItr.previous();
            if (isOperator(term, '^')) {
                String var1, var2;
                powItr.remove();
                var1 = powItr.previous();
                powItr.remove();
                var2 = powItr.next();
                if (var2.equals("-")) {
                    powItr.remove();
                    var2 += powItr.next();
                }
                powItr.set(BigNumber.of(var1, mathContext).power(var2).toString());
            }
        }

        // Handling Multiplication & Division
        ListIterator<String> op1Itr = terms.listIterator();
        while (op1Itr.hasNext()) {
            String term = op1Itr.next();
            if (isOperator(term, '*') || isOperator(term, '/')) {
                String var1, var2;
                op1Itr.remove();
                var1 = op1Itr.previous();
                op1Itr.remove();
                var2 = op1Itr.next();
                if (var2.equals("-")) {
                    op1Itr.remove();
                    var2 += op1Itr.next();
                }
                if (isOperator(term, '/')) {
                    op1Itr.set(BigNumber.of(var1, mathContext).divide(var2).toString());
                } else {
                    op1Itr.set(BigNumber.of(var1, mathContext).multiply(var2).toString());
                }
            }
        }

        // Handling Addition & Subtraction
        ListIterator<String> op2Itr = terms.listIterator();
        while (op2Itr.hasNext()) {
            String term = op2Itr.next();
            if (isOperator(term, '+')) {
                String addend1, addend2;
                op2Itr.remove();
                addend1 = op2Itr.previous();
                op2Itr.remove();
                addend2 = op2Itr.next();
                if (addend2.equals("-")) {
                    op2Itr.remove();
                    addend2 += op2Itr.next();
                    op2Itr.set(BigNumber.of(addend1, mathContext).add(addend2).toString());
                } else
                    op2Itr.set(BigNumber.of(addend1, mathContext).add(addend2).toString());
            }

            if (isOperator(term, '-')) {
                String subtrahend1 = null, subtrahend2;
                op2Itr.remove();
                if (op2Itr.hasPrevious()) {
                    subtrahend1 = op2Itr.previous();
                    op2Itr.remove();
                }
                subtrahend2 = op2Itr.next();
                if (subtrahend1 != null)
                    // to handle testcase: -1 - -1
                    if (subtrahend2.equals("-")) {
                        op2Itr.remove();
                        subtrahend2 += op2Itr.next();
                        op2Itr.set(BigNumber.of(subtrahend1, mathContext).subtract(subtrahend2).toString());
                    } else {
                        op2Itr.set(BigNumber.of(subtrahend1, mathContext).subtract(subtrahend2).toString());
                    }
                else {
                    // To handle this testcase: -4+x
                    op2Itr.set(BigNumber.of(subtrahend2, mathContext).multiply("-1").toString());
                }
            }
        }

        return new BigNumber(terms.get(0), mathContext);
    }

    private boolean isWithinBrackets(String term) {
        return term.charAt(0) == '(' && term.charAt(term.length() - 1) == ')';
    }

    private boolean isVariable(String term) {
        return isAlphabet(term.charAt(0)) && !term.contains("(");
    }

    private boolean isFunction(String term) {
        return isAlphabet(term.charAt(0)) && term.contains("(");
    }

    private String getFunctionName(String term) {
        return term.substring(0, term.indexOf('('));
    }

    public ArrayList<String> getFunctionArgs(String term) {
        ArrayList<String> args = new ArrayList<>();
        int openBracketsCount = 0;
        StringBuilder arg = new StringBuilder();
        for (char ch : term.toCharArray()) {
            if (ch == '(')
                openBracketsCount++;
            else if (ch == ')') openBracketsCount--;


            if (openBracketsCount > 0) {
                if (openBracketsCount > 1) {
                    arg.append(ch);
                } else {
                    if (ch == ',') {
                        args.add(arg.toString().trim());
                        arg.delete(0, arg.length());
                    } else if (ch != '(') arg.append(ch);
                }
            } else {
                if (!arg.isEmpty()) args.add(arg.toString().trim());
            }
        }
        return args;
    }


    private boolean isNumber(char character) {
        return character >= '0' & character <= '9';
    }

    private boolean isAlphabet(char character) {
        return character >= 'a' & character <= 'z' || character >= 'A' & character <= 'Z';
    }

    private boolean isOperator(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '.' || ch == '/' || ch == '^' || ch == '!';
    }

    private boolean isOperator(String term, char ch) {
        return String.valueOf(ch).equals(term);
    }
}