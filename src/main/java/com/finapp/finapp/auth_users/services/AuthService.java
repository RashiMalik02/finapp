package com.finapp.finapp.auth_users.services;

import com.finapp.finapp.auth_users.dtos.LoginRequest;
import com.finapp.finapp.auth_users.dtos.LoginResponse;
import com.finapp.finapp.auth_users.dtos.RegisterationRequest;
import com.finapp.finapp.auth_users.dtos.ResetPasswordRequest;
import com.finapp.finapp.res.Response;

public interface AuthService {
    Response<String> register(RegisterationRequest request);
    Response<LoginResponse> login(LoginRequest loginRequest);
    Response<?> forgetPassword(String email);
    Response<?> updatePasswordViaResetCode(ResetPasswordRequest resetPasswordRequest);
}
