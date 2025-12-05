package com.example.journalapp.controller;

import com.example.journalapp.entity.User;
import com.example.journalapp.service.UserService;
import com.example.journalapp.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

// Jab bhi kabhi koi springboot/spring application banaye, to ek HealthCheck controller jaroor bana lo.
@RestController
@RequestMapping("/public")
@Slf4j
public class PublicController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/health-check")
    public String HealthCheck() {
        return "OK";
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user){
        try {
            userService.saveNewUser(user);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        catch (Exception e) {
            if (e.getMessage().contains("duplicate key")) {
                return new ResponseEntity<>("Username already exists", HttpStatus.CONFLICT);
            }
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user){
        try {
            /*
            Below statement represents:
           “User is TRYING to login.
            Please check these credentials.”
            This object does NOT mean the user is authenticated yet.
            🚫 It contains:
            No roles
            No authentication status
            No authorities
            Just raw username + raw password
            ✔ AuthenticationManager uses THIS object to:
            Call UserDetailsService
            Fetch user from DB
            Check password using BCrypt
            If correct → authentication SUCCESS, however this doesn't return a UserDetails object for Spring Security
            This is just a login request wrapper.
             */
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
        ); /*
            This line does 3 things internally:
            (1) Calls your UserDetailsServiceImpl.loadUserByUsername()
            Spring Security:
            → “Find user by username.”
            (2) Compares passwords
            It takes:
            raw password from request body
            hashed password from DB
            Then applies BCrypt check:
            BCryptPasswordEncoder.matches(raw, hashed)
            (3) If match → Authentication Success
            If not → throws exception.
           */
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            /*
            UserDetails is:
            1. Not your class
            2. Spring Security’s interface
            3. It contains username, password, roles, isEnabled, etc.
             */
            String jwt = jwtUtil.generateToken(userDetails.getUsername());
            return new ResponseEntity<>(jwt, HttpStatus.OK);
        }
        catch (Exception e){
            log.error("Exception occurred while creating AuthenticationToken ", e);
            return new ResponseEntity<>("Incorrect username or password", HttpStatus.BAD_REQUEST);
        }
    }
}
