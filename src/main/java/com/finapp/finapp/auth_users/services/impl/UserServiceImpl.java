package com.finapp.finapp.auth_users.services.impl;

import com.finapp.finapp.auth_users.dtos.UpdatePasswordRequest;
import com.finapp.finapp.auth_users.dtos.UserDTO;
import com.finapp.finapp.auth_users.entity.User;
import com.finapp.finapp.auth_users.repo.UserRepo;
import com.finapp.finapp.auth_users.services.UserService;
import com.finapp.finapp.aws.S3Service;
import com.finapp.finapp.exceptions.BadRequestException;
import com.finapp.finapp.exceptions.NotFoundException;
import com.finapp.finapp.notification.dtos.NotificationDTO;
import com.finapp.finapp.notification.services.NotificationService;
import com.finapp.finapp.res.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    private final S3Service s3Service;

    private final String uploadDir = "uploads/profile-pictures/";

    @Override
    public User getCurrentLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null) {
            throw new NotFoundException("User not authenticated");
        }
        String email = authentication.getName();
        return userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("User Not Found"));
    }

    @Override
    public Response<UserDTO> getMyProfile() {
        User user = getCurrentLoggedInUser();
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        return Response.<UserDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("User retrieved")
                .data(userDTO)
                .build();
    }

    @Override
    public Response<Page<UserDTO>> getAllUsers(int page, int size) {
        Page<User> users = userRepo.findAll(PageRequest.of(page , size));

        Page<UserDTO> usersDTOS = users.map(user -> modelMapper.map(user, UserDTO.class));

        return Response.<Page<UserDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Users retrieved")
                .data(usersDTOS)
                .build();

    }

    @Override
    public Response<?> updatePassword(UpdatePasswordRequest updatePasswordRequest) {
        User user = getCurrentLoggedInUser();

        String oldPassword = updatePasswordRequest.getOldPassword();
        String newPassword = updatePasswordRequest.getNewPassword();

        if(oldPassword == null || newPassword == null) {
            throw new BadRequestException("old password and new password are required");
        }

        if(!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Old password is not correct");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepo.save(user);

        HashMap<String , Object> vars = new HashMap<>();

        vars.put("name", savedUser.getFirstName());

        //Send a password change confirmation email
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(savedUser.getEmail())
                .subject("Your password was successfully changed")
                .templateName("password-change")
                .templateVariables(vars)
                .build();

        notificationService.sendEmail(notificationDTO, savedUser);

        return Response.builder().message("Password successfully changed").build();
    }

    @Override
    public Response<?> uploadProfilePicture(MultipartFile file) {
        User user = getCurrentLoggedInUser();

        try {
            Path uploadPath = Paths.get(uploadDir);

            if(!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            if(user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                Path oldFile = Paths.get(user.getProfilePictureUrl());

                if(Files.exists(oldFile)) {
                    Files.delete(oldFile);
                }
            }

            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";

            if(originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String newFilename = UUID.randomUUID() + fileExtension;
            Path filePath = uploadPath.resolve(newFilename);

            Files.copy(file.getInputStream(), filePath);

            String fileUrl = uploadDir+newFilename;

            user.setProfilePictureUrl(fileUrl);
            userRepo.save(user);

            return Response.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Profile photo added successfullyy!!")
                    .build();
        } catch(IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Response<?> uploadProfilePictureToS3(MultipartFile file) {
        log.info("inside s3 function");
        User user = getCurrentLoggedInUser();
        try {
            if(user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                s3Service.deleteFile(user.getProfilePictureUrl());
            }

            String s3Url = s3Service.uploadFile(file, "profile-pictures");
            log.info("got the url: " + s3Url);
            user.setProfilePictureUrl(s3Url);
            userRepo.save(user);

            return Response.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Profile photo added successfullyy!!")
                    .build();

        } catch(IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
