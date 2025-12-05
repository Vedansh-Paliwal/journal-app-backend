package com.example.journalapp.service;

import com.example.journalapp.entity.User;
import com.example.journalapp.repository.JournalEntryRepository;
import com.example.journalapp.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    public void saveAdmin(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Arrays.asList("USER", "ADMIN"));
        userRepository.save(user);
    }

    public void saveUser(User user){
        userRepository.save(user);
    }

    public void saveNewUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Arrays.asList("USER"));
        userRepository.save(user);
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public Optional<User> getUsersByID(ObjectId id){
        return userRepository.findById(id);
    }

    public void deleteUsersByID(ObjectId id){
        userRepository.deleteById(id);
    }

    public User findByUserName(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void deleteUserAndJournals(String username) {

        // 1. Get user from DB
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return;
        }
        // 2. Delete all journal entries for this user
        user.getJournalEntries().forEach(journal -> {
            journalEntryRepository.deleteById(journal.getId());
        });

        // 3. Delete the user itself
        userRepository.deleteByUsername(username);
    }
}
