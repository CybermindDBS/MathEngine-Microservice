package com.cdev.mathengine;

import com.cdev.mathengine.api.core.ExpressionEvaluator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SpringWebFluxApplication {

    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringApplication.run(SpringWebFluxApplication.class, args);
        ExpressionEvaluator eval = context.getBean(ExpressionEvaluator.class);

//        Using MathEngine to calculate a expression with defined variables and functions.
//        String result = (String) eval.evaluate(
//                // Main expression
//                "calc(5, 2, 10) + (var + var2 * fib(3)) / sqrt(4) * calc2(1, 1, 10) " + "\n" +
//
//                        // Variables
//                        "var = 5 + 1" + "\n" +
//                        "var2 = 5 + 2" + "\n" +
//
//                        // User defined functions
//                        "public static BigNumber calc(int n, double d, BigNumber b) {" + "\n" +
//                        "BigNumber accum = BigNumber.of(0);" + "\n" +
//                        "for(int i = 1; i <= n; i++) {" + "\n" +
//                        "accum = accum.add(BigNumber.of(i).multiply(d).multiply(b));" + "\n" +
//                        "logger.log(\"Iteration: \" + i + \" Value: \" + accum);" + "\n" +
//                        "}" + "\n" +
//                        "return accum;" + "\n" +
//                        "}" + "\n" +
//                        "public static BigNumber calc2(int n, double d, BigNumber b) {" + "\n" +
//                        "BigNumber accum = BigNumber.of(0);" + "\n" +
//                        "for(int i = 1; i <= n; i++) {" + "\n" +
//                        "accum = accum.add(BigNumber.of(i).multiply(d).multiply(b));" + "\n" +
//                        "logger.log(\"Iteration: \" + i + \" Value: \" + accum);" + "\n" +
//                        "}" + "\n" +
//                        "return accum;" + "\n" +
//                        "}"
//
//                , MathContext.DECIMAL128).get("result");
//        System.out.println("Final Output: " + result);

//        Using MatchEngine to calculate pi.
//        HashMap<String, String> variables = new HashMap<>();
//        variables.put("digits", "1000");
//        variables.put("precision", "digits + pct(30, digits)");
//        variables.put("part1", "(-1)^k * (6*k)! * (545140134*k + 13591409) / part2");
//        variables.put("part2", "(3*k)! * (k!)^3 * 640320^(3*k)");
//        BigNumber result = eval.evaluate("trunc(digits, (426880 * sqrt(10005)) / sum(k,0,(digits/14),1,part1))", variables, null);
//        System.out.println(result);

        /*
        trunc(digits, (426880 * sqrt(10005)) / sum(k,0,(digits/14),1,part1))
        digits = 1000
        precision = digits +  pct(30, digits)
        part1 =  (-1)^k * (6*k)! * (545140134*k + 13591409) / part2
        part2 = (3*k)! * (k!)^3 * 640320^(3*k)
         */
    }
}