package com.example.journalapp.entity;

import lombok.Data;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "users") //“This Java class represents a document inside a MongoDB collection.”
@Data // Lombok generates all the essential functions(getters, setters, etc.) for us if we write this.
public class User {

    @Id
    private ObjectId id;

    @Indexed(unique = true) //  signifies the creation of a unique index on the specified field(s), however it
    // won't happen automatically so we have to set it true in application.properties.
    // “Please create an index on the username field so searches become fast and ensure it(username) is unique.”
    @NonNull
    private String username;
    @NonNull
    private String password;

    @DBRef // we created a reference of JournalEntry entity
    private List<JournalEntry> journalEntries = new ArrayList<>();
    private List<String> roles;
}
