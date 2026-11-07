package com.venkatesh.irctc_backend;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.venkatesh.irctc.dto.SeatAvailabilityRequest;
import com.venkatesh.irctc.entity.Train;
import com.venkatesh.irctc.repository.TrainRepository;
import com.venkatesh.irctc.repository.TrainSeatAvailabilityRepository;
import com.venkatesh.irctc.service.TrainSeatAvailabilityService;

class TrainSeatAvailabilityServiceTest {

    @Test
    void rejectsAvailabilityAboveTrainCapacity() {
        Train train = new Train();
        train.setId(10L);
        train.setTotalSeats(5);
        TrainRepository trains = proxy(TrainRepository.class, (method, args) ->
                "findById".equals(method) ? Optional.of(train) : null);
        TrainSeatAvailabilityRepository availability = proxy(TrainSeatAvailabilityRepository.class,
                (method, args) -> {
                    if ("findByTrainIdAndTravelDate".equals(method)) {
                        throw new AssertionError("Availability lookup should follow capacity validation");
                    }
                    return null;
                });
        TrainSeatAvailabilityService service = new TrainSeatAvailabilityService();
        ReflectionTestUtils.setField(service, "trainRepository", trains);
        ReflectionTestUtils.setField(service, "seatRepository", availability);

        SeatAvailabilityRequest request = new SeatAvailabilityRequest();
        request.setTrainId(10L);
        request.setTraveldate(LocalDate.now().plusDays(1));
        request.setAvailableSeats(6);

        assertThrows(IllegalStateException.class, () -> service.createSeatAvailability(request));
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, RepositoryCall call) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
                (proxy, method, args) -> {
                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    if ("toString".equals(method.getName())) {
                        return "Test proxy for " + type.getSimpleName();
                    }
                    return call.invoke(method.getName(), args == null ? new Object[0] : args);
                });
    }

    @FunctionalInterface
    private interface RepositoryCall {
        Object invoke(String method, Object[] args);
    }
}
