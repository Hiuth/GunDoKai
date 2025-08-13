package org.example.gundokai.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ResetPasswordRequest {
    @NotBlank(message = "EMAIL_NOT_BLANK")
    @Email(message = "EMAIL_INVALID")
    String email;

    @NotBlank(message = "CODE_NOT_BLANK")
    String code;

    @NotBlank(message = "PASSWORD_NOT_BLANK")
    @Size(min = 7, message = "PASSWORD_INVALID")
    String newPassword;
}
