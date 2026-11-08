package com.roomsetu.security;

import com.roomsetu.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Now loads the actual user on every request to determine authorities, rather than
 * trusting a claim baked into the JWT at login time. This costs one extra DB read per
 * authenticated request — acceptable at pilot scale, and it means revoking someone's
 * admin status takes effect immediately rather than only after their token expires.
 * If this ever becomes a real bottleneck, cache it (short TTL, a few seconds) rather
 * than reverting to a JWT claim — a stale admin claim is a worse trade than a cache miss.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.isValid(token)) {
                Long userId = jwtUtil.extractUserId(token);

                userRepository.findById(userId).ifPresent(user -> {
                    List<GrantedAuthority> authorities = user.isAdmin()
                            ? List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"))
                            : List.of(new SimpleGrantedAuthority("ROLE_USER"));

                    var auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
                // If the user was deleted/not found, we simply don't set auth —
                // the request falls through as unauthenticated, same as a bad token.
            }
        }
        chain.doFilter(request, response);
    }
}