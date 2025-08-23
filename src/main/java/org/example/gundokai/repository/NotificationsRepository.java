package org.example.gundokai.repository;


import org.example.gundokai.entity.Notifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationsRepository extends JpaRepository<Notifications, String> {
    @Query("SELECT n FROM Notifications n WHERE n.user.id = :userId")
    List<Notifications> findAllByUserId(String userId);
}
