package com.venkatesh.irctc.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;

import com.venkatesh.irctc.service.BookingService;

@RestController
@Validated
@RequestMapping("/booking")
public class BookingController {
	
	@Autowired
	private BookingService bookingService;
	
	@PostMapping
	public ResponseEntity<?> bookTicket(
			@RequestParam Long trainId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam @Positive int seats){
		
		return ResponseEntity.ok(
                bookingService.bookTicket(trainId, date, seats));
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
