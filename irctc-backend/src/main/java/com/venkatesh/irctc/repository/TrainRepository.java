package com.venkatesh.irctc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.venkatesh.irctc.entity.Train;


public interface TrainRepository extends JpaRepository<Train, Long> {
	
	Optional<Train> findByTrainNumber(String trainNumber);

	List<Train> findBySourceAndDestination(String source, String destination);
	
	
}

