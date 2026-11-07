package com.venkatesh.irctc.dto;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.venkatesh.irctc.entity.TravelClass;
import java.util.List;

public class BookingResponse {
	
	private Long bookingId;
	
	private String trainNumber;
	
	private String status;
	private String pnr;
	private TravelClass travelClass;
	private String paymentStatus;
	private BigDecimal paymentAmount;
	private String paymentMethod;
	private String transactionReference;
	private LocalDateTime paidAt;
	private String refundReference;
	private LocalDateTime refundedAt;
	
	private LocalDate travelDate;
	
	private int seatsBooked;
	
	private String trainName;
	private List<PassengerResponse> passengers;

	public Long getBookingId() {
		return bookingId;
	}

	public void setBookingId(Long bookingId) {
		this.bookingId = bookingId;
	}

	public String getTrainNumber() {
		return trainNumber;
	}

	public void setTrainNumber(String trainNumber) {
		this.trainNumber = trainNumber;
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
	public String getPaymentStatus() { return paymentStatus; }
	public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
	public BigDecimal getPaymentAmount() { return paymentAmount; }
	public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }
	public String getPaymentMethod() { return paymentMethod; }
	public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
	public String getTransactionReference() { return transactionReference; }
	public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
	public LocalDateTime getPaidAt() { return paidAt; }
	public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
	public String getRefundReference() { return refundReference; }
	public void setRefundReference(String refundReference) { this.refundReference = refundReference; }
	public LocalDateTime getRefundedAt() { return refundedAt; }
	public void setRefundedAt(LocalDateTime refundedAt) { this.refundedAt = refundedAt; }

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

	public String getTrainName() {
		return trainName;
	}

	public void setTrainName(String trainName) {
		this.trainName = trainName;
	}

	public List<PassengerResponse> getPassengers() {
		return passengers;
	}

	public void setPassengers(List<PassengerResponse> passengers) {
		this.passengers = passengers;
	}
	
	

}
