package org.example.gundokai.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gundokai.dto.request.ProductCreationRequest;
import org.example.gundokai.dto.request.ProductUpdateRequest;
import org.example.gundokai.dto.respone.ApiResponse;
import org.example.gundokai.dto.respone.ProductResponse;
import org.example.gundokai.entity.Product;
import org.example.gundokai.entity.SubCategory;
import org.example.gundokai.exception.AppException;
import org.example.gundokai.exception.ErrorCode;
import org.example.gundokai.mapper.ProductMapper;
import org.example.gundokai.repository.ProductRepository;
import org.example.gundokai.repository.SubCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class ProductService {
    ProductRepository productRepository;
    ProductMapper productMapper;
    FileStorageService fileStorageService;
    SubCategoryRepository subCategoryRepository;

    public ProductResponse createProduct(String subCategoryId, ProductCreationRequest productCreationRequest, MultipartFile file) {
        SubCategory subCategory = subCategoryRepository.findById(subCategoryId).orElseThrow(
                ()-> new AppException(ErrorCode.SUB_CATEGORY_NOT_EXISTS)
        );
        if(productRepository.existsByProductName(productCreationRequest.getProductName())) {
            throw new AppException(ErrorCode.PRODUCT_ALREADY_EXISTS);
        }
        String productImg = fileStorageService.uploadFile(file);
        Product product = productMapper.toProduct(productCreationRequest);
        product.setThumbnail(productImg);
        product.setSubcategory(subCategory);
        return productMapper.toProductResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(String productId, ProductUpdateRequest productUpdateRequest, MultipartFile file){
        Product product = productRepository.findById(productId).orElseThrow(
                ()-> new AppException(ErrorCode.PRODUCT_NOT_EXISTS)
        );
        if(!productUpdateRequest.getProductName().isBlank()){
            if(productRepository.existsByProductName(productUpdateRequest.getProductName())) {
                throw new AppException(ErrorCode.PRODUCT_ALREADY_EXISTS);
            }
            product.setProductName(productUpdateRequest.getProductName());
        }
        if (file != null && !file.isEmpty()) {
            fileStorageService.deleteFile(product.getThumbnail());
            String productImg = fileStorageService.uploadFile(file);
            product.setThumbnail(productImg);
        }
        if(!productUpdateRequest.getDescription().isBlank()){
            product.setDescription(productUpdateRequest.getDescription());
        }
        if(productUpdateRequest.getPrice() != 0){
            product.setPrice(productUpdateRequest.getPrice());
        }

        if(productUpdateRequest.getStockQuantity() != 0){
            product.setStockQuantity(productUpdateRequest.getStockQuantity());
        }
        if(!productUpdateRequest.getStatus().isBlank()){
            product.setStatus(productUpdateRequest.getStatus());
        }
        return productMapper.toProductResponse(productRepository.save(product));
    }

    public List<Product> getProductBySubCategoryId(String subCategoryId){
        if(!subCategoryRepository.existsById(subCategoryId)) {
            throw new AppException(ErrorCode.SUB_CATEGORY_NOT_EXISTS);
        }
        return productRepository.findAllBySubcategory_Id((subCategoryId));
    }
}

