package com.finapp.finapp;

import com.finapp.finapp.auth_users.entity.User;
import com.finapp.finapp.enums.NotificationType;
import com.finapp.finapp.notification.dtos.NotificationDTO;
import com.finapp.finapp.notification.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@RequiredArgsConstructor
public class FinappApplication {

//	private final NotificationService notificationService;

	public static void main(String[] args) {
		SpringApplication.run(FinappApplication.class, args);
	}

//	@Bean
//	CommandLineRunner runner() {
//		return args -> {
//			NotificationDTO notificationDTO = NotificationDTO.builder()
//					.recipient("rashii.malik02@gmail.com")
//					.subject("testing email")
//					.body("Hello 😀, this is a test email")
//					.type(NotificationType.EMAIL)
//					.build();
//
//			notificationService.sendEmail(notificationDTO, new User());
//		};
}
