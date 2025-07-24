package org.example.gundokai.mapper;

import org.example.gundokai.dto.request.SubCategoryCreationRequest;
import org.example.gundokai.dto.respone.SubCategoryResponse;
import org.example.gundokai.entity.SubCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubCategoryMapper {
    SubCategory toSubCategory(SubCategoryCreationRequest subCategoryCreationRequest);

    @Mapping(source = "mainCategory.id", target = "mainCategoryId")
    SubCategoryResponse toSubCategoryResponse(SubCategory subCategory);
}
