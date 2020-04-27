package com.tokmeninov.springboot1.controllers;

import com.tokmeninov.springboot1.models.Role;
import com.tokmeninov.springboot1.models.User;
import com.tokmeninov.springboot1.models.dto.CaptchaResponseDto;
import com.tokmeninov.springboot1.rep.UserRepo;
import com.tokmeninov.springboot1.service.UserService;
import org.dom4j.rule.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class RegistrationController {
    private final static String CAPTCHA_URL= "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s";
    @Autowired
    private UserService userService;

    @Value("${recaptcha.secret}")
    public String secret;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/registration")
    public String registration(User user){
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(@RequestParam("password2") String passwordConfirm,@RequestParam("g-recaptcha-response") String captchaResponse, @Valid User user, BindingResult bindingResult, Model model){
        String url=String.format(CAPTCHA_URL,secret,captchaResponse);
        CaptchaResponseDto response = restTemplate.postForObject(url, Collections.emptyList(), CaptchaResponseDto.class);
        if(!response.isSuccess()){
            model.addAttribute("captchaError", "Fill captcha");
        }

        boolean isConfirmEmpty= StringUtils.isEmpty(passwordConfirm);
        model.addAttribute("user",user);
        if (isConfirmEmpty){
            model.addAttribute("password2Error","Password confirmation cannot be empty!");
        }
        if(user.getPassword()!=null&& !user.getPassword().equals(passwordConfirm)){
            model.addAttribute("passwordError","Passwords are different!");
        }
        if(isConfirmEmpty || bindingResult.hasErrors() || !response.isSuccess()){

            return "registration";
        }
        if (!userService.addUser(user)){
            model.addAttribute("usernameError", "User exists");
            return "registration";
        }

        return "redirect:/login";
    }

    @GetMapping("/activate/{code}")
    public String activate(Model model, @PathVariable String code){
        boolean isActivated=userService.activateUser(code);
        if(isActivated){
            model.addAttribute("messageType","success");
            model.addAttribute("message","User successfully activated!");
        }else{
            model.addAttribute("messageType","danger");
            model.addAttribute("message","Activation code is not found!");
        }
        return "login";
    }
}
