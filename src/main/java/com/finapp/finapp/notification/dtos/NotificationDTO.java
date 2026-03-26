package com.finapp.finapp.notification.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.finapp.finapp.auth_users.entity.User;
import com.finapp.finapp.enums.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationDTO {
    private Long id;

    private String subject;
    @NotBlank(message = "recipient is required")
    private String recipient;

    private String body;

    private NotificationType type;

    private User user;

    private LocalDateTime createdAt;

    private String templateName;
    private Map<String, Object> templateVariables;
}
