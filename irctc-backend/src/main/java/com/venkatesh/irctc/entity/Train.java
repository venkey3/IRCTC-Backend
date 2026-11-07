package com.venkatesh.irctc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
@Entity
@Table(name="trains", uniqueConstraints = @UniqueConstraint(
		name = "uk_trains_train_number", columnNames = "train_number"))
public class Train {
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@NotBlank
	@Column(name = "train_number", nullable = false)
    private String trainNumber;

	@NotBlank
	@Column(name = "train_name", nullable = false)
    private String trainName;

	@NotBlank
	@Column(name = "source", nullable = false)
    private String source;

	@NotBlank
	@Column(name = "destination", nullable = false)
    private String destination;

	@Column(name = "total_seats", nullable = false)
    private int totalSeats;

	@DecimalMin(value = "0.01", message = "Fare must be greater than zero")
	@Digits(integer = 8, fraction = 2, message = "Fare can have up to 2 decimal places")
	@Column(name = "fare_per_seat", nullable = false, precision = 10, scale = 2)
	private BigDecimal farePerSeat;

	@Column(name = "sleeper_seats", nullable = false)
	private int sleeperSeats;

	@Column(name = "ac_seats", nullable = false)
	private int acSeats;

	@Column(name = "sleeper_fare", nullable = false, precision = 10, scale = 2)
	private BigDecimal sleeperFare;

	@Column(name = "ac_fare", nullable = false, precision = 10, scale = 2)
	private BigDecimal acFare;

	@PrePersist
	private void applyDefaultFare() {
		if (farePerSeat == null) farePerSeat = new BigDecimal("500.00");
		if (sleeperSeats == 0 && acSeats == 0 && totalSeats > 0) {
			sleeperSeats = (totalSeats + 1) / 2;
			acSeats = totalSeats - sleeperSeats;
		}
		if (sleeperFare == null) sleeperFare = farePerSeat;
		if (acFare == null) acFare = farePerSeat.multiply(new BigDecimal("2.00"));
		totalSeats = sleeperSeats + acSeats;
	}

	@PreUpdate
	private void updateTotalSeats() { totalSeats = sleeperSeats + acSeats; }


	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTrainName() {
		return trainName;
	}

	public void setTrainName(String trainName) {
		this.trainName = trainName;
	}

	public String getTrainNumber() {
		return trainNumber;
	}

	public void setTrainNumber(String trainNumber) {
		this.trainNumber = trainNumber;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public int getTotalSeats() {
		return totalSeats;
	}

	public void setTotalSeats(int totalSeats) {
		this.totalSeats = totalSeats;
	}

	public BigDecimal getFarePerSeat() { return farePerSeat; }

	public void setFarePerSeat(BigDecimal farePerSeat) { this.farePerSeat = farePerSeat; }
	public int getSleeperSeats() { return sleeperSeats; }
	public void setSleeperSeats(int sleeperSeats) { this.sleeperSeats = sleeperSeats; }
	public int getAcSeats() { return acSeats; }
	public void setAcSeats(int acSeats) { this.acSeats = acSeats; }
	public BigDecimal getSleeperFare() { return sleeperFare; }
	public void setSleeperFare(BigDecimal sleeperFare) { this.sleeperFare = sleeperFare; }
	public BigDecimal getAcFare() { return acFare; }
	public void setAcFare(BigDecimal acFare) { this.acFare = acFare; }

	public int getSeatsForClass(TravelClass travelClass) {
		return travelClass == TravelClass.AC ? acSeats : sleeperSeats;
	}

	public BigDecimal getFareForClass(TravelClass travelClass) {
		return travelClass == TravelClass.AC ? acFare : sleeperFare;
	}

	
	
	
	
	

}
