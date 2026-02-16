// package com.exam.library_management.service;

// import com.exam.library_management.entity.User;
// import com.exam.library_management.repository.UserRepository;
// import com.exam.library_management.security.JwtUtil;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Service;

// import java.util.Optional;

// @Service
// public class AuthService {

//     private final UserRepository userRepository;
//     private final PasswordEncoder passwordEncoder;
//     private final JwtUtil jwtUtil;

//     public AuthService(UserRepository userRepository,
//                        PasswordEncoder passwordEncoder,
//                        JwtUtil jwtUtil) {
//         this.userRepository = userRepository;
//         this.passwordEncoder = passwordEncoder;
//         this.jwtUtil = jwtUtil;
//     }

//     public String login(String username, String password) {

//         Optional<User> userOpt = userRepository.findByEmail(username);
//         if (userOpt.isEmpty()) {
//             userOpt = userRepository.findByLibraryId(username);
//         }

//         User user = userOpt.orElseThrow(() ->
//                 new RuntimeException("Invalid username or password"));

//         if (!passwordEncoder.matches(password, user.getPassword())) {
//             throw new RuntimeException("Invalid username or password");
//         }

//         return jwtUtil.generateToken(user.getEmail());
//     }
// }

package com.exam.library_management.service;

import com.exam.library_management.security.JwtUtil;
import com.exam.library_management.dto.LoginRequest;
import com.exam.library_management.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.security.core.AuthenticationException;
import com.exam.library_management.exception.BadRequestException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {

        try{
        // 1️⃣ Delegate authentication to Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 2️⃣ Load authenticated user
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(request.getUsername());

        // 3️⃣ Generate JWT from UserDetails
        String token = jwtUtil.generateToken(userDetails);

        return new LoginResponse(token);
        } catch (AuthenticationException ex){
                throw new BadRequestException("Invalid username or password");
        }
    }
}
