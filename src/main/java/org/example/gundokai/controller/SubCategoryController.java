package org.example.gundokai.controller;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gundokai.dto.request.SubCategoryCreationRequest;
import org.example.gundokai.dto.request.SubCategoryUpdateRequest;
import org.example.gundokai.dto.respone.ApiResponse;
import org.example.gundokai.dto.respone.SubCategoryResponse;
import org.example.gundokai.entity.SubCategory;
import org.example.gundokai.service.SubCategoryService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/subCategory/")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubCategoryController {
    SubCategoryService subCategoryService;

    @PostMapping(value = "/create/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<SubCategoryResponse> createSubCategory(
            @PathVariable("id") String categoryId,
            @RequestParam("subCategoryName") String subCategoryName,
            @RequestParam("description") String description,
            @RequestPart(value ="file") MultipartFile file
    ){
        SubCategoryCreationRequest subCategory = SubCategoryCreationRequest.builder()
                .subCategoryName(subCategoryName)
                .description(description)
                .build();
        ApiResponse<SubCategoryResponse> response = new ApiResponse<>();
        response.setMessage("Sub Category Created: "+ subCategoryName);
        response.setResult(subCategoryService.createSubCategory(categoryId,subCategory,file));
        return response;
    }

    @PutMapping(value = "/update/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<SubCategoryResponse> updateSubCategory(
            @PathVariable("id") String subCategoryId,
            @RequestParam("subCategoryName") String subCategoryName,
            @RequestParam("description") String description,
            @RequestParam("mainCategoryId") String mainCategoryId,
            @RequestPart(value ="file",required = false) MultipartFile file
    ){
        SubCategoryUpdateRequest updateSubCategory = SubCategoryUpdateRequest.builder()
                .subCategoryName(subCategoryName)
                .description(description)
                .mainCategoryId(mainCategoryId)
                .build();
        ApiResponse<SubCategoryResponse> response = new ApiResponse<>();
        response.setMessage("Sub Category Updated: "+ subCategoryName);
        response.setResult(subCategoryService.updateSubCategory(subCategoryId,updateSubCategory,file));
        return response;
    }

    @GetMapping("/getAll/{id}")
    public ApiResponse<List<SubCategory>> getAllSubCategory(@PathVariable("id") String categoryId){
        ApiResponse<List<SubCategory>> response = new ApiResponse<>();
        response.setMessage("Get All Sub Category");
        response.setResult(subCategoryService.getSubCategoryByMainCategoryId(categoryId));
        return response;
    }
}
