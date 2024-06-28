package com.kyunes.user_service.user_service.controller;

import com.kyunes.user_service.user_service.dto.LogInDto;
import com.kyunes.user_service.user_service.dto.SignUpDto;
import com.kyunes.user_service.user_service.dto.TokenDto;
import com.kyunes.user_service.user_service.model.User;
import com.kyunes.user_service.user_service.security.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    UserDetailsManager userDetailsManager;

    @Autowired
    TokenGenerator tokenGenerator;

    @Autowired
    DaoAuthenticationProvider daoAuthenticationProvider;

    @Autowired
    @Qualifier("jwtRefreshTokenAuthProvider")
    JwtAuthenticationProvider refreshTokenAuthProvider;

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody SignUpDto signUpDto) {
        User user = new User(signUpDto.getUsername(), signUpDto.getPassword());
        userDetailsManager.createUser(user);
        Authentication auth = UsernamePasswordAuthenticationToken.authenticated(user, signUpDto.getPassword(), Collections.EMPTY_LIST);

        return ResponseEntity.ok(tokenGenerator.createToken(auth));
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LogInDto loginDto) {
        Authentication auth = daoAuthenticationProvider.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(loginDto.getUsername(), loginDto.getPassword()));

        return ResponseEntity.ok(tokenGenerator.createToken(auth));
    }

    @PostMapping("/token")
    public ResponseEntity token(@RequestBody TokenDto tokenDTO) {
        Authentication authentication = refreshTokenAuthProvider.authenticate(new BearerTokenAuthenticationToken(tokenDTO.getRefreshToken()));
        Jwt jwt = (Jwt) authentication.getCredentials();

        return ResponseEntity.ok(tokenGenerator.createToken(authentication));
    }
}
