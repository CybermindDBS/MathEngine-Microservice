package com.cdev.mathengine.api.core;

import com.cdev.mathengine.api.core.utils.UserFunctionLogger;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Functions {
    static ExpressionEvaluator eval = new ExpressionEvaluator();

    public static BigNumber summation(String variableName, long start, long end, long step, String expression, HashMap<String, String> variables, String userDefinedFunctionClassName, UserFunctionLogger userFunctionLogger, UserDefinedFunctionClassHandler userDefinedFunctionClassHandler, MathContext... mathContext) throws Exception {
        HashMap<String, String> initialCopyOfVariables = new HashMap<>(variables);
        BigNumber result = BigNumber.of("0", mathContext);
        for (long i = start; (step > 0 ? i <= end : i >= end); i += step) {
            variables = new HashMap<>(initialCopyOfVariables);
            variables.put(variableName, String.valueOf(i));
            BigNumber term = eval.evaluate(expression, variables, userDefinedFunctionClassName, userFunctionLogger, userDefinedFunctionClassHandler, mathContext);
            result = result.add(term);
        }
        return result;
    }

    public static BigNumber fibonacci(Object n) {
        List<String> inputs = parseInputs(n);
        BigNumber nVal = BigNumber.of(inputs.get(0));
        if (nVal.compareTo(BigNumber.of("1")) <= 0) return nVal;
        BigNumber a = BigNumber.of("0"), b = BigNumber.of("1");
        for (BigNumber i = BigNumber.of(2); i.compareTo(nVal) <= 0; i = i.add("1")) {
            BigNumber temp = a.add(b);
            a = b;
            b = temp;
        }
        return b;
    }

    public static BigNumber truncate(BigNumber value, int digits) {
        if (digits <= 0) return BigNumber.of("0");

        String valueStr = value.toString();
        digits = Math.min(digits, valueStr.length());

        int decimalIndex = valueStr.indexOf(".");
        boolean isNegative = valueStr.startsWith("-");

        int increment = 0;
        if (!(decimalIndex == -1 || decimalIndex > digits)) increment++;
        if (isNegative) increment++;

        return new BigNumber(valueStr.substring(0, digits + increment));
    }


    // Utility methods
    public static List<String> parseInputs(Object... objects) {
        List<String> inputs = new ArrayList<>();

        for (Object obj : objects) {
            if (obj instanceof BigNumber val)
                inputs.add(val.toString());
            else if (obj instanceof BigDecimal val)
                inputs.add(val.toPlainString());
            else inputs.add(String.valueOf(obj));
        }

        return inputs;
    }
}
