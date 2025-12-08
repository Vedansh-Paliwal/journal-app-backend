package com.example.journalapp.filter;

import com.example.journalapp.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*
======================  SPRING SECURITY + JWT FULL FLOW  ======================

📌 1. User sends a request (to /signup, /login, /journal, /user, /admin… anything)
The request enters the server and FIRST goes into the Spring Security Filter Chain.
This happens before ANY controller method is executed.

📌 2. My JwtFilter runs BEFORE Spring’s own UsernamePasswordAuthenticationFilter
Because I added:
    .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

So JwtFilter sees EVERY request, whether token exists or not.

📌 3. JwtFilter tries to read the Authorization header:
    Authorization: Bearer <token>

- If header missing → JwtFilter does nothing → pass request forward.
- If header starts with "Bearer " → extract token → extract username from token.

JwtFilter NEVER blocks any request. It only tries to authenticate.

📌 4. If token is found:
JwtFilter:
    - extracts username from JWT (using JwtUtil)
    - loads the full user from DB using UserDetailsServiceImpl
    - validates token expiry + signature
    - creates an Authentication object manually
    - stores it inside SecurityContext:
        SecurityContextHolder.getContext().setAuthentication(authentication)

From this point onward, Spring Security knows:
    ✔ who the user is
    ✔ what roles he has
    ✔ that he is authenticated

📌 5. After ALL filters finish → Spring applies my authorization rules:
    .requestMatchers("/journal/**", "/user/**").authenticated()
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .anyRequest().permitAll()

Meaning:
- If endpoint is protected → Spring checks SecurityContext
- If JwtFilter DID set authentication → allowed
- If JwtFilter did NOT set authentication (no token or invalid token) → rejected

Authorization ALWAYS happens AFTER filters.

📌 6. If authorization succeeds → request finally reaches the Controller
Example:
In /journal controller:
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();

Controller then loads the real user from DB,
gets journal entries, updates user, deletes entry, etc.

📌 7. JWT login (/login) works like this:
- User sends username + password in request body
- AuthenticationManager.authenticate(...) checks:
      username exists?
      BCrypt password matches?
- If yes → a JWT token is generated and returned to client
- Server does NOT store this token anywhere.
  Token is fully self-contained (username, iat, exp, signature).

📌 8. Client stores this JWT and sends it with every future request:
    Authorization: Bearer <token>

JwtFilter validates it on every request.
No session. Completely stateless.

===================== END OF JWT + SPRING SECURITY FLOW =======================
 */

/*
Its ONLY job:
👉 Read the JWT from request
👉 Check if it is valid
👉 If valid → tell Spring Security “User is logged in”
 */
@Component
public class JwtFilter extends OncePerRequestFilter { // OncePerRequestFilter ensures:
//    This filter runs exactly once per request — not twice, not multiple times through forwards.

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    // HttpServletRequest request : This represents the incoming HTTP request(contains everything we send in fetch() in JS).
    // HttpServletResponse response : Represents the response that your server will send back.
    // FilterChain : This represents the chain of filters Spring Security uses.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt); // JWT contains "sub": "vedansh". This line reads that.
        }
        if(username != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username); // I know the username from token. Now let me load full user info from DB.
//            WHY NOT JUST USE UserService.findByUsername() ? Because Spring Security does not work with your custom User class.
//            Spring Security wants its users in a specific internal format, called: UserDetails. Your User class ≠ Spring Security’s UserDetails class.
//            WHY DOES SPRING SECURITY NEED UserDetails?
//            Because Spring Security builds an internal object called:
//            Authentication
//            And for that, it requires:
//            Collection<GrantedAuthority>
//            String username
//            String password (still needed for compatibility)
//            account flags
//            principal object
//            Your custom User class does NOT provide these things.
            if(jwtUtil.validateToken(jwt)) {
//                Because JWT is stateless.
//                You don’t store sessions.
//                You don’t store tokens in DB.
//                The ONLY way to know whether a token is still usable is:
//                      Check signature (done earlier)
//                      Check expiration (being done now)
//                If expired → reject request → user must login again.
                // 3 lines below do 1 thing, tell Spring Security that the user is now logged in.
                // This line below creates an object that tells Spring Security: “This user is LOGGED IN and here are his roles.”
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                /*
                    Create an authentication object using(User is not logging in now, he is already logged in):
                        the user
                        no password (that’s why null)
                        the user’s roles
                */
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                /*
                What information does this add?
                    IP address
                    Session ID
                    Browser info
                    Not mandatory, but useful for Spring logs & auditing.
                    This step simply adds some metadata.
                    Think of it as:
                    “Also include connection details for this login.”
                 */
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // This tells Spring:👉 This user is authenticated. This line tells Spring Security:
                // “From this moment onward, this user is authenticated for this request.”
//                After this line, ANYWHERE in your app:
//                SecurityContextHolder.getContext().getAuthentication() will return:
//                1. who the user is
//                2. roles
//                3. authenticated = true
            }
        }
        filterChain.doFilter(request,response); // This means: “I’m done. Pass the request to the NEXT filter in line.”
    }
}
