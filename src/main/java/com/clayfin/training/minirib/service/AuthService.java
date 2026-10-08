package com.clayfin.training.minirib.service;

import com.clayfin.training.minirib.dto.request.LoginRequest;
import com.clayfin.training.minirib.dto.request.RegisterRequest;
import com.clayfin.training.minirib.dto.response.LoginResponse;
import com.clayfin.training.minirib.dto.response.RegisterResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
    RegisterResponse register(RegisterRequest request);
}
