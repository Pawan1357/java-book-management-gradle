// package com.exam.library_management.security;

// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
// import org.springframework.web.filter.OncePerRequestFilter;

// import java.io.IOException;

// public class JwtAuthenticationFilter extends OncePerRequestFilter {

//     private final JwtUtil jwtUtil;
//     private final CustomUserDetailsService userDetailsService;

//     public JwtAuthenticationFilter(JwtUtil jwtUtil,
//                                    CustomUserDetailsService userDetailsService) {
//         this.jwtUtil = jwtUtil;
//         this.userDetailsService = userDetailsService;
//     }

//     @Override
//     protected void doFilterInternal(HttpServletRequest request,
//                                     HttpServletResponse response,
//                                     FilterChain filterChain)
//             throws ServletException, IOException {

//         final String authHeader = request.getHeader("Authorization");

//         String username = null;
//         String jwt = null;

//         if (authHeader != null && authHeader.startsWith("Bearer ")) {
//             jwt = authHeader.substring(7);
//             // username = jwtUtil.extractUsername(jwt);
//             try {
//                 username = jwtUtil.extractUsername(jwt);
//             } catch (Exception e) {
//                 response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT");
//                 return;
//             }
//         }

//         if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

//             UserDetails userDetails = userDetailsService.loadUserByUsername(username);

//             if (jwtUtil.validateToken(jwt, userDetails)) {

//                 UsernamePasswordAuthenticationToken authToken =
//                         new UsernamePasswordAuthenticationToken(
//                                 userDetails,
//                                 null,
//                                 userDetails.getAuthorities()
//                         );

//                 authToken.setDetails(
//                         new WebAuthenticationDetailsSource().buildDetails(request)
//                 );

//                 SecurityContextHolder.getContext().setAuthentication(authToken);
//             }
//             else {
//                 response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT");
//                 return;
//             }
//         }

//         filterChain.doFilter(request, response);
//     }
// }

package com.exam.library_management.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        String username;

        try {
            username = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            filterChain.doFilter(request, response); // ⬅️ important
            return;
        }

        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
