package com.example.login_auth_api.entrypoint.controllers;

import com.example.login_auth_api.entrypoint.dto.LoginRequestDTO;
import com.example.login_auth_api.entrypoint.dto.RegisterRequestDTO;
import com.example.login_auth_api.entrypoint.dto.ResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.login_auth_api.infra.security.TokenService;
import com.example.login_auth_api.repositories.UserRepository;
import com.example.login_auth_api.domain.models.User;

import java.util.Optional;

@SuppressWarnings("rawtypes")
@RestController
@RequestMapping("/auth")
public class AuthController {
  @Autowired
  private final UserRepository repository;
  @Autowired
  private final PasswordEncoder passwordEncoder;
  @Autowired
  private final TokenService tokenService;
  
  public AuthController (
    UserRepository repository, 
    PasswordEncoder passwordEncoder, 
    TokenService tokenService
  ) {
    this.repository = repository;
    this.passwordEncoder = passwordEncoder;
    this.tokenService = tokenService;
  }
  
  @PostMapping("/login")
  public ResponseEntity login(@RequestBody LoginRequestDTO body) {
    User user = this.repository.findByEmail(body.email()).orElseThrow(() -> new RuntimeException(("User not found")));
    
    if (passwordEncoder.matches(body.password(), user.getPassword())) {
      String token = this.tokenService.generateToken(user);
      return ResponseEntity.ok(new ResponseDTO(user, token));
    }
    
    return ResponseEntity.badRequest().build();
  }

  @PostMapping("/register")
  public ResponseEntity register(@RequestBody RegisterRequestDTO body) {
    Optional<User> user = this.repository.findByEmail(body.email());
    
    if (user.isEmpty()) {
      User newUser = new User();
      newUser.setPassword(passwordEncoder.encode(body.password()));
      newUser.setEmail(body.email());
      newUser.setName(body.name());
      this.repository.save(newUser);
      String token = this.tokenService.generateToken(newUser);
      return ResponseEntity.ok(new ResponseDTO(newUser, token));
    }

    return ResponseEntity.badRequest().build();
  }
}
