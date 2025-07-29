package org.example.gundokai.dto.respone;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDetailResponse {
    String id;
    String manufacturer;
    String material;
    String ratio;
    String origin;
    int quantityOfPack;
    String height;
    String productId;
}
