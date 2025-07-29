package org.example.gundokai.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDetailRequest {
    String manufacturer;
    String material;
    String ratio;
    String origin;
    int quantityOfPack;
    String height;
}
