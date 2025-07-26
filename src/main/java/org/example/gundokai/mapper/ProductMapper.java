package org.example.gundokai.mapper;


import org.example.gundokai.dto.request.ProductCreationRequest;
import org.example.gundokai.dto.respone.ProductResponse;
import org.example.gundokai.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toProduct(ProductCreationRequest productCreationRequest);

    ProductResponse toProductResponse(Product product);
}
