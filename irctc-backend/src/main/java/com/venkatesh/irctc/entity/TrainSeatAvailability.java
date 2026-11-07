package com.venkatesh.irctc.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
@Table(name="train_seat_availability", uniqueConstraints = @UniqueConstraint(
		name = "uk_seat_availability_train_date_class", columnNames = { "train_id", "travel_date", "travel_class" }))
public class TrainSeatAvailability {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@jakarta.persistence.Column(nullable = false)
	private LocalDate travelDate;
	
	@jakarta.persistence.Column(nullable = false)
	private int availableSeats;

	@Enumerated(EnumType.STRING)
	@jakarta.persistence.Column(name = "travel_class", nullable = false, length = 16)
	private TravelClass travelClass = TravelClass.SLEEPER;
	
	@ManyToOne
	@JoinColumn(name = "train_id", nullable = false)
	private Train train;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public LocalDate getTravelDate() {
		return travelDate;
	}

	public void setTravelDate(LocalDate travelDate) {
		this.travelDate = travelDate;
	}

	public int getAvailableSeats() {
		return availableSeats;
	}

	public void setAvailableSeats(int availableSeats) {
		this.availableSeats = availableSeats;
	}

	public TravelClass getTravelClass() { return travelClass; }
	public void setTravelClass(TravelClass travelClass) { this.travelClass = travelClass; }

	public Train getTrain() {
		return train;
	}

	public void setTrain(Train train) {
		this.train = train;
	}
	
	
	
	

}
