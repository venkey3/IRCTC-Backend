package com.venkatesh.irctc.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="bookings")
public class Booking {
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	
	@jakarta.persistence.Column(nullable = false)
	private LocalDate travelDate;
	
	@jakarta.persistence.Column(nullable = false)
	private int seatsBooked;
	
	@jakarta.persistence.Column(nullable = false)
	private String status;

	@jakarta.persistence.Column(nullable = false, unique = true, length = 10)
	private String pnr;

	@jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
	@jakarta.persistence.Column(name = "travel_class", nullable = false, length = 16)
	private TravelClass travelClass = TravelClass.SLEEPER;
	
	@ManyToOne
	@JoinColumn(name="user_id", nullable = false)
	private User user;
	
	@ManyToOne
	@JoinColumn(name="train_id", nullable = false)
	private Train train;

	@OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Passenger> passengers = new ArrayList<>();

	@OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
	private Payment payment;
	
	private LocalDate createdAt = LocalDate.now();

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

	public int getSeatsBooked() {
		return seatsBooked;
	}

	public void setSeatsBooked(int seatsBooked) {
		this.seatsBooked = seatsBooked;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPnr() { return pnr; }

	public void setPnr(String pnr) { this.pnr = pnr; }
	public TravelClass getTravelClass() { return travelClass; }
	public void setTravelClass(TravelClass travelClass) { this.travelClass = travelClass; }

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Train getTrain() {
		return train;
	}

	public void setTrain(Train train) {
		this.train = train;
	}

	public List<Passenger> getPassengers() {
		return passengers;
	}

	public Payment getPayment() { return payment; }

	public void setPayment(Payment payment) {
		this.payment = payment;
		if (payment != null) payment.setBooking(this);
	}

	public void addPassenger(Passenger passenger) {
		passengers.add(passenger);
		passenger.setBooking(this);
	}
	
	
	
	

}
