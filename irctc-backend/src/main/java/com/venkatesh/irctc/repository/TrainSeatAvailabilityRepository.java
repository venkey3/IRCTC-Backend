package com.venkatesh.irctc.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.venkatesh.irctc.entity.Train;
import com.venkatesh.irctc.entity.TrainSeatAvailability;
import com.venkatesh.irctc.entity.TravelClass;

import jakarta.persistence.LockModeType;

public interface TrainSeatAvailabilityRepository extends JpaRepository<TrainSeatAvailability, Long> {
	
	Optional<TrainSeatAvailability> findByTrainIdAndTravelDate(Long trainid, LocalDate traveDate);

	Optional<TrainSeatAvailability> findByTrainIdAndTravelDateAndTravelClass(Long trainId, LocalDate travelDate, TravelClass travelClass);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT t
        FROM TrainSeatAvailability t
        WHERE t.train.id = :trainId  
        AND t.travelDate = :travelDate
    """)
    TrainSeatAvailability findSeatForUpdate(Long trainId, LocalDate travelDate);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT t FROM TrainSeatAvailability t WHERE t.train.id = :trainId AND t.travelDate = :travelDate AND t.travelClass = :travelClass")
	TrainSeatAvailability findSeatForUpdate(Long trainId, LocalDate travelDate, TravelClass travelClass);

	//Optional<Train> findByTrainIdAndTravelDate(long trainId, LocalDate date);
	
	

}
