package com.venkatesh.irctc.repository;

import java.util.List;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.venkatesh.irctc.entity.Booking;
import com.venkatesh.irctc.entity.User;
import com.venkatesh.irctc.entity.TravelClass;

public interface BookingRepository extends JpaRepository<Booking, Long> {
	
	List<Booking> findByUser(User user);

	boolean existsByPnr(String pnr);

	List<Booking> findByTrain_IdAndTravelDateAndStatus(Long trainId, LocalDate travelDate, String status);

	List<Booking> findByTrain_IdAndTravelDateAndTravelClassAndStatus(Long trainId, LocalDate travelDate,
			TravelClass travelClass, String status);

}
