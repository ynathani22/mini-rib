package com.clayfin.training.minirib.service;

import com.clayfin.training.minirib.dto.request.LoginRequest;
import com.clayfin.training.minirib.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
