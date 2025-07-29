package org.example.gundokai.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gundokai.dto.request.ProductDetailRequest;
import org.example.gundokai.dto.respone.ProductDetailResponse;
import org.example.gundokai.entity.Product;
import org.example.gundokai.entity.ProductDetail;
import org.example.gundokai.exception.AppException;
import org.example.gundokai.exception.ErrorCode;
import org.example.gundokai.mapper.ProductDetailMapper;
import org.example.gundokai.repository.ProductDetailRepository;
import org.example.gundokai.repository.ProductRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class ProductDetailService {
    ProductDetailRepository productDetailRepository;
    ProductDetailMapper productDetailMapper;
    FileStorageService fileStorageService;
    ProductRepository productRepository;

    public ProductDetailResponse createProductDetail(String productId, ProductDetailRequest productDetailRequest){
        Product product = productRepository.findById(productId).orElseThrow(
                ()-> new AppException(ErrorCode.PRODUCT_NOT_EXISTS)
        );
        ProductDetail productDetail = productDetailMapper.toProductDetail(productDetailRequest);
        productDetail.setProduct(product);
        return productDetailMapper.toProductDetailResponse(productDetailRepository.save(productDetail));
    }

    public ProductDetailResponse updateProductDetail(String productDetailId, ProductDetailRequest productDetailRequest){
        ProductDetail productDetail = productDetailRepository.findById(productDetailId).orElseThrow(
                ()-> new AppException(ErrorCode.PRODUCT_DETAIL_NOT_EXISTS)
        );
       if(!productDetailRequest.getManufacturer().isBlank()){
           productDetail.setManufacturer(productDetailRequest.getManufacturer());
       }
       if(!productDetailRequest.getMaterial().isBlank()){
           productDetail.setMaterial(productDetailRequest.getMaterial());
       }
       if(!productDetailRequest.getRatio().isBlank()){
           productDetail.setRatio(productDetailRequest.getRatio());
       }
       if(!productDetailRequest.getOrigin().isBlank()){
           productDetail.setOrigin(productDetailRequest.getOrigin());
       }
       if(productDetailRequest.getQuantityOfPack() != 0){
           productDetail.setQuantityOfPack(productDetailRequest.getQuantityOfPack());
       }
       if(!productDetailRequest.getHeight().isBlank()){
            productDetail.setHeight(productDetailRequest.getHeight());
       }
       return productDetailMapper.toProductDetailResponse(productDetailRepository.save(productDetail));
    }

    public ProductDetailResponse getProductDetailByProductId(String productId){
        if(!productRepository.existsById(productId)){
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTS);
        }
        return productDetailMapper.toProductDetailResponse(productDetailRepository.findByProductId(productId));
    }

    public String deleteProductDetailByProductDetailId(String productDetailId){
        ProductDetail productDetail = productDetailRepository.findById(productDetailId).orElseThrow(
                ()-> new AppException(ErrorCode.PRODUCT_DETAIL_NOT_EXISTS)
        );
        productDetailRepository.delete(productDetail);
        if (productDetailRepository.existsById(productDetailId)) {
            return "Deleted product detail failed";
        }
        return "Deleted product detail successfully";
    }
}

