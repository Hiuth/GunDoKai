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
public class ProductCreationRequest {
    @NotBlank(message = "NOT_NULL")
    @Size(min = 2, message ="PRODUCTNAME_INVALID")
    String productName;

    @NotBlank(message = "NOT_NULL")
    long price;

    @NotBlank(message = "NOT_NULL")
    int stockQuantity;

    String description;
}
