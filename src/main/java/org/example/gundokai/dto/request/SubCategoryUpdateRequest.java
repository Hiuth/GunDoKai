package org.example.gundokai.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubCategoryUpdateRequest {
    String subCategoryName;
    String description;
    String mainCategoryId;
}
