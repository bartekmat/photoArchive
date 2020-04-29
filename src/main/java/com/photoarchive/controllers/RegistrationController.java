package com.photoarchive.controllers;

import com.photoarchive.exceptions.TokenNotFoundException;
import com.photoarchive.exceptions.UserAlreadyExistsException;
import com.photoarchive.models.RegistrationFormData;
import com.photoarchive.security.User;
import com.photoarchive.security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/register")
public class RegistrationController {

    private UserService userService;
    private PasswordEncoder passwordEncoder;

    @ModelAttribute(name = "registrationFormData")
    private RegistrationFormData registrationFormData(){
        return new RegistrationFormData();
    }

    @Autowired
    public RegistrationController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String showRegistrationPage(){
        return "registration";
    }

    @GetMapping("/token")
    public String processAccountActivation(@RequestParam String value, Model model){
        try {
            userService.activate(value);
        } catch (TokenNotFoundException e) {
            model.addAttribute("invalidToken", e.getMessage());
            return "registration";
        }
        return "redirect:/login";
    }

    @PostMapping
    public String register(RegistrationFormData data, Model model){
        User user = data.toUser(passwordEncoder);
        try {
            userService.register(user);
        } catch (UserAlreadyExistsException e) {
            model.addAttribute("userAlreadyExists", e.getMessage());
            return "registration";
        }
        return "redirect:/login";
    }

}
