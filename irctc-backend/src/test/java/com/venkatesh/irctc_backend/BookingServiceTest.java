package com.venkatesh.irctc_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.venkatesh.irctc.entity.Booking;
import com.venkatesh.irctc.entity.Train;
import com.venkatesh.irctc.entity.TrainSeatAvailability;
import com.venkatesh.irctc.entity.User;
import com.venkatesh.irctc.repository.BookingRepository;
import com.venkatesh.irctc.repository.TrainRepository;
import com.venkatesh.irctc.repository.TrainSeatAvailabilityRepository;
import com.venkatesh.irctc.repository.UserRepository;
import com.venkatesh.irctc.service.BookingService;

class BookingServiceTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsBookingBeyondConfiguredWindowBeforeLookingUpSeats() {
        BookingService service = service(null, null, null, null);
        LocalDate date = LocalDate.now().plusDays(61);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.bookTicket(10L, date, 1));

        assertEquals("Travel date is outside the booking window of 60 days", exception.getMessage());
    }

    @Test
    void rejectsPastBookingDateBeforeLookingUpSeats() {
        BookingService service = service(null, null, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> service.bookTicket(10L, LocalDate.now().minusDays(1), 1));
    }

    @Test
    void rejectsNonPositiveSeatCountBeforeLookingUpSeats() {
        BookingService service = service(null, null, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> service.bookTicket(10L, LocalDate.now(), 0));
    }

    @Test
    void rejectsRequestWhenNotEnoughSeatsAreAvailable() {
        TrainSeatAvailability availability = new TrainSeatAvailability();
        availability.setAvailableSeats(1);
        TrainSeatAvailabilityRepository seats = repository(TrainSeatAvailabilityRepository.class,
                (method, args) -> "findSeatForUpdate".equals(method) ? availability : null);
        BookingRepository bookings = repository(BookingRepository.class, (method, args) -> null);
        BookingService service = service(seats, bookings, null, null);
        authenticateAsTraveler();

        assertThrows(RuntimeException.class,
                () -> service.bookTicket(10L, LocalDate.now().plusDays(1), 2));

        assertEquals(1, availability.getAvailableSeats());
    }

    @Test
    void booksWithinWindowAndDecrementsAvailableSeats() {
        TrainSeatAvailability availability = new TrainSeatAvailability();
        availability.setAvailableSeats(3);
        Train train = new Train();
        train.setId(10L);
        train.setTrainName("Express");
        train.setTrainNumber("100");
        User user = new User("traveler", "traveler@example.com", "encoded", "ROLE_USER");

        TrainSeatAvailabilityRepository seats = repository(TrainSeatAvailabilityRepository.class,
                (method, args) -> "findSeatForUpdate".equals(method) ? availability : null);
        TrainRepository trains = repository(TrainRepository.class,
                (method, args) -> "findById".equals(method) ? Optional.of(train) : null);
        UserRepository users = repository(UserRepository.class,
                (method, args) -> "findByUsername".equals(method) ? Optional.of(user) : null);
        BookingRepository bookings = repository(BookingRepository.class, (method, args) -> {
            if ("save".equals(method)) {
                Booking booking = (Booking) args[0];
                booking.setId(50L);
                return booking;
            }
            return null;
        });
        BookingService service = service(seats, bookings, trains, users);
        authenticateAsTraveler();

        var response = service.bookTicket(10L, LocalDate.now().plusDays(1), 2);

        assertEquals(1, availability.getAvailableSeats());
        assertEquals(50L, response.getBookingId());
        assertEquals(2, response.getSeatsBooked());
    }

    private BookingService service(TrainSeatAvailabilityRepository seats, BookingRepository bookings,
            TrainRepository trains, UserRepository users) {
        BookingService service = new BookingService();
        ReflectionTestUtils.setField(service, "bookingWindowDays", 60);
        ReflectionTestUtils.setField(service, "seatRepo", seats);
        ReflectionTestUtils.setField(service, "bookingRepo", bookings);
        ReflectionTestUtils.setField(service, "trainRepository", trains);
        ReflectionTestUtils.setField(service, "userRepository", users);
        return service;
    }

    private void authenticateAsTraveler() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("traveler", "", List.of()));
    }

    @SuppressWarnings("unchecked")
    private <T> T repository(Class<T> type, RepositoryCall call) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
                (proxy, method, args) -> call.invoke(method.getName(), args == null ? new Object[0] : args));
    }

    @FunctionalInterface
    private interface RepositoryCall {
        Object invoke(String method, Object[] args);
    }
}
