package com.venkatesh.irctc.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.venkatesh.irctc.entity.Train;
import com.venkatesh.irctc.entity.TrainSeatAvailability;
import com.venkatesh.irctc.repository.TrainRepository;
import com.venkatesh.irctc.repository.TrainSeatAvailabilityRepository;

@Service
public class TrainService {
	
	@Value("${booking.window.days}")
	private int bookingWindowDays;
	
	@Autowired
	private TrainRepository trainReppository;
	
	@Autowired
	private TrainSeatAvailabilityRepository trainSeatAvailabilityRepository;
	
	public Train addTrain(Train train) {
		
		if(trainReppository.findByTrainNumber(train.getTrainNumber()).isPresent()) {
			throw new IllegalStateException("Train Already Exists");
		}
		
		return trainReppository.save(train);
	}
	
	public List<Train> getAllTrains(){
		return trainReppository.findAll();
		
	}
	
	public List<Train> searchTrains(String source,  String destination){
		return trainReppository.findBySourceAndDestination(source,destination);
	}
	
	public TrainSeatAvailability getSeatAvailibility(long trainId, LocalDate date) {
		LocalDate today=LocalDate.now();
		 LocalDate maxBookingDate = today.plusDays(bookingWindowDays);
		 
		 if (date.isAfter(maxBookingDate)) {
	            throw new IllegalStateException("Booking not opened for this date yet");
	        }

	        return trainSeatAvailabilityRepository
	                .findByTrainIdAndTravelDate(trainId, date)
	                .orElseGet(() -> {
	                    Train train = trainReppository.findById(trainId)
	                            .orElseThrow(() -> new RuntimeException("Train not found"));

	                    TrainSeatAvailability newEntry = new TrainSeatAvailability();
	                    newEntry.setTrain(train);
	                    newEntry.setTravelDate(date);
	                    newEntry.setAvailableSeats(train.getTotalSeats());

	                    return trainSeatAvailabilityRepository.save(newEntry);
	                });
	}
	
	

}
