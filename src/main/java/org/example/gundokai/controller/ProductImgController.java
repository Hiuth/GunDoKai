package org.example.gundokai.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gundokai.dto.respone.ApiResponse;
import org.example.gundokai.dto.respone.ProductImgResponse;
import org.example.gundokai.entity.ProductImg;
import org.example.gundokai.service.ProductImgService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/productImg/")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductImgController {
    ProductImgService productImgService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create/{productId}")
    public ApiResponse<ProductImgResponse> createProductImg(@PathVariable String productId, @RequestParam("file") MultipartFile productImg){
        ApiResponse<ProductImgResponse> response = new ApiResponse<>();
        response.setMessage("Product Img Created: "+ productImg);
        response.setResult(productImgService.createProductImg(productId,productImg));
        return response;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/{productImgId}")
    public ApiResponse<ProductImgResponse> updateProductImg(@PathVariable String productImgId, @RequestParam("file") MultipartFile productImg){
        ApiResponse<ProductImgResponse> response = new ApiResponse<>();
        response.setMessage("Product Img Updated: "+ productImg);
        response.setResult(productImgService.updateProductImg(productImgId,productImg));
        return response;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{productImgId}")
    public ApiResponse<String> deleteProductImg(@PathVariable String productImgId){
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Product Img Deleted");
        response.setResult(productImgService.deleteProductImg(productImgId));
        return response;
    }

    @GetMapping("/getAllImg/{productId}")
    public ApiResponse<List<ProductImgResponse>> getProductImg(@PathVariable String productId){
        ApiResponse<List<ProductImgResponse>> response = new ApiResponse<>();
        response.setMessage("Get All Product Img");
        response.setResult(productImgService.getAllProductImg(productId));
        return response;
    }
}
