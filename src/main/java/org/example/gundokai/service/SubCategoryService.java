package org.example.gundokai.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gundokai.dto.request.SubCategoryCreationRequest;
import org.example.gundokai.dto.request.SubCategoryUpdateRequest;
import org.example.gundokai.dto.respone.SubCategoryResponse;
import org.example.gundokai.entity.MainCategory;
import org.example.gundokai.entity.SubCategory;
import org.example.gundokai.exception.AppException;
import org.example.gundokai.exception.ErrorCode;
import org.example.gundokai.mapper.SubCategoryMapper;
import org.example.gundokai.repository.MainCategoryRepository;
import org.example.gundokai.repository.SubCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class SubCategoryService {
    MainCategoryRepository mainCategoryRepository;
    SubCategoryRepository subCategoryRepository;
    SubCategoryMapper subCategoryMapper;
    FileStorageService fileStorageService;

    public SubCategoryResponse createSubCategory(String categoryId,SubCategoryCreationRequest subCategoryCreationRequest, MultipartFile file) {
        MainCategory main = mainCategoryRepository.findById(categoryId).orElseThrow(
                ()-> new AppException(ErrorCode.CATEGORY_NOT_EXISTS)
        );
        if(subCategoryRepository.existsBySubCategoryName(subCategoryCreationRequest.getSubCategoryName())) {
            throw new AppException(ErrorCode.SUB_CATEGORY_ALREADY_EXISTS);
        }
        String subCategoryImg = fileStorageService.uploadFile(file);
        SubCategory subCategory = subCategoryMapper.toSubCategory(subCategoryCreationRequest);
        subCategory.setSubCategoryImg(subCategoryImg);
        subCategory.setMainCategory(main);
        subCategoryRepository.save(subCategory);
        return subCategoryMapper.toSubCategoryResponse(subCategory);
    }

    public SubCategoryResponse updateSubCategory(String subCategoryId, SubCategoryUpdateRequest updateSubCategoryRequest, MultipartFile file) {
        SubCategory subCategory = subCategoryRepository.findById(subCategoryId).orElseThrow(
                ()-> new AppException(ErrorCode.SUB_CATEGORY_NOT_EXISTS)
        );
        if(!updateSubCategoryRequest.getSubCategoryName().isBlank()){
            if(subCategoryRepository.existsBySubCategoryName(updateSubCategoryRequest.getSubCategoryName())) {
                throw new AppException(ErrorCode.SUB_CATEGORY_ALREADY_EXISTS);
            }
            subCategory.setSubCategoryName(updateSubCategoryRequest.getSubCategoryName());
        }
        if(file !=null && !file.isEmpty()){
            fileStorageService.deleteFile(subCategory.getSubCategoryImg());
            subCategory.setSubCategoryImg(fileStorageService.uploadFile(file));
        }

        if(!updateSubCategoryRequest.getDescription().isBlank()){
            subCategory.setDescription(subCategory.getDescription());
        }

        if(!updateSubCategoryRequest.getMainCategoryId().isBlank()){
            MainCategory main = mainCategoryRepository.findById(updateSubCategoryRequest.getMainCategoryId()).orElseThrow(
                    ()-> new AppException(ErrorCode.CATEGORY_NOT_EXISTS)
            );
            subCategory.setMainCategory(main);
        }
        subCategoryRepository.save(subCategory);
        return subCategoryMapper.toSubCategoryResponse(subCategory);
    }

    public List<SubCategory> getSubCategoryByMainCategoryId(String mainCategoryId) {
        if(!mainCategoryRepository.existsById(mainCategoryId)) {
            throw new AppException(ErrorCode.CATEGORY_NOT_EXISTS);
        }
        return subCategoryRepository.findAllByMainCategoryId(mainCategoryId);
    }
}
