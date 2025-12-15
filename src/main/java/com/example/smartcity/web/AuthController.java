package com.example.smartcity.web;

import com.example.smartcity.dto.RegisterRequest;
import com.example.smartcity.metier.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterRequest registerRequest,
                           HttpServletRequest request,
                           Model model) {
        try {
            String appUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            authService.registerCitoyen(registerRequest, appUrl);
            return "redirect:/login?checkEmail";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }



    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/verify")
    public String verify(@RequestParam String token,
                         @RequestParam(required = false) Long notif) {

        authService.verifyEmail(token);
        return "redirect:/login?verified";
    }

}
