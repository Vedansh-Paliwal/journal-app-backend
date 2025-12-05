package com.example.journalapp.config;

import com.example.journalapp.filter.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/* EnableWebSecurity annotation signals spring to enable its web security support. It's used in conjunction with
@Configuration. How is it different from dependency we added in application.properties? Using this annotation, we will be
customizing Spring Security.*/

// <------------- BELOW CODE IS OBSOLETE, IT IS NO LONGER FUNCTIONAL IN SPRING SECURITY 6, SO WE USE SOMETHING ELSE ------------->

//public class SpringSecurity extends WebSecurityConfigureAdapter {
//    /* WebSecurityConfigureAdapter is a utility class in Spring Security framework that provides default configurations and
//    allows customization of certain features. */
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception { This method provides a way to configure how requests are secured.
//        It defines how request matching should be done and what security actions should be applied.
//        http
//            .authorizeRequests()
//                .antMatchers("/hello").permitAll()
//                .anyRequest().authenticated()
//            .and()
//            .formLogin();
//    }
//}

// BASIC AUTHENTICATION, is by design, STATELESS(meaning every time we send a request, we need to send username and password every
// single time we hit an endpoint, implying that latter request has no clue what the former request was). However, some applications
//do mix Basic Authentication with session management for various reasons, so that clients won't need to send Authorization header
//with every request. When you login with Spring Security, it manages your authentication across multiple requests, despite HTTP
//being stateless.

/*
When you log in with Spring Security, it manages your authentication across multiple requests, despite
HTTP being stateless.
1. Session Creation: After successful authentication, an HTTP session is formed. Your authentication
details are stored in this session.
2. Session Cookie: A JSESSIONID cookie is sent to your browser, which gets sent back with subsequent
requests, helping the server recognize your session.
3. SecurityContext: Using the JSESSIONID, Spring Security fetches your authentication details for each
request.
4. Session Timeout: Sessions have a limited life. If you're inactive past this limit, you're logged out.
5. Logout: When logging out, your session ends, and the related cookie is removed.
6. Remember-Me: Spring Security can remember you even after the session ends using a different
persistent cookie (typically have a longer lifespan) .
In essence, Spring Security leverages sessions and cookies, mainly JSESSIONID, to ensure you remain
authenticated across requests.
 */

// <----------------------------------------------------- NEW CODE --------------------------------------------------------->
@Configuration
@EnableWebSecurity
public class SpringSecurity {
    /*
    Annotate a METHOD with @Bean
    This means: “Hey Spring, the object returned by this method should be a bean.”
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    You cannot write:
        @Component
        public class BCryptPasswordEncoder {}

        Because YOU did not create this class.
        It's part of Spring Security, not your code.
        So instead you write:
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    This tells Spring:
    “Create an object of BCryptPasswordEncoder and store it in your IoC container.”
    */

    /*
    This function is NOT called when a client hits an endpoint. It runs once when your Spring Boot application starts.
    NOT when Postman hits /hello, /user, /journal, etc.

    HttpSecurity is NOT the incoming request.
    It’s NOT request data.
    It’s NOT the HTTP request from Postman.

    It is a SETTINGS OBJECT.
    A configuration builder.
    Think of it like:
    “Here Spring, apply these rules whenever ANY request comes.”
    http is like a blank sheet where you define rules for all future requests.
    You are NOT handling a request. You are defining security rules that Spring Security applies internally.
     */

    @Autowired
    private JwtFilter jwtFilter;

    // PART 1 → Authorization (which endpoints need login)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
         /*
            HttpSecurity = main object to apply security rules.
            Here we say:
            - /hello is public (anyone can access)
            - all other endpoints require login (authenticated)
            - use default spring login form (like old .formLogin())
        */
        // Below is the authorization step, and it happens AFTER filters.
        return http
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/journal/**","/user/**").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // All other endpoints require the user to log in
                        .anyRequest().permitAll()
                        // This endpoint is allowed for everyone
                )
//                .formLogin(Customizer.withDefaults())
                // Same as old .formLogin(): gives you a Spring-provided login page
//                .httpBasic(Customizer.withDefaults())
                // Enables Basic Authentication instead of form login, which means Postman must send username/password via Basic Auth header
                .csrf(AbstractHttpConfigurer::disable)
                // Disable CSRF for simplicity (common for APIs)
                .cors(Customizer.withDefaults())
                // It tells Spring Security: "I will provide a CORS configuration bean. Please use it."
                // It does NOT set CORS rules
                // It just activates CORS support.
                // The actual rules (allowed origins, headers, methods) will come from the CORS bean we create next.
                /*
                 A filter is a small piece of code that runs before (and/or after) the controller gets the request.
                 It can inspect, modify, or reject the request.
                 Where filters run (request flow — tiny) :
                 Client → sends HTTP request
                 Spring’s Filter Chain runs filters in order (first one you added, then next, ...).
                 If filters allow, request reaches Controller.
                 Controller handles it and sends response back through filters.
                 So filters run earlier than controllers.

                 Spring Security by default looks for form login / basic auth. It doesn’t automatically read your JWT and set
                 the user in the security context.
                 JwtFilter’s job: read the token from the header, validate it, and if valid create an Authentication and put it
                 into SecurityContextHolder. That way, downstream code (controllers, @PreAuthorize, etc.) sees the user as
                 “logged in”.
                  */
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    /*
    When your Spring Boot app starts:
    Spring creates a blank HttpSecurity configuration object
    It passes that object to your method
    You write rules on it.
    You return a SecurityFilterChain bean
    Spring stores that chain in its security system
    DONE.
    No requests are happening yet.

    When a client sends a request later:
    The system works like:
    Incoming Request
        ↓
    Security Filter Chain (using YOUR rules)
        ↓
    Controller
        ↓
    Service
        ↓
    Repository
    Your function is NOT called here.
    Only the SecurityFilterChain is used.
     */
    /*
    PART 2 → Authentication (how users are loaded + password check)
    What it does:
        Takes username/password from Basic Auth header
        Asks your UserDetailsService to load the user from DB
        Takes PasswordEncoder and checks password
        If OK → creates Authentication object
        Stores it in SecurityContext
        This object is stored in : SecurityContextHolder.getContext().getAuthentication();
        It contains:
        username
        roles
        whether the user is authenticated
        NOTHING SENSITIVE → no password
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    // PART 3 → BCrypt encoder bean
//    BCryptPasswordEncoder() = “Hash passwords before saving. Compare hashed passwords on login.”
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
    CORS is NOT a filter you add manually.
    CORS is NOT part of JWT.
    CORS is NOT part of authentication.
    CORS is simply rules telling Spring Security which frontends are allowed to talk to your backend.
    1️⃣ CorsConfiguration
        This holds the rules:
        setAllowedOrigins() → who can call you
        setAllowedMethods() → GET, POST, PUT, etc.
        setAllowedHeaders() → Content-Type, Authorization
        setAllowCredentials() → whether cookies allowed
    2️⃣ CorsConfigurationSource
        This maps your rules to all your endpoints (“/**”).
        Spring Security reads this bean and applies CORS rules.
    */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 1️⃣ Which frontend origins are allowed to call the backend
        config.setAllowedOrigins(List.of(
                "http://localhost:5500",
                "http://127.0.0.1:5500"
        ));

        // 2️⃣ Which HTTP methods are allowed
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));

        // 3️⃣ Which headers the frontend may send
        config.setAllowedHeaders(List.of("Content-Type", "Authorization"));

        // 4️⃣ Allow cookies / Authorization header (needed for JWT)
        config.setAllowCredentials(true);

        // 5️⃣ Apply this configuration to ALL endpoints (/**)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source; // Contains a mapping of URL pattern → CORS rule. We are returning it to Spring Security itself.
        // More precisely, Spring Security looks for CorsConfiguration bean, when it finds one, it uses it.
    }
}