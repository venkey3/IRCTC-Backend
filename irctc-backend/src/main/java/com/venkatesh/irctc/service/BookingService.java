package com.venkatesh.irctc.service;

import java.time.LocalDate;
import java.security.SecureRandom;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import com.venkatesh.irctc.dto.BookingResponse;
import com.venkatesh.irctc.dto.BookingRequest;
import com.venkatesh.irctc.dto.PassengerRequest;
import com.venkatesh.irctc.dto.PassengerResponse;
import com.venkatesh.irctc.dto.PaymentRequest;
import com.venkatesh.irctc.entity.Booking;
import com.venkatesh.irctc.entity.Passenger;
import com.venkatesh.irctc.entity.Payment;
import com.venkatesh.irctc.entity.TravelClass;
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
	private static final SecureRandom PNR_RANDOM = new SecureRandom();
	private static final int SEATS_PER_COACH = 72;
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
		return createBooking(trainId, date, seats, Collections.emptyList());
	}

	@Transactional
	public BookingResponse bookTicket(BookingRequest request) {
		if (request == null || request.getPassengers() == null || request.getPassengers().isEmpty()) {
			throw new IllegalArgumentException("At least one passenger is required");
		}
		return createBooking(request.getTrainId(), request.getTravelDate(),
				request.getPassengers().size(), request.getPassengers());
	}

	@Transactional
	public BookingResponse initiateBooking(BookingRequest request) {
		if (request == null || request.getPassengers() == null || request.getPassengers().isEmpty()) {
			throw new IllegalArgumentException("At least one passenger is required");
		}
		validateTravelDate(request.getTravelDate());
		Train train = trainRepository.findById(request.getTrainId())
				.orElseThrow(() -> new IllegalArgumentException("Train not found"));
		TravelClass travelClass = request.getTravelClass() == null ? TravelClass.SLEEPER : request.getTravelClass();
		if (train.getSeatsForClass(travelClass) <= 0) throw new IllegalStateException("This train has no " + travelClass + " seats configured");
		TrainSeatAvailability availability = seatRepo.findSeatForUpdate(request.getTrainId(), request.getTravelDate(), travelClass);
		if (availability == null || availability.getAvailableSeats() < request.getPassengers().size()) {
			throw new IllegalStateException("Not enough seats are available for this booking");
		}
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		Booking booking = new Booking();
		booking.setTrain(train);
		booking.setUser(user);
		booking.setTravelDate(request.getTravelDate());
		booking.setSeatsBooked(request.getPassengers().size());
		booking.setStatus("PENDING_PAYMENT");
		booking.setPnr(generatePnr());
		booking.setTravelClass(travelClass);
		for (PassengerRequest passengerRequest : request.getPassengers()) {
			Passenger passenger = new Passenger();
			passenger.setFullName(passengerRequest.getFullName().trim());
			passenger.setAge(passengerRequest.getAge());
			passenger.setGender(passengerRequest.getGender());
			booking.addPassenger(passenger);
		}

		Payment payment = new Payment();
		payment.setAmount(train.getFareForClass(travelClass).multiply(BigDecimal.valueOf(request.getPassengers().size())));
		payment.setStatus("PENDING");
		payment.setPaymentMethod("DEMO_CARD");
		booking.setPayment(payment);
		return toResponse(bookingRepo.save(booking));
	}

	@Transactional
	public BookingResponse processDemoPayment(Long bookingId, PaymentRequest request) {
		Booking booking = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new IllegalArgumentException("Booking not found"));
		ensureBookingOwner(booking);
		Payment payment = booking.getPayment();
		if (payment == null || !"PENDING".equals(payment.getStatus())
				|| !"PENDING_PAYMENT".equals(booking.getStatus())) {
			throw new IllegalStateException("This booking is not awaiting payment");
		}
		if (!"DEMO_CARD".equals(request.getPaymentMethod()) && !"DEMO_UPI".equals(request.getPaymentMethod())) {
			throw new IllegalArgumentException("Choose a supported demo payment method");
		}
		payment.setPaymentMethod(request.getPaymentMethod());
		if (!request.isSuccessful()) {
			payment.setStatus("FAILED");
			booking.setStatus("PAYMENT_FAILED");
			return toResponse(bookingRepo.save(booking));
		}

		TrainSeatAvailability availability = seatRepo.findSeatForUpdate(booking.getTrain().getId(), booking.getTravelDate(), booking.getTravelClass());
		if (availability == null || availability.getAvailableSeats() < booking.getSeatsBooked()) {
			payment.setStatus("FAILED");
			booking.setStatus("PAYMENT_FAILED");
			bookingRepo.save(booking);
			return toResponse(booking);
		}
		assignSeats(booking, availability);
		availability.setAvailableSeats(availability.getAvailableSeats() - booking.getSeatsBooked());
		seatRepo.save(availability);
		payment.setStatus("SUCCESS");
		payment.setTransactionReference("DEMO-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
		payment.setPaidAt(LocalDateTime.now());
		booking.setStatus("CONFIRMED");
		return toResponse(bookingRepo.save(booking));
	}

	private BookingResponse createBooking(Long trainId, LocalDate date, int seats,
			List<PassengerRequest> passengerRequests) {
		
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
				seatRepo.findSeatForUpdate(trainId, date, TravelClass.SLEEPER);
		
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
		booking.setPnr(generatePnr());
		booking.setTravelClass(TravelClass.SLEEPER);
		for (PassengerRequest passengerRequest : passengerRequests) {
			Passenger passenger = new Passenger();
			passenger.setFullName(passengerRequest.getFullName().trim());
			passenger.setAge(passengerRequest.getAge());
			passenger.setGender(passengerRequest.getGender());
			booking.addPassenger(passenger);
		}
		
		Booking savedBooking = bookingRepo.save(booking);
		return toResponse(savedBooking);
	}
	
	@Transactional
	public List<BookingResponse> getMyBookings(){
		
		String Username  =  SecurityContextHolder.getContext().getAuthentication().getName();
		
		User user = userRepository.findByUsername(Username).orElseThrow(()-> new RuntimeException("User Not Found"));
		
		List<Booking> bookings = bookingRepo.findByUser(user);
		
		return bookings.stream().map(this::toResponse).toList();
	}

	private BookingResponse toResponse(Booking booking) {
		BookingResponse response = new BookingResponse();
		response.setBookingId(booking.getId());
		response.setTrainName(booking.getTrain().getTrainName());
		response.setTrainNumber(booking.getTrain().getTrainNumber());
		response.setTravelDate(booking.getTravelDate());
		response.setStatus(booking.getStatus());
		response.setPnr(booking.getPnr());
		response.setTravelClass(booking.getTravelClass());
		if (booking.getPayment() != null) {
			response.setPaymentStatus(booking.getPayment().getStatus());
			response.setPaymentAmount(booking.getPayment().getAmount());
			response.setPaymentMethod(booking.getPayment().getPaymentMethod());
			response.setTransactionReference(booking.getPayment().getTransactionReference());
			response.setPaidAt(booking.getPayment().getPaidAt());
			response.setRefundReference(booking.getPayment().getRefundReference());
			response.setRefundedAt(booking.getPayment().getRefundedAt());
		}
		response.setSeatsBooked(booking.getSeatsBooked());
		response.setPassengers(booking.getPassengers().stream().map(passenger -> {
			PassengerResponse passengerResponse = new PassengerResponse();
			passengerResponse.setPassengerId(passenger.getId());
			passengerResponse.setFullName(passenger.getFullName());
			passengerResponse.setAge(passenger.getAge());
			passengerResponse.setGender(passenger.getGender());
			passengerResponse.setCoachNumber(passenger.getCoachNumber());
			passengerResponse.setSeatNumber(passenger.getSeatNumber());
			return passengerResponse;
		}).toList());
		return response;
	}

	private String generatePnr() {
		String pnr;
		do {
			pnr = Long.toString(PNR_RANDOM.nextLong(1_000_000_000L, 10_000_000_000L));
		} while (bookingRepo.existsByPnr(pnr));
		return pnr;
	}

	private void assignSeats(Booking booking, TrainSeatAvailability availability) {
		int capacity = booking.getTrain().getSeatsForClass(booking.getTravelClass());
		Set<Integer> occupiedSeats = new HashSet<>();
		List<Booking> confirmedBookings = bookingRepo.findByTrain_IdAndTravelDateAndTravelClassAndStatus(
				booking.getTrain().getId(), booking.getTravelDate(), booking.getTravelClass(), "CONFIRMED");
		for (Booking confirmedBooking : confirmedBookings) {
			for (Passenger passenger : confirmedBooking.getPassengers()) {
				if (passenger.getCoachNumber() != null && passenger.getSeatNumber() != null) {
					int coachIndex = Integer.parseInt(passenger.getCoachNumber().substring(1));
					int seatIndex = ((coachIndex - 1) * SEATS_PER_COACH) + passenger.getSeatNumber();
					if (seatIndex > 0 && seatIndex <= capacity) occupiedSeats.add(seatIndex);
				}
			}
		}

		int previouslyBooked = Math.max(0, capacity - availability.getAvailableSeats());
		for (int seatIndex = 1; occupiedSeats.size() < previouslyBooked && seatIndex <= capacity; seatIndex++) {
			occupiedSeats.add(seatIndex);
		}

		for (Passenger passenger : booking.getPassengers()) {
			int seatIndex = 1;
			while (seatIndex <= capacity && occupiedSeats.contains(seatIndex)) seatIndex++;
			if (seatIndex > capacity) {
				throw new IllegalStateException("No individual seats remain for this booking");
			}
			int coachIndex = ((seatIndex - 1) / SEATS_PER_COACH) + 1;
			int seatNumber = ((seatIndex - 1) % SEATS_PER_COACH) + 1;
			String coachPrefix = booking.getTravelClass() == TravelClass.AC ? "A" : "S";
			passenger.setCoachNumber(coachPrefix + coachIndex);
			passenger.setSeatNumber(seatNumber);
			occupiedSeats.add(seatIndex);
		}
	}

	private void validateTravelDate(LocalDate date) {
		if (date == null) throw new IllegalArgumentException("Travel date is required");
		if (date.isBefore(LocalDate.now())) throw new IllegalArgumentException("Cannot book for past date");
		if (date.isAfter(LocalDate.now().plusDays(bookingWindowDays))) {
			throw new IllegalArgumentException("Travel date is outside the booking window of " + bookingWindowDays + " days");
		}
	}

	private void ensureBookingOwner(Booking booking) {
		String loggedInUser = SecurityContextHolder.getContext().getAuthentication().getName();
		if (!booking.getUser().getUsername().equals(loggedInUser)) {
			throw new IllegalArgumentException("You are not authorized to access this booking");
		}
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
		if (!"CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
			throw new IllegalStateException("Only confirmed bookings can be cancelled");
		}
		
		if(booking.getTravelDate().isBefore(LocalDate.now())) {
			throw new RuntimeException("Journey is already completed.Cancellation is not allowed");
		}
		
		TrainSeatAvailability availability = seatRepo.findSeatForUpdate(
				booking.getTrain().getId(), booking.getTravelDate(), booking.getTravelClass());
		
		availability.setAvailableSeats(availability.getAvailableSeats() + booking.getSeatsBooked());
		
		seatRepo.save(availability);
		
		booking.setStatus("CANCELLED");
		if (booking.getPayment() != null && "SUCCESS".equals(booking.getPayment().getStatus())) {
			booking.getPayment().setStatus("REFUNDED");
			booking.getPayment().setRefundReference("DEMO-REFUND-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
			booking.getPayment().setRefundedAt(LocalDateTime.now());
		}
		
		bookingRepo.save(booking);
		
		return " Booking Cancelled Successfully";
		
	}

}
