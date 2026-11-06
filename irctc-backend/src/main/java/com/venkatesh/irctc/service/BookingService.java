package com.venkatesh.irctc.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import com.venkatesh.irctc.dto.BookingResponse;
import com.venkatesh.irctc.entity.Booking;
import com.venkatesh.irctc.entity.Train;
import com.venkatesh.irctc.entity.TrainSeatAvailability;
import com.venkatesh.irctc.entity.User;
import com.venkatesh.irctc.repository.BookingRepository;
import com.venkatesh.irctc.repository.TrainRepository;
import com.venkatesh.irctc.repository.TrainSeatAvailabilityRepository;
import com.venkatesh.irctc.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class BookingService {
	@Value("${booking.window.days:60}")
	private int bookingWindowDays;
	
	@Autowired
	private TrainSeatAvailabilityRepository seatRepo;
	
	@Autowired
	private BookingRepository bookingRepo;
	
	@Autowired
	private TrainRepository trainRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	/**
	 * @param trainId
	 * @param date
	 * @param seats
	 * @return
	 */
	@Transactional
	public BookingResponse bookTicket(Long trainId, LocalDate date, int seats) {
		
		if (seats <= 0) {
			throw new IllegalArgumentException("Seats must be greater than 0");
		}
		if (date == null) {
			throw new IllegalArgumentException("Travel date is required");
		}
		if (date.isBefore(LocalDate.now())) {
			throw new IllegalArgumentException("Cannot book for past date");
		}
		if (date.isAfter(LocalDate.now().plusDays(bookingWindowDays))) {
			throw new IllegalArgumentException("Travel date is outside the booking window of " + bookingWindowDays + " days");
		}

		TrainSeatAvailability availability =
                seatRepo.findSeatForUpdate(trainId, date);
		
		if(availability == null) {
			throw new RuntimeException(" No availibilty found");
			
		}
		
		if(availability.getAvailableSeats()< seats) {
			throw new RuntimeException(" Not enough seats available");
			
		}
		
		availability.setAvailableSeats(availability.getAvailableSeats() - seats);
		
		seatRepo.save(availability);
		
		Train train =
		        trainRepository.findById(trainId)
		        .orElseThrow(() ->
		             new RuntimeException("Train not found"));
		
		String username= SecurityContextHolder.getContext().getAuthentication().getName();
		
		User user= userRepository.findByUsername(username).orElseThrow(()-> new RuntimeException("User not found"));
		
		
		
		Booking booking = new Booking();
		
		booking.setTrain(train);
		booking.setUser(user);
		booking.setTravelDate(date);
		booking.setSeatsBooked(seats);
		booking.setStatus("CONFIRMED");
		
		Booking savedBooking = bookingRepo.save(booking);
		
		BookingResponse response= new BookingResponse();
		
		response.setBookingId(savedBooking.getId());
		response.setSeatsBooked(savedBooking.getSeatsBooked());
		response.setTrainName(savedBooking.getTrain().getTrainName());
		response.setTrainNumber(savedBooking.getTrain().getTrainNumber());
		response.setTravelDate(savedBooking.getTravelDate());
		response.setStatus(savedBooking.getStatus());
		
		
		return response;
		
	}
	
	public List<BookingResponse> getMyBookings(){
		
		String Username  =  SecurityContextHolder.getContext().getAuthentication().getName();
		
		User user = userRepository.findByUsername(Username).orElseThrow(()-> new RuntimeException("User Not Found"));
		
		List<Booking> bookings = bookingRepo.findByUser(user);
		
		List<BookingResponse> responses = new ArrayList<BookingResponse>();
		
		for(Booking booking : bookings) {
			BookingResponse response = new BookingResponse();
			
			response.setBookingId(booking.getId());
			response.setTrainName(booking.getTrain().getTrainName());
			response.setTrainNumber(booking.getTrain().getTrainNumber());
			response.setTravelDate(booking.getTravelDate());
			response.setStatus(booking.getStatus());
			response.setSeatsBooked(booking.getSeatsBooked());
			
			responses.add(response);
			
		}
		
		return responses;
	}
	
	@Transactional
	public String cancelBooking(Long bookingId) {
		
		Booking booking = bookingRepo.findById(bookingId)
				.orElseThrow(()-> new RuntimeException("Booking not Exist"));
		
		String loggedInUser = SecurityContextHolder.getContext()
				.getAuthentication()
				.getName();
		
		if(!booking.getUser().getUsername().equals(loggedInUser)) {
			throw new RuntimeException("You are not authorized to cancel this booking");
		}
		
		if("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
			throw new RuntimeException("Booking is already Cancelled");
		}
		
		if(booking.getTravelDate().isBefore(LocalDate.now())) {
			throw new RuntimeException("Journey is already completed.Cancellation is not allowed");
		}
		
		TrainSeatAvailability availability = seatRepo.findSeatForUpdate(booking.getTrain().getId(), booking.getTravelDate());
		
		availability.setAvailableSeats(availability.getAvailableSeats() + booking.getSeatsBooked());
		
		seatRepo.save(availability);
		
		booking.setStatus("CANCELLED");
		
		bookingRepo.save(booking);
		
		return " Booking Cancelled Successfully";
		
	}

}
