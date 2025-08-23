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
import jakarta.transaction.Transactional;
import org.example.gundokai.entity.ProductDetail;
import org.example.gundokai.entity.ProductImg;
import org.example.gundokai.repository.ProductDetailRepository;
import org.example.gundokai.repository.ProductImgRepository;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class ProductService {
    ProductRepository productRepository;
    ProductMapper productMapper;
    FileStorageService fileStorageService;
    SubCategoryRepository subCategoryRepository;
    ProductDetailRepository productDetailRepository;
    ProductImgRepository productImgRepository;

    public ProductResponse createProduct(String subCategoryId, ProductCreationRequest productCreationRequest, MultipartFile file) {
        SubCategory subCategory = subCategoryRepository.findById(subCategoryId).orElseThrow(
                ()-> new AppException(ErrorCode.SUB_CATEGORY_NOT_EXISTS)
        );
        if(productRepository.existsByProductName(productCreationRequest.getProductName())) {
            throw new AppException(ErrorCode.PRODUCT_ALREADY_EXISTS);
        }
        String productImg = fileStorageService.uploadFile(file);
        Product product = productMapper.toProduct(productCreationRequest);
        product.setStatus(productCreationRequest.getStatus());
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

    public List<Product> getAllProduct(){
        return productRepository.findAll();
    }

    public ProductResponse getProductByProductId(String productId){
        if(!productRepository.existsById(productId)){
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTS);
        }
        return productMapper.toProductResponse(productRepository.findById(productId).orElseThrow());
    }

    @Transactional
    public String deleteProduct(String productId) {
        Product product = productRepository.findById(productId).orElseThrow(
                ()-> new AppException(ErrorCode.PRODUCT_NOT_EXISTS)
        );

        // Xóa tất cả ProductImg liên quan
        List<ProductImg> productImgs = productImgRepository.findAllByProductId(productId);
        for (ProductImg productImg : productImgs) {
            fileStorageService.deleteFile(productImg.getProductImg());
            productImgRepository.delete(productImg);
        }
        // Xóa ProductDetail liên quan
        ProductDetail productDetail = productDetailRepository.findByProductId(productId);
        if (productDetail != null) {
            productDetailRepository.delete(productDetail);
        }
        // Xóa thumbnail của product
        if (product.getThumbnail() != null) {
            fileStorageService.deleteFile(product.getThumbnail());
        }

        // Xóa product
        productRepository.delete(product);

        if (productRepository.existsById(productId)) {
            return "Deleted product failed";
        }
        return "Deleted product successfully";
    }

    public List<Product> randomProduct(){
        return productRepository.findRandomProducts();
    }

    public Integer getProductCount() {
        return productRepository.findAll().size();
    }
}

