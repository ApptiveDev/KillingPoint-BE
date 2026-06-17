package apptive.team5.global.filter;

import apptive.team5.global.exception.NotFoundEntityException;
import apptive.team5.jwt.TokenType;
import apptive.team5.jwt.component.JWTUtil;
import apptive.team5.user.domain.UserEntity;
import apptive.team5.user.service.UserLowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UserLowService userLowService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authorization.startsWith("Bearer ")) {
            throw new ServletException("Invalid JWT Token");
        }

        String accessToken = authorization.split(" ")[1];

        if (!jwtUtil.validateToken(accessToken, TokenType.ACCESS_TOKEN)) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            Map<String, String> message = Map.of("message", "Invalid Token");
            String invalidTokenMessage = objectMapper.writeValueAsString(message);
            response.getWriter().write(invalidTokenMessage);
            return;
        }

        Claims claims = jwtUtil.getClaims(accessToken);
        Long userId = Long.valueOf(claims.get("userId").toString());

        try {
            UserEntity findUser = userLowService.findById(userId);
            if (findUser.isLocked()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                Map<String, String> message = Map.of("message", "Locked User");
                String invalidTokenMessage = objectMapper.writeValueAsString(message);
                response.getWriter().write(invalidTokenMessage);
                return;
            }
            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_"+findUser.getRoleType().name()));
            Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        } catch (NotFoundEntityException ex) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }

    }
}
