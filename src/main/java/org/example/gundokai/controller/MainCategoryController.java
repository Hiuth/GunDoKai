package org.example.gundokai.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gundokai.dto.request.MainCategoryRequest;
import org.example.gundokai.dto.respone.ApiResponse;
import org.example.gundokai.dto.respone.MainCategoryResponse;
import org.example.gundokai.entity.MainCategory;
import org.example.gundokai.service.MainCategoryService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/mainCategory/")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MainCategoryController {
    MainCategoryService mainCategoryService;

    @PostMapping(value = "/create",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MainCategoryResponse> createMainCategory(
            @RequestParam("categoryName") String categoryName,
            @RequestPart(value ="file")MultipartFile file){
        MainCategoryRequest request = MainCategoryRequest.builder()
                .categoryName(categoryName)
                .build();
        ApiResponse<MainCategoryResponse> response = new ApiResponse<>();
        response.setMessage("Main Category Created: "+ categoryName);
        response.setResult(mainCategoryService.createMainCategory(request, file));
        return response;
    }

    @PutMapping(value = "/update/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MainCategoryResponse> updateMainCategory(
            @PathVariable("id") String id,
            @RequestParam("categoryName") String categoryName,
            @RequestPart(value ="file",required = false) MultipartFile file){
        MainCategoryRequest request = MainCategoryRequest.builder()
                .categoryName(categoryName)
                .build();
        ApiResponse<MainCategoryResponse> response = new ApiResponse<>();
        response.setMessage("Main Category Updated: "+ categoryName);
        response.setResult(mainCategoryService.updateMainCategory(id, request, file));
        return response;
    }

    @GetMapping("/getAll")
    public ApiResponse<List<MainCategory>> getAllMainCategory(){
        ApiResponse<List<MainCategory>> response = new ApiResponse<>();
        response.setMessage("Get All Main Category");
        response.setResult(mainCategoryService.GetAllMainCategory());
        return response;
    }
}
