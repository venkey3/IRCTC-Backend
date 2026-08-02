package com.venkatesh.irctc.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.venkatesh.irctc.dto.SeatAvailabilityRequest;
import com.venkatesh.irctc.entity.Train;
import com.venkatesh.irctc.entity.TrainSeatAvailability;
import com.venkatesh.irctc.repository.TrainRepository;
import com.venkatesh.irctc.repository.TrainSeatAvailabilityRepository;

@Service
public class TrainSeatAvailabilityService {
	
	@Autowired
	private TrainRepository trainRepository;
	
	@Autowired
	private TrainSeatAvailabilityRepository seatRepository;
	
	public TrainSeatAvailability createSeatAvailability(@RequestBody SeatAvailabilityRequest Request) {
		
		Train train = trainRepository.findById(Request.getTrainId())
				.orElseThrow(() -> new IllegalStateException("Train not found"));
		
		if(seatRepository.findByTrainIdAndTravelDate(Request.getTrainId(), Request.getTraveldate()).isPresent()) {
			
			throw new IllegalStateException("Seat Availibilty already exists for this train and date");
		}
		
		if (Request.getTraveldate() == null) {
		    throw new IllegalStateException("Travel date cannot be null");
		}

		if (Request.getTraveldate().isBefore(LocalDate.now())) {
		    throw new IllegalStateException("You cannot add for previous days");
		}

		 
		TrainSeatAvailability availbility = new TrainSeatAvailability();
		
		availbility.setTrain(train);
		availbility.setAvailableSeats(Request.getAvailableSeats());
		availbility.setTravelDate(Request.getTraveldate());
		
		
		
		return seatRepository.save(availbility);
		
	}
	
	
	
	
	

}
