package org.example.gundokai.mapper;

import org.example.gundokai.dto.request.MainCategoryRequest;
import org.example.gundokai.dto.respone.MainCategoryResponse;
import org.example.gundokai.entity.MainCategory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MainCategoryMapper {
    MainCategory toMainCategory(MainCategoryRequest mainCategoryRequest);

    MainCategoryResponse toMainCategoryResponse(MainCategory mainCategory);
}
