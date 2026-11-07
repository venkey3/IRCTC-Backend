package com.venkatesh.irctc.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.venkatesh.irctc.dto.SeatAvailabilityRequest;
import com.venkatesh.irctc.entity.Train;
import com.venkatesh.irctc.entity.TrainSeatAvailability;
import com.venkatesh.irctc.entity.TravelClass;
import com.venkatesh.irctc.repository.TrainRepository;
import com.venkatesh.irctc.repository.TrainSeatAvailabilityRepository;

@Service
public class TrainSeatAvailabilityService {
	
	@Autowired
	private TrainRepository trainRepository;
	
	@Autowired
	private TrainSeatAvailabilityRepository seatRepository;
	
	public TrainSeatAvailability createSeatAvailability(@RequestBody SeatAvailabilityRequest Request) {
		if (Request.getTraveldate() == null) {
		    throw new IllegalStateException("Travel date cannot be null");
		}

		if (Request.getTraveldate().isBefore(LocalDate.now())) {
		    throw new IllegalStateException("You cannot add for previous days");
		}
		if (Request.getAvailableSeats() < 0) {
			throw new IllegalStateException("Available seats cannot be negative");
		}

		Train train = trainRepository.findById(Request.getTrainId())
				.orElseThrow(() -> new IllegalStateException("Train not found"));
		TravelClass travelClass = Request.getTravelClass() == null ? TravelClass.SLEEPER : Request.getTravelClass();
		int classCapacity = train.getSeatsForClass(travelClass);
		if (classCapacity <= 0) throw new IllegalStateException("This train has no " + travelClass + " seats configured");
		if (Request.getAvailableSeats() > classCapacity) {
			throw new IllegalStateException("Available seats cannot exceed train capacity");
		}

		if(seatRepository.findByTrainIdAndTravelDateAndTravelClass(Request.getTrainId(), Request.getTraveldate(), travelClass).isPresent()) {
			throw new IllegalStateException("Seat Availability already exists for this train and date");
		}

		 
		TrainSeatAvailability availbility = new TrainSeatAvailability();
		
		availbility.setTrain(train);
		availbility.setAvailableSeats(Request.getAvailableSeats());
		availbility.setTravelDate(Request.getTraveldate());
		availbility.setTravelClass(travelClass);
		
		
		
		return seatRepository.save(availbility);
		
	}
	
	
	
	
	

}
