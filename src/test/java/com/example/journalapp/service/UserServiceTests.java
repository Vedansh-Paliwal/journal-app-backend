package com.example.journalapp.service;

import com.example.journalapp.repository.UserRepository;
import io.jsonwebtoken.lang.Strings;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class UserServiceTests {

    @Autowired
    private UserRepository userRepository;

//    @Disabled // When we will run whole class, this test won't be done
    @ParameterizedTest
    @ValueSource(strings = {
            "Ram",
            "Vipul",
            "Shyam"
    })
    public void testFindByUsername(String name) {
//        assertEquals(4, 2+2);
        assertNotNull(userRepository.findByUsername(name));
    }

    @ParameterizedTest
    @CsvSource({
            "1,1,2",
            "2,10,12",
            "3,3,9"
    })
    public void test(int a, int b, int expected) {
        assertEquals(expected, a + b);
    }
}
