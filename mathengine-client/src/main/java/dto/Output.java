package dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class Output {
    String result;
    String logs;
    String error;
    String timeElapsed;

    String mainExpr;
    ArrayList<String> variables;
    ArrayList<String> functions;


    public Output(HashMap<String, Object> output) {
        this.result = (String) output.get("result");
        this.logs = (String) output.get("logs");
        if (logs.isBlank()) logs = null;
        this.timeElapsed = (String) output.get("timeElapsed");

        mainExpr = (String) output.get("mainExpr");
        variables = (ArrayList<String>) output.get("variables");
        functions = (ArrayList<String>) output.get("functions");
    }

    public Output(String error) {
        this.error = error;
    }
}
