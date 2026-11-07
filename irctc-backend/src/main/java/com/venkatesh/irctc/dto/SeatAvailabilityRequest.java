package com.venkatesh.irctc.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import com.venkatesh.irctc.entity.TravelClass;

public class SeatAvailabilityRequest {
	@Min(1)
	private long trainId;
	
	@NotNull
	private LocalDate traveldate;
	
	@Min(0)
	private int availableSeats;

	private TravelClass travelClass = TravelClass.SLEEPER;
	
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

	public TravelClass getTravelClass() { return travelClass; }
	public void setTravelClass(TravelClass travelClass) { this.travelClass = travelClass; }
	
	

}
