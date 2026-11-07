package com.venkatesh.irctc.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.venkatesh.irctc.entity.Train;
import com.venkatesh.irctc.dto.TrainUpdateRequest;
import com.venkatesh.irctc.entity.TrainSeatAvailability;
import com.venkatesh.irctc.entity.TravelClass;
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
		if (train.getSleeperSeats() == 0 && train.getAcSeats() == 0 && train.getTotalSeats() > 0) {
			train.setSleeperSeats((train.getTotalSeats() + 1) / 2);
			train.setAcSeats(train.getTotalSeats() - train.getSleeperSeats());
		}
		if (train.getSleeperSeats() < 0 || train.getAcSeats() < 0
				|| train.getSleeperSeats() + train.getAcSeats() <= 0) {
			throw new IllegalArgumentException("Train must have at least one seat in one travel class");
		}
		java.math.BigDecimal baseFare = train.getFarePerSeat() == null
				? new java.math.BigDecimal("500.00") : train.getFarePerSeat();
		if (train.getSleeperFare() == null) train.setSleeperFare(baseFare);
		if (train.getAcFare() == null) train.setAcFare(baseFare.multiply(new java.math.BigDecimal("2.00")));
		if (train.getSleeperFare().signum() <= 0 || train.getAcFare().signum() <= 0) {
			throw new IllegalArgumentException("Class fares must be greater than zero");
		}
		train.setFarePerSeat(train.getSleeperFare());
		train.setTotalSeats(train.getSleeperSeats() + train.getAcSeats());
		
		if(trainReppository.findByTrainNumber(train.getTrainNumber()).isPresent()) {
			throw new IllegalStateException("Train Already Exists");
		}
		
		return trainReppository.save(train);
	}
	
	public List<Train> getAllTrains(){
		return trainReppository.findAll();
		
	}

	public Train getTrain(long trainId) {
		return trainReppository.findById(trainId)
				.orElseThrow(() -> new IllegalArgumentException("Train not found"));
	}

	public Train updateTrain(long trainId, TrainUpdateRequest request) {
		Train train = getTrain(trainId);
		String trainNumber = request.getTrainNumber().trim();
		trainReppository.findByTrainNumber(trainNumber)
				.filter(existingTrain -> existingTrain.getId() != trainId)
				.ifPresent(existingTrain -> { throw new IllegalStateException("Train number is already in use"); });
		train.setTrainNumber(trainNumber);
		train.setTrainName(request.getTrainName().trim());
		train.setSource(request.getSource().trim());
		train.setDestination(request.getDestination().trim());
		if (request.getSleeperFare().signum() <= 0 || request.getAcFare().signum() <= 0) {
			throw new IllegalArgumentException("Class fares must be greater than zero");
		}
		train.setSleeperFare(request.getSleeperFare());
		train.setAcFare(request.getAcFare());
		train.setFarePerSeat(request.getSleeperFare());
		return trainReppository.save(train);
	}
	
	public List<Train> searchTrains(String source,  String destination){
		return trainReppository.findBySourceAndDestination(source,destination);
	}
	
	public TrainSeatAvailability getSeatAvailibility(long trainId, LocalDate date) {
		return getSeatAvailibility(trainId, date, TravelClass.SLEEPER);
	}

	public TrainSeatAvailability getSeatAvailibility(long trainId, LocalDate date, TravelClass travelClass) {
		LocalDate today = LocalDate.now();
		if (date == null) {
			throw new IllegalArgumentException("Travel date is required");
		}
		if (date.isBefore(today)) {
			throw new IllegalStateException("Travel date cannot be in the past");
		}
		LocalDate maxBookingDate = today.plusDays(bookingWindowDays);
		 
		 if (date.isAfter(maxBookingDate)) {
	            throw new IllegalStateException("Booking not opened for this date yet");
	        }
		if (travelClass == null) throw new IllegalArgumentException("Choose Sleeper or AC class");

	        return trainSeatAvailabilityRepository
	                .findByTrainIdAndTravelDateAndTravelClass(trainId, date, travelClass)
	                .orElseGet(() -> {
	                    Train train = trainReppository.findById(trainId)
	                            .orElseThrow(() -> new RuntimeException("Train not found"));
	                    int classCapacity = train.getSeatsForClass(travelClass);
	                    if (classCapacity <= 0) throw new IllegalStateException("This train has no " + travelClass + " seats configured");

	                    TrainSeatAvailability newEntry = new TrainSeatAvailability();
	                    newEntry.setTrain(train);
	                    newEntry.setTravelDate(date);
	                    newEntry.setTravelClass(travelClass);
	                    newEntry.setAvailableSeats(classCapacity);

	                    return trainSeatAvailabilityRepository.save(newEntry);
	                });
	}
	
	

}
