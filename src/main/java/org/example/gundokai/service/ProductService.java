package org.example.gundokai.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gundokai.mapper.ProductMapper;
import org.example.gundokai.repository.ProductRepository;
import org.example.gundokai.repository.SubCategoryRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class ProductService {
    ProductRepository productRepository;
    ProductMapper productMapper;
    FileStorageService fileStorageService;
    SubCategoryRepository subCategoryRepository;


}

