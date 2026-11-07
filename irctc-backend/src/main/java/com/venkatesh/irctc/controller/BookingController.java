package com.venkatesh.irctc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.venkatesh.irctc.dto.BookingRequest;
import com.venkatesh.irctc.dto.PaymentRequest;
import com.venkatesh.irctc.service.BookingService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/booking")
public class BookingController {
	
	@Autowired
	private BookingService bookingService;
	
	@PostMapping
	public ResponseEntity<?> bookTicket(@Valid @RequestBody BookingRequest request){
		
		return ResponseEntity.ok(
		        bookingService.initiateBooking(request));
	}

	@PostMapping("/{bookingId}/payment")
	public ResponseEntity<?> processDemoPayment(@PathVariable Long bookingId,
			@Valid @RequestBody PaymentRequest request) {
		return ResponseEntity.ok(bookingService.processDemoPayment(bookingId, request));
	}
	
	@GetMapping("/my-bookings")
	public ResponseEntity<?> getMyBookings(){
		return ResponseEntity.ok(bookingService.getMyBookings());
	}
	
	@PatchMapping("/{bookingId}/cancel")
		public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId){
		
		return ResponseEntity.ok( bookingService.cancelBooking(bookingId));
			
		}
	
	

}
