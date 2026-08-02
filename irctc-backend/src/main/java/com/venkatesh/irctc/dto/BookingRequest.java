package com.venkatesh.irctc.dto;

import java.time.LocalDate;

public class BookingRequest {
	
	
	private Long trainId;
	
	private LocalDate travelDate;
	
	private int seatsRequired;

	public Long getTrainId() {
		return trainId;
	}

	public void setTrainId(Long trainId) {
		this.trainId = trainId;
	}

	public LocalDate getTravelDate() {
		return travelDate;
	}

	public void setTravelDate(LocalDate travelDate) {
		this.travelDate = travelDate;
	}

	public int getSeatsRequired() {
		return seatsRequired;
	}

	public void setSeatsRequired(int seatsRequired) {
		this.seatsRequired = seatsRequired;
	}
	
	

}
