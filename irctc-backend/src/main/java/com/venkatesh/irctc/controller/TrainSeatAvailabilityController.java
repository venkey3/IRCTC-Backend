package com.venkatesh.irctc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.venkatesh.irctc.dto.SeatAvailabilityRequest;
import com.venkatesh.irctc.entity.TrainSeatAvailability;
import com.venkatesh.irctc.service.TrainSeatAvailabilityService;

@RestController
@RequestMapping("/admin/seats")
public class TrainSeatAvailabilityController {
	
	@Autowired
	private TrainSeatAvailabilityService seatService;
	
	@PostMapping
	public ResponseEntity<?> createSeatAvailbility(@RequestBody SeatAvailabilityRequest request){
		
		TrainSeatAvailability availbility = seatService.createSeatAvailability(request);
		return ResponseEntity.ok(availbility) ;
	}
	
	

}
