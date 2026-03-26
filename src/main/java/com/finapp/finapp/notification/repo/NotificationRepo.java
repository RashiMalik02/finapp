package com.finapp.finapp.notification.repo;

import com.finapp.finapp.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepo extends JpaRepository<Notification, Long> {
}
