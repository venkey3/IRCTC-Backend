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

	@Min(1)
	@Column(name = "total_seats", nullable = false)
    private int totalSeats;


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

	
	
	
	
	

}
