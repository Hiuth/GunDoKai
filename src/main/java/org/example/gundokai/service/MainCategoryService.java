package org.example.gundokai.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gundokai.dto.request.MainCategoryRequest;
import org.example.gundokai.dto.respone.MainCategoryResponse;
import org.example.gundokai.entity.MainCategory;
import org.example.gundokai.exception.AppException;
import org.example.gundokai.exception.ErrorCode;
import org.example.gundokai.mapper.MainCategoryMapper;
import org.example.gundokai.repository.MainCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class MainCategoryService {
    MainCategoryRepository mainCategoryRepository;
    MainCategoryMapper mainCategoryMapper;
    FileStorageService fileStorageService;

    public MainCategoryResponse createMainCategory(MainCategoryRequest mainCategoryRequest, MultipartFile file){
        if(mainCategoryRepository.existsByCategoryName(mainCategoryRequest.getCategoryName())){
            throw new AppException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }
        String categoryImg = fileStorageService.uploadFile(file);
        MainCategory mainCategory = mainCategoryMapper.toMainCategory(mainCategoryRequest);
        mainCategory.setCategoryImg(categoryImg);
        return mainCategoryMapper.toMainCategoryResponse(mainCategoryRepository.save(mainCategory));
    }

    public MainCategoryResponse updateMainCategory(String id,MainCategoryRequest mainCategoryRequest, MultipartFile file){
        MainCategory mainCategory = mainCategoryRepository.findById(id).orElseThrow(
                ()->new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        if(!mainCategoryRequest.getCategoryName().isBlank()){
            mainCategory.setCategoryName(mainCategoryRequest.getCategoryName());
        }
        if (file != null && !file.isEmpty()) {
            fileStorageService.deleteFile(mainCategory.getCategoryImg());
            String categoryImg = fileStorageService.uploadFile(file);
            mainCategory.setCategoryImg(categoryImg);
        }
        return mainCategoryMapper.toMainCategoryResponse(mainCategoryRepository.save(mainCategory));
    }

    public List<MainCategory> GetAllMainCategory(){
        if(mainCategoryRepository.findAll().isEmpty()){
             throw new AppException(ErrorCode.LIST_EMPTY);
         }
         return mainCategoryRepository.findAll();
    }
}
