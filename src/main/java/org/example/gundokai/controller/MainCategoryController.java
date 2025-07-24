package org.example.gundokai.controller;

import com.sun.tools.javac.Main;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gundokai.dto.request.MainCategoryRequest;
import org.example.gundokai.dto.respone.ApiResponse;
import org.example.gundokai.dto.respone.MainCategoryResponse;
import org.example.gundokai.service.MainCategoryService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
}
