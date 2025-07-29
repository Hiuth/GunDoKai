package org.example.gundokai.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gundokai.dto.respone.ProductImgResponse;
import org.example.gundokai.entity.Product;
import org.example.gundokai.entity.ProductImg;
import org.example.gundokai.exception.AppException;
import org.example.gundokai.exception.ErrorCode;
import org.example.gundokai.repository.ProductImgRepository;
import org.example.gundokai.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class ProductImgService {
    ProductImgRepository productImgRepository;
    ProductRepository productRepository;
    FileStorageService fileStorageService;

    private ProductImgResponse convertToResponse(ProductImg productImg){
         return ProductImgResponse.builder()
                .id(productImg.getId())
                .productImg(productImg.getProductImg())
                .productId(productImg.getProduct().getId())
                .build();
    }

    public ProductImgResponse createProductImg(String productId, MultipartFile file) {
        Product product = productRepository.findById(productId).orElseThrow(
                ()-> new AppException(ErrorCode.PRODUCT_NOT_EXISTS));
        String productImg = fileStorageService.uploadFile(file);
        return convertToResponse(productImgRepository.save(ProductImg.builder()
                .productImg(productImg)
                .product(product)
                .build()));
    }

    public ProductImgResponse updateProductImg(String productImgId, MultipartFile file) {
        ProductImg productImg = productImgRepository.findById(productImgId).orElseThrow(
                ()-> new AppException(ErrorCode.PRODUCT_IMG_NOT_EXISTS)
        );
        if(file != null && !file.isEmpty()){
            fileStorageService.deleteFile(productImg.getProductImg());
            productImg.setProductImg(fileStorageService.uploadFile(file));
        }
        return convertToResponse(productImgRepository.save(productImg));
    }

    @Transactional
    public String deleteProductImg(String productImgId) {
        ProductImg productImg = productImgRepository.findById(productImgId).orElseThrow(
                ()-> new AppException(ErrorCode.PRODUCT_IMG_NOT_EXISTS));
        fileStorageService.deleteFile(productImg.getProductImg());
        productImgRepository.delete(productImg);
        if (productImgRepository.existsById(productImgId)) {
            return "Deleted product img failed";
        }
        return "Deleted product img successfully";
    }

    public List<ProductImg> getAllProductImg(String productId) {
        if(productRepository.existsById(productId)){
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTS);
        }
        return productImgRepository.findAllByProductId(productId);
    }
}
