package com.example.journalapp.controller;

import com.example.journalapp.dto.UpdateUserRequest;
import com.example.journalapp.entity.User;
import com.example.journalapp.repository.UserRepository;
import com.example.journalapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

// Controller calls Service, Service calls Repository

// Controllers are basically special type of classes/components which handle our HTTP request
@RestController
@RequestMapping("/user") // Makes the actual path of endpoints below as "/journal/endpoint"
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<Map<String, String>> getUser(Authentication authentication) {
        String username = authentication.getName();

        Map<String, String> response = new HashMap<>();
        response.put("username", username);

        return ResponseEntity.ok(response);
    }

    @PutMapping()
    public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        /*
        authentication.getName()
        → gives the username of the authenticated person
        → but only a STRING, nothing else
         */
        String username = authentication.getName();
        /*
        You STILL need the full user object
        → from MongoDB
        → to update it, encode new password, modify username, etc.
         */
        User userInDB = userService.findByUserName(username);
        // If the user wants to change password (oldPassword provided)
        if (request.getOldPassword() != null && !request.getOldPassword().isEmpty()) {
            // Verify old password
            if (!passwordEncoder.matches(request.getOldPassword(), userInDB.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Old password is incorrect");
            }
        }

        // Update username if provided and not empty
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            userInDB.setUsername(request.getUsername());
        }

        // If a new password is provided → update it
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            // 2️⃣ Encode new password before saving it
            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            userInDB.setPassword(encodedPassword);
        }
        userService.saveUser(userInDB);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        userService.deleteUserAndJournals(username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
