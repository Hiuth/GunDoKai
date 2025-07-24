package org.example.gundokai.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String subCategoryName;
    String subCategoryImg;

    @Column(columnDefinition = "TEXT")
    String description;
    @ManyToOne
    @JoinColumn(name = "mainCategory_id", referencedColumnName = "id")
    MainCategory mainCategory;
}
