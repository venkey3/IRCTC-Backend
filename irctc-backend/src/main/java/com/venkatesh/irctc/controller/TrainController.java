package com.venkatesh.irctc.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.venkatesh.irctc.entity.Train;
import com.venkatesh.irctc.entity.TravelClass;
import com.venkatesh.irctc.service.TrainService;

@RestController
@RequestMapping("/trains")
public class TrainController {
	
	@Autowired
	private TrainService trainService;
	
	@PostMapping
	public ResponseEntity<?> addTrain(@Valid @RequestBody Train train){
		Train savedTrain = trainService.addTrain(train);
        return ResponseEntity.ok(savedTrain);
	}
	
	@GetMapping
	public ResponseEntity<List<Train>> getAllTrains(){
		return ResponseEntity.ok(trainService.getAllTrains());
	}
	
	@GetMapping("/search")
	public ResponseEntity<List<Train>> searchTrins(@RequestParam String source, @RequestParam String destination){
		return ResponseEntity.ok(trainService.searchTrains(source, destination));
	}
	
	@GetMapping("/availability")
	public ResponseEntity<?> getAvailability(
	        @RequestParam Long trainId,
	        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
	        @RequestParam(defaultValue = "SLEEPER") TravelClass travelClass) {

	    return ResponseEntity.ok(
	            trainService.getSeatAvailibility(trainId, date, travelClass)
	    );
	}

}
