package org.example.gundokai.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @OnDelete(action = OnDeleteAction.SET_NULL)
    MainCategory mainCategory;
}
