package org.example.gundokai.mapper;


import org.example.gundokai.dto.request.ProductCreationRequest;
import org.example.gundokai.dto.respone.ProductResponse;
import org.example.gundokai.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toProduct(ProductCreationRequest productCreationRequest);

    @Mapping(target = "subCategoryId", source = "subcategory.id")
    ProductResponse toProductResponse(Product product);
}
