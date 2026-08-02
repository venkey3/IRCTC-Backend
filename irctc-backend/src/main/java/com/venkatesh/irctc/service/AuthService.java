package com.venkatesh.irctc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.venkatesh.irctc.dto.LoginRequest;
import com.venkatesh.irctc.dto.RegisterRequest;
import com.venkatesh.irctc.entity.User;
import com.venkatesh.irctc.repository.UserRepository;
import com.venkatesh.irctc.util.JwtUtil;


@Service
public class AuthService {
	
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	
	@Autowired
	private JwtUtil jwtutil;

	public void register(RegisterRequest request) {
		// TODO Auto-generated method stub
		
		if (userRepository.findByUsername(request.getUsername()).isPresent()) {
           throw new RuntimeException ("Username already exists");
        }
	 
	 User user= new User(
			 request.getUsername(),
			 request.getEmail(),
			 passwordEncoder.encode(request.getPassword()),
			 "ROLE_USER"
			 );
	 
	 userRepository.save(user);
		
	}
	
	public String login(LoginRequest request) {
		User user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> 
		new RuntimeException("Invalid username"));
		
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) throw new RuntimeException("Invalid Password");

		return jwtutil.generateToken(user.getUsername());
				
	}

}
