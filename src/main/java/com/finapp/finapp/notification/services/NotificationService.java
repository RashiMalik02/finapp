package com.finapp.finapp.notification.services;

import com.finapp.finapp.auth_users.entity.User;
import com.finapp.finapp.notification.dtos.NotificationDTO;

public interface NotificationService {
    void sendEmail(NotificationDTO notificationDTO, User user);
}
