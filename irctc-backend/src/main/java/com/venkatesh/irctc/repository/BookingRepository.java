package com.venkatesh.irctc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.venkatesh.irctc.entity.Booking;
import com.venkatesh.irctc.entity.User;

public interface BookingRepository extends JpaRepository<Booking, Long> {
	
	List<Booking> findByUser(User user);

}
