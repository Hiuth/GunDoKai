package org.example.gundokai.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gundokai.dto.request.ProductCreationRequest;
import org.example.gundokai.dto.request.ProductUpdateRequest;
import org.example.gundokai.dto.respone.ApiResponse;
import org.example.gundokai.dto.respone.ProductResponse;
import org.example.gundokai.entity.Product;
import org.example.gundokai.service.ProductService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/product/")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    ProductService productService;

    @PostMapping(value = "/create/{subCategoryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductResponse> createProduct(
            @PathVariable("subCategoryId") String subCategoryId,
            @RequestParam("productName") String productName,
            @RequestParam("price") long price,
            @RequestParam("description") String description,
            @RequestParam("stockQuantity") int stockQuantity,
            @RequestPart(value = "file") MultipartFile file
    ){
        ProductCreationRequest productCreationRequest = ProductCreationRequest.builder()
                .productName(productName)
                .price(price)
                .description(description)
                .stockQuantity(stockQuantity)
                .build();
        ApiResponse<ProductResponse> response = new ApiResponse<>();
        response.setMessage("Product Created: "+ productName);
        response.setResult(productService.createProduct(subCategoryId,productCreationRequest,file));
        return response;
    }

    @PutMapping(value = "/update/{productId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable("productId") String productId,
            @RequestParam("productName") String productName,
            @RequestParam("price") long price,
            @RequestParam("description") String description,
            @RequestParam("status") String status,
            @RequestParam("subCategoryId") String subCategoryId,
            @RequestParam("stockQuantity") int stockQuantity,
            @RequestPart(value = "file",required = false) MultipartFile file
    ){
        ProductUpdateRequest productUpdateRequest = ProductUpdateRequest.builder()
                .productName(productName)
                .price(price)
                .description(description)
                .status(status)
                .subCategoryId(subCategoryId)
                .stockQuantity(stockQuantity)
                .build();
        ApiResponse<ProductResponse> response = new ApiResponse<>();
        response.setMessage("Product Updated: "+ productName);
        response.setResult(productService.updateProduct(productId,productUpdateRequest,file));
        return response;
    }

    @GetMapping("/getProduct/{subCategoryId}")
    public ApiResponse<List<Product>> getProductBySubCategory(@PathVariable("subCategoryId") String subCategoryId){
        ApiResponse<List<Product>> response = new ApiResponse<>();
        response.setMessage("Get All Product By Sub Category");
        response.setResult(productService.getProductBySubCategoryId(subCategoryId));
        return response;
    }
}
