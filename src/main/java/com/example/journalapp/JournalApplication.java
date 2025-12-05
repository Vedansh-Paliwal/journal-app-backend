package com.example.journalapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@PropertySource("classpath:application-secret.properties")
@EnableTransactionManagement // Springboot will create a transactional context(a container) corresponding to all the methods who
// have @Transactional annotation, which will be having all the DB operations related to that method, and they will all be treated
// like one operation, if any one of them fails, all will be rolled back(We are achieving ATOMICITY). All this work is handled
//by PlatformTransactionManager(which is an interface, so it has an implementation named MongoTransactionManager).
public class JournalApplication {
    /* Rest -> Representational State Transfer
    API -> Application Programming Interface
    Rest API = HTTP Verb(GET,PUT,POST,DELETE) + URL*/
    public static void main(String[] args) {
        SpringApplication.run(JournalApplication.class, args);
    }
    /*
    “Create this object and manage it inside Spring’s container, exactly like @Service or @Component.”
    The only difference is:
    @Component is placed on a class
    @Bean is placed on a method that returns an object
    */
    @Bean
    public PlatformTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        /*
        Means: “Please create a MongoTransactionManager object and inject it wherever needed.”
        This object is responsible for:
        starting a transaction
        committing transaction
        rolling back if something fails
        */
        return new MongoTransactionManager(dbFactory);
        /*
        MongoTransactionManager is a Spring class that teaches Spring:
        “How to run a transaction in MongoDB.”
        If you DO NOT create this bean → Spring does not know how to handle @Transactional for MongoDB.
        */
    }
}