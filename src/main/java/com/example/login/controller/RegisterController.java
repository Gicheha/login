package com.example.login.controller;

import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.login.model.User;
import com.example.login.service.EmailService;
import com.example.login.service.UserService;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

@Controller
public class RegisterController {
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserService userService;
    private EmailService emailService;

    @Autowired
    public RegisterController(BCryptPasswordEncoder bCryptPasswordEncoder, UserService userService, EmailService emailService){
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userService = userService;
        this.emailService = emailService;
    }

    //Return registration form template
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView processRegistrationPage(ModelAndView modelAndView, User user){
        modelAndView.addObject("user",user);
        modelAndView.setViewName("register");

        return modelAndView;
    }

    //Process form Input Data
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView processRegistrationForm(ModelAndView modelAndView,@Valid User user, BindingResult bindingResult, HttpServletRequest request){

        //Lookup user in database by e-mail
        User userExists = userService.findByEmail(user.getEmail());

        System.out.println(userExists);

        if(userExists != null){
            modelAndView.addObject("alreadyRegisteredMessage","Oops! This is a user registered.");
            modelAndView.setViewName("register");
            bindingResult.reject("email");
        }

        if(bindingResult.hasErrors()){
            modelAndView.setViewName("register");
        }else{
            //new user so user object created and confirmation email is sent
            user.setEnabled(false);

            //Random String 36-character string token for confirmation link
            user.setConfirmationToken(UUID.randomUUID().toString());

            userService.saveUser(user);

            String appUrl = request.getScheme() + "://" + request.getServerName();

            SimpleMailMessage registrationEmail = new SimpleMailMessage();
            registrationEmail.setTo(user.getEmail());
            registrationEmail.setSubject("Registration Email");
            registrationEmail.setText(("To confirm your e-mail address, please click the link below:\n"
                    + appUrl + "/confirm?token=" + user.getConfirmationToken()));
            registrationEmail.setFrom("noreply@domain.com");

            emailService.sendEmail(registrationEmail);

            modelAndView.addObject("confirmationMessage", "A confirmation e-mail has been sent to " + user.getEmail());
            modelAndView.setViewName("register");

        }

        return modelAndView;

    }



}
