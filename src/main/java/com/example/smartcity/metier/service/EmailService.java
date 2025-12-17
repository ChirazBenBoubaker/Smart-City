package com.example.smartcity.metier.service;


public interface EmailService {
    void send(String to, String subject, String htmlBody);
}