package dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class FunctionsDTO {
    @NonNull
    public String functionString;

    public String getFunctions() {
        return functionString;
    }

    public void setFunctions(String functionString) {
        this.functionString = functionString;
    }
}