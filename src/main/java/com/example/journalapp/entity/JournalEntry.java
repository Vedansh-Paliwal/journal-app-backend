package com.example.journalapp.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "journal_entries") //“This Java class represents a document inside a MongoDB collection.”
@Data // Lombok generates all the essential functions(getters, setters, etc.) for us if we write this.
/* ❌ WRONG assumption:
        “@NonNull means title cannot be null and Spring will validate it.”
✔ REAL truth:
        @NonNull is only a Lombok annotation.
        What Lombok does when you put @NonNull:
👉 1. Lombok creates a constructor parameter
👉 2. Lombok adds a null-check inside that generated constructor
👉 3. THAT’S IT.

If you do NOT write any constructor
→ Java automatically creates one
If you write ANY constructor
(even one with 1 argument)
→ Java stops generating the default constructor.
Why?
Because Java assumes:
“The developer knows what they’re doing.
They want to control object creation themselves.”
*/
@NoArgsConstructor
public class JournalEntry {

    @Id // @Id maps to MongoDB’s _id field.
    @JsonSerialize(using = ToStringSerializer.class) // It tells Jackson: “Whenever you convert this ObjectId to JSON, serialize it as STRING using ObjectId.toString().”
    private ObjectId id;

    @NonNull
    private String title;

    private String content;

    private LocalDateTime date;
}
