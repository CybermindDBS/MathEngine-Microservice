package dto;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class NewUserAccountDTO {
    @NonNull
    String username;
    @NonNull
    String password;
    @NonNull
    String confirmPassword;
}
