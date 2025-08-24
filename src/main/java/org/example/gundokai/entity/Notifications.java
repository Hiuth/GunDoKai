package org.example.gundokai.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notifications {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    User user;

    String message;
    @Column(name = "is_read", nullable = false)
    boolean readOrNot = false;


    LocalDateTime sent_at;
    @PrePersist
    public void prePersist(){
        this.sent_at = LocalDateTime.now();
    }
}
