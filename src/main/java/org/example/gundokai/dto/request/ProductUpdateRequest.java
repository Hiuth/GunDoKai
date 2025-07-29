package org.example.gundokai.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductUpdateRequest {
    String productName;
    Long price;
    String description;
    String subCategoryId;
    String status;
    Integer stockQuantity;
}
