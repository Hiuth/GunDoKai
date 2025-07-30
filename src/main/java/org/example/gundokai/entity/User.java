package org.example.gundokai.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String username;

    @Column(unique = true)
    String email;

    String password;

    String gender;

    Integer phoneNumber;

    String fullName;

    LocalDateTime createdAt;

    String code;

    @ManyToMany
    Set<Role> roles;
}