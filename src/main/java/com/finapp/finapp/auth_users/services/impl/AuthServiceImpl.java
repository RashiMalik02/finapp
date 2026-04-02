package com.finapp.finapp.auth_users.services.impl;

import com.finapp.finapp.account.entity.Account;
import com.finapp.finapp.auth_users.CodeGenerator;
import com.finapp.finapp.auth_users.dtos.LoginRequest;
import com.finapp.finapp.auth_users.dtos.LoginResponse;
import com.finapp.finapp.auth_users.dtos.RegisterationRequest;
import com.finapp.finapp.auth_users.dtos.ResetPasswordRequest;
import com.finapp.finapp.auth_users.entity.PasswordResetCode;
import com.finapp.finapp.auth_users.entity.User;
import com.finapp.finapp.auth_users.repo.PasswordResetCodeRepo;
import com.finapp.finapp.auth_users.repo.UserRepo;
import com.finapp.finapp.auth_users.services.AuthService;
import com.finapp.finapp.enums.AccountType;
import com.finapp.finapp.enums.Currency;
import com.finapp.finapp.exceptions.BadRequestException;
import com.finapp.finapp.exceptions.NotFoundException;
import com.finapp.finapp.notification.dtos.NotificationDTO;
import com.finapp.finapp.notification.services.NotificationService;
import com.finapp.finapp.res.Response;
import com.finapp.finapp.role.entity.Role;
import com.finapp.finapp.role.repo.RoleRepo;
import com.finapp.finapp.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    private final CodeGenerator codeGenerator;
    private final PasswordResetCodeRepo passwordResetCodeRepo;

    @Value("${password.reset.link}")
    private String resetLink;

    @Override
    public Response<String> register(RegisterationRequest request) {
        List<Role> roles;

        if(request.getRoles() == null || request.getRoles().isEmpty()) {
            Role defaultRole = roleRepo.findByName("CUSTOMER")
                    .orElseThrow(() -> new NotFoundException("CUSTOMER role not found"));

            roles = Collections.singletonList(defaultRole);
        } else {
            roles = request.getRoles().stream()
                    .map(roleName-> roleRepo.findByName(roleName)
                            .orElseThrow(() -> new NotFoundException("Role not found: " + roleName)))
                    .toList();

        }

        if(userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("user account with this email already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .active(true)
                .build();

        User savedUser = userRepo.save(user);

//        //autogenerate an account number for the user
//        Account savedAccount = accountService.save(AccountType.SAVINGS, savedUser);

        HashMap<String , Object> vars = new HashMap<>();

        vars.put("name", savedUser.getFirstName());

        //Send a welcome email
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(savedUser.getEmail())
                .subject("Welcome to the Finance Bank!!")
                .templateName("welcome")
                .templateVariables(vars)
                .build();

        notificationService.sendEmail(notificationDTO, savedUser);

        //send an account creation/details email
        HashMap<String , Object> accountVars = new HashMap<>();

        accountVars.put("name", savedUser.getFirstName());
//        accountVars.put("accountNumber", savedAccount.getAccountNumber());
        accountVars.put("accountType", AccountType.SAVINGS.name());
        accountVars.put("currency", Currency.USD);

        NotificationDTO accountDetailsEmail = NotificationDTO.builder()
                .recipient(savedUser.getEmail())
                .subject("Congratulations! your new account has been created")
                .templateName("account-created")
                .templateVariables(accountVars)
                .build();

        notificationService.sendEmail(accountDetailsEmail, savedUser);

        return Response.<String>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Your account has been created successfully")
//                .data("Email of your account details has been sent to you! Your Account Number is: " + savedAccount.getAccountNumber())
                .build();

    }

    @Override
    public Response<LoginResponse> login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User user = userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("Email not found!"));

        if(!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Password does not match!");
        }

        String token = tokenService.generateToken(email);

        LoginResponse loginResponse = LoginResponse.builder()
                .token(token)
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .build();

        return Response.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Logged in successfully")
                .data(loginResponse)
                .build();
    }

    @Override
    @Transactional
    public Response<?> forgetPassword(String email) {
        User user = userRepo.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found!"));
        passwordResetCodeRepo.deleteByUserId(user.getId());

        String code = codeGenerator.generateUniqueCode();

        PasswordResetCode resetCode = PasswordResetCode.builder()
                .code(code)
                .user(user)
                .used(false)
                .expiryDate(calculateExpiryDate())
                .build();

        passwordResetCodeRepo.save(resetCode);

        //send reset email link
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", user.getFirstName());
        templateVariables.put("resetLink", resetLink + code);

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject("Password Reset Code")
                .templateName("password-reset")
                .templateVariables(templateVariables)
                .build();

        notificationService.sendEmail(notificationDTO, user);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Reset Code sent to your email")
                .build();

    }

    @Override
    @Transactional
    public Response<?> updatePasswordViaResetCode(ResetPasswordRequest resetPasswordRequest) {
        String resetCode = resetPasswordRequest.getCode();
        String newPassword = resetPasswordRequest.getNewPassword();

        PasswordResetCode passwordResetCode = passwordResetCodeRepo.findByCode(resetCode)
                .orElseThrow(() -> new BadRequestException("Invalid code"));
        //check expiration
        if(passwordResetCode.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetCodeRepo.delete(passwordResetCode);  //remove expired code
            throw new BadRequestException("reset code is expired");
        }

        //update the password
        User user = passwordResetCode.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        //Delete the code immediately after successful update
        passwordResetCodeRepo.delete(passwordResetCode);

        //send confirmation email
        Map<String, Object> templateVar = new HashMap<>();

        templateVar.put("name", user.getFirstName());

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject("password updated successfully!")
                .templateName("password-update-confirmation")
                .templateVariables(templateVar)
                .build();
        notificationService.sendEmail(notificationDTO, user);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("password updated successfully!")
                .build();
    }

    private LocalDateTime calculateExpiryDate() {
        return LocalDateTime.now().plusHours(5);
    }
}
