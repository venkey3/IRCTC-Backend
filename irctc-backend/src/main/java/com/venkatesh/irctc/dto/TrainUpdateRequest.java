package com.venkatesh.irctc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class TrainUpdateRequest {
    @NotBlank(message = "Train number is required")
    @Size(max = 30, message = "Train number cannot exceed 30 characters")
    private String trainNumber;
    @NotBlank(message = "Train name is required")
    @Size(max = 100, message = "Train name cannot exceed 100 characters")
    private String trainName;
    @NotBlank(message = "Origin is required")
    private String source;
    @NotBlank(message = "Destination is required")
    private String destination;
    @NotNull(message = "Sleeper fare is required")
    @DecimalMin(value = "0.01", message = "Fare must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Fare can have up to 2 decimal places")
    private BigDecimal sleeperFare;
    @NotNull(message = "AC fare is required")
    @DecimalMin(value = "0.01", message = "Fare must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Fare can have up to 2 decimal places")
    private BigDecimal acFare;

    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }
    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) { this.trainName = trainName; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public BigDecimal getSleeperFare() { return sleeperFare; }
    public void setSleeperFare(BigDecimal sleeperFare) { this.sleeperFare = sleeperFare; }
    public BigDecimal getAcFare() { return acFare; }
    public void setAcFare(BigDecimal acFare) { this.acFare = acFare; }
}
