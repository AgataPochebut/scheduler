package com.it.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.it.dto.ErrorResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.token.Token;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//our filter. must be been. чтобы сработал @Autowired
@Component
 public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthenticationFilter.class);
    private static final String AUTHORIZATION = "Authorization";

    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserDetailsService userDetailsService;

//    private TokenService tokenService;
//    private UserDetailsService userDetailsService;
//
//    @Autowired
//    public TokenAuthenticationFilter(TokenService tokenService, UserDetailsService userDetailsService) {
//        this.tokenService = tokenService;
//        this.userDetailsService = userDetailsService;
//    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            //get encrypted token from header
            String tokenKey = getToken(request);
            if (tokenKey != null) {
                Token token = tokenService.verifyToken(tokenKey);
                String username = token.getExtendedInformation();

                //get user from username
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                //set an authenticated user
                //либо лог+парль либо userdet+request т.к. в пер
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception exception) {

            String errorMessage = exception.getMessage();

            LOGGER.error(errorMessage, exception);

            ErrorResponseDto errorResponseDTO = new ErrorResponseDto(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getWriter(), errorResponseDTO);
        }

        filterChain.doFilter(request, response);
    }

    //we get string. it's encrypted by MD5 JSON with username and
    private String getToken(HttpServletRequest request) {
        return request.getHeader(AUTHORIZATION);
    }
}