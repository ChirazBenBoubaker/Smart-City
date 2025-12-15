package com.example.smartcity.metier.service;

import com.example.smartcity.dto.RegisterRequest;

public interface AuthService {
    void registerCitoyen(RegisterRequest request, String appUrl);
    void verifyEmail(String token);

}