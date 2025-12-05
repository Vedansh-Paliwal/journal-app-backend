package com.example.journalapp.service;

import com.example.journalapp.entity.User;
import com.example.journalapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username); // If found, then build Spring Security's own internal User object:
        if(user != null){
            // Here, we are returning: “An object of class User, but typed as UserDetails.” So, who implemented UserDetails methods?
            // Spring Security developers already did it in: org.springframework.security.core.userdetails.User
            // This class implements UserDetails.
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())  // username from DB
                    .password(user.getPassword())  // hashed password stored in DB
                    .roles(user.getRoles().toArray(new String[0])) // convert List<String> → array
                    .build();
        }
        /*
        Earlier: You searched for a user only when you needed it.
        Now: Spring Security needs to search for a user automatically when someone tries to log in.
        To do that, Spring calls: loadUserByUsername(username)
         */
        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}
