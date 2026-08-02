package com.venkatesh.irctc.dto;

import java.time.LocalDate;

public class SeatAvailabilityRequest {
	private long trainId;
	
	private LocalDate traveldate;
	
	private int availableSeats;
	
	// getters and setters
	

	public long getTrainId() {
		return trainId;
	}

	public void setTrainId(long trainId) {
		this.trainId = trainId;
	}

	public LocalDate getTraveldate() {
		return traveldate;
	}

	public void setTraveldate(LocalDate traveldate) {
		this.traveldate = traveldate;
	}

	public int getAvailableSeats() {
		return availableSeats;
	}

	public void setAvailableSeats(int availableSeats) {
		this.availableSeats = availableSeats;
	}
	
	

}
