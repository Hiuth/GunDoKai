package org.example.gundokai.repository;


import org.example.gundokai.entity.Notifications;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationsRepository extends JpaRepository<Notifications, String> {
}
