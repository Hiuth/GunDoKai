package org.example.gundokai.repository;


import org.example.gundokai.entity.Notifications;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationsRepository extends JpaRepository<Notifications, String> {
    List<Notifications> findAllByUserId(String userId);
}
