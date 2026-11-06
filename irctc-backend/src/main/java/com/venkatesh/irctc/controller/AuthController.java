package com.venkatesh.irctc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.venkatesh.irctc.dto.LoginRequest;
import com.venkatesh.irctc.dto.RegisterRequest;
import com.venkatesh.irctc.entity.User;
import com.venkatesh.irctc.repository.UserRepository;
import com.venkatesh.irctc.service.AuthService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/auth")
public class AuthController {
	
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private AuthService authservice;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@PostMapping("/register")
	public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request){
		
		authservice.register(request);
		
		 
		 return ResponseEntity.ok("User registered successfully");
		 
	
	}
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request){

	    String token = authservice.login(request);

	    return ResponseEntity.ok(token);
	}
}
