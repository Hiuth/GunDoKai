package org.example.gundokai.dto.request;


import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubCategoryCreationRequest {
    @NotBlank(message = "NOT_NULL")
    @Size(min = 2, message ="CATEGORYNAME_INVALID")
    String subCategoryName;
    String description;
}
