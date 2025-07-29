package org.example.gundokai.mapper;

import org.example.gundokai.dto.request.ProductDetailRequest;
import org.example.gundokai.dto.respone.ProductDetailResponse;
import org.example.gundokai.entity.ProductDetail;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductDetailMapper {
    ProductDetail toProductDetail(ProductDetailRequest productDetailRequest);

    ProductDetailResponse toProductDetailResponse(ProductDetail productDetail);
}
