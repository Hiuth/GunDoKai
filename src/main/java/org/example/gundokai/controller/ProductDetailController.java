package org.example.gundokai.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gundokai.dto.request.ProductDetailRequest;
import org.example.gundokai.dto.respone.ApiResponse;
import org.example.gundokai.dto.respone.ProductDetailResponse;
import org.example.gundokai.service.ProductDetailService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/productDetail/")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductDetailController {
    ProductDetailService productDetailService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create/{productId}")
    public ApiResponse<ProductDetailResponse> createProductDetail(@Valid @RequestBody ProductDetailRequest productDetailRequest, @PathVariable String productId){
        ApiResponse<ProductDetailResponse> response = new ApiResponse<>();
        response.setMessage("Product Detail Created: "+ productDetailRequest.getManufacturer());
        response.setResult(productDetailService.createProductDetail(productId,productDetailRequest));
        return response;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{productDetailId}")
    public ApiResponse<ProductDetailResponse> updateProductDetail(@Valid @RequestBody ProductDetailRequest productDetailRequest, @PathVariable String productDetailId){
        ApiResponse<ProductDetailResponse> response = new ApiResponse<>();
        response.setMessage("Product Detail Updated: "+ productDetailRequest.getManufacturer());
        response.setResult(productDetailService.updateProductDetail(productDetailId,productDetailRequest));
        return response;
    }

    @GetMapping("/get/{productDetailId}")
    public ApiResponse<ProductDetailResponse> getProductDetail(@PathVariable String productDetailId){
        ApiResponse<ProductDetailResponse> response = new ApiResponse<>();
        response.setMessage("Get Product Detail");
        response.setResult(productDetailService.getProductDetailByProductId(productDetailId));
        return response;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{productDetailId}")
    public ApiResponse<String> deleteProductDetail(@PathVariable String productDetailId){
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Product Detail Deleted");
        response.setResult(productDetailService.deleteProductDetailByProductDetailId(productDetailId));
        return response;
    }
}
