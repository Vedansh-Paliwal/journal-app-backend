package com.example.journalapp.controller;

import com.example.journalapp.entity.JournalEntry;
import com.example.journalapp.entity.User;
import com.example.journalapp.service.JournalEntryService;
import com.example.journalapp.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

// Controller calls Service, Service calls Repository
// Controllers are basically special type of classes/components which handle our HTTP request
@RestController // "Spring, this class handles HTTP requests and returns JSON, not HTML."
@RequestMapping("/journal") // Makes the actual path of endpoints below as "/journal/endpoint"
public class JournalEntryController {

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getAllJournalEntriesOfUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        User user = userService.findByUserName(userName);
        List<JournalEntry> all = user.getJournalEntries();
        if(all != null && !all.isEmpty()){
            return new ResponseEntity<>(all, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping()
    public ResponseEntity<?> createEntry(@RequestBody JournalEntry myEntry) { // It means take data from request and turn it into an object that we can use
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            journalEntryService.saveJournalEntry(myEntry, userName);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/id/{myId}")
    public ResponseEntity<?> getJournalEntryByID(@PathVariable ObjectId myId) {

        // 1. Get logged-in username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 2. Fetch user from DB
        User user = userService.findByUserName(username);

        // 3. Check if this journal ID belongs to the user
        boolean journalBelongsToUser = false;

        for (JournalEntry entry : user.getJournalEntries()) {
            if (entry.getId().equals(myId)) {
                journalBelongsToUser = true;
                break;
            }
        }

        if (!journalBelongsToUser) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // user does NOT own this journal
        }

        // 4. Fetch actual journal entry from DB
        Optional<JournalEntry> journalEntry = journalEntryService.getJournalEntryById(myId);

        if (journalEntry.isPresent()) {
            return new ResponseEntity<>(journalEntry.get(), HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/id/{myId}")
    public ResponseEntity<Void> deleteEntryByID(@PathVariable ObjectId myId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        if(journalEntryService.getJournalEntryById(myId).isPresent()) {
            journalEntryService.deleteJournalEntryById(myId, username);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<?> updateJournalByID(@PathVariable ObjectId id,
                                               @RequestBody JournalEntry updatedEntry) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUserName(username);
        boolean journalBelongsToUser = false;
        for (JournalEntry entry : user.getJournalEntries()) {
            if (entry.getId().equals(id)) {
                journalBelongsToUser = true;
                break;
            }
        }
        if (!journalBelongsToUser) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // user does NOT own this journal
        }
        JournalEntry oldEntry = journalEntryService.getJournalEntryById(id).orElse(null);
        if(oldEntry != null) {
            oldEntry.setTitle(!updatedEntry.getTitle().isEmpty() ? updatedEntry.getTitle() : oldEntry.getTitle());
            oldEntry.setContent(updatedEntry.getContent() != null && !updatedEntry.getContent().isEmpty() ? updatedEntry.getContent() : oldEntry.getContent());
            journalEntryService.saveJournalEntry(oldEntry);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
