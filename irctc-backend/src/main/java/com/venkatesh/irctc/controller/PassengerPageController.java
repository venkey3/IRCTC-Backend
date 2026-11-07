package com.venkatesh.irctc.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.venkatesh.irctc.dto.BookingRequest;
import com.venkatesh.irctc.dto.BookingResponse;
import com.venkatesh.irctc.dto.PassengerRequest;
import com.venkatesh.irctc.dto.ProfileUpdateRequest;
import com.venkatesh.irctc.dto.PaymentRequest;
import com.venkatesh.irctc.entity.Passenger.Gender;
import com.venkatesh.irctc.entity.Train;
import com.venkatesh.irctc.entity.TrainSeatAvailability;
import com.venkatesh.irctc.entity.TravelClass;
import com.venkatesh.irctc.entity.User;
import com.venkatesh.irctc.repository.TrainRepository;
import com.venkatesh.irctc.repository.UserRepository;
import com.venkatesh.irctc.service.BookingService;
import com.venkatesh.irctc.service.TrainService;

import jakarta.validation.Valid;

@Controller
public class PassengerPageController {

    private final TrainService trainService;
    private final TrainRepository trainRepository;
    private final BookingService bookingService;
    private final UserRepository userRepository;

    @Value("${booking.window.days:60}")
    private int bookingWindowDays;

    public PassengerPageController(TrainService trainService, TrainRepository trainRepository,
            BookingService bookingService, UserRepository userRepository) {
        this.trainService = trainService;
        this.trainRepository = trainRepository;
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    @GetMapping("/app/dashboard")
    public String dashboard(Model model) {
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (isAdmin) {
            return "redirect:/app/admin/dashboard";
        }
        List<BookingResponse> bookings = bookingService.getMyBookings();
        LocalDate today = LocalDate.now();
        List<BookingResponse> upcoming = bookings.stream()
                .filter(booking -> "CONFIRMED".equalsIgnoreCase(booking.getStatus()))
                .filter(booking -> !booking.getTravelDate().isBefore(today))
                .sorted(Comparator.comparing(BookingResponse::getTravelDate))
                .toList();
        List<BookingResponse> recent = bookings.stream()
                .sorted(Comparator.comparing(BookingResponse::getBookingId).reversed())
                .limit(5)
                .toList();

        model.addAttribute("upcomingBookings", upcoming);
        model.addAttribute("recentBookings", recent);
        return "app/dashboard";
    }

    @GetMapping("/app/trains/search")
    public String searchTrains(@RequestParam(required = false) String source,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "SLEEPER") TravelClass travelClass,
            Model model) {
        model.addAttribute("source", source == null ? "" : source);
        model.addAttribute("destination", destination == null ? "" : destination);
        model.addAttribute("travelDate", date == null ? "" : date.toString());
        model.addAttribute("travelClass", travelClass);
        model.addAttribute("travelClasses", TravelClass.values());

        boolean validDate = date == null || (!date.isBefore(LocalDate.now())
                && !date.isAfter(LocalDate.now().plusDays(bookingWindowDays)));
        if (!validDate) {
            model.addAttribute("dateError", date.isBefore(LocalDate.now())
                    ? "Choose today or a future date. Past travel dates are unavailable."
                    : "Booking is available up to " + bookingWindowDays + " days in advance.");
        }

        if (validDate && source != null && !source.isBlank() && destination != null && !destination.isBlank()) {
            model.addAttribute("trains", trainService.searchTrains(source.trim(), destination.trim()));
        }
        return "app/train-search";
    }

    @GetMapping("/app/trains/{trainId}/availability")
    public String trainAvailability(@PathVariable Long trainId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "SLEEPER") TravelClass travelClass,
            Model model) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Train not found"));
        TrainSeatAvailability availability = trainService.getSeatAvailibility(trainId, date, travelClass);
        model.addAttribute("train", train);
        model.addAttribute("travelDate", date);
        model.addAttribute("availability", availability);
        model.addAttribute("travelClass", travelClass);
        return "app/train-availability";
    }

    @GetMapping("/app/bookings/new")
    public String newBooking(@RequestParam Long trainId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "SLEEPER") TravelClass travelClass,
            Model model) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Train not found"));
        TrainSeatAvailability availability = trainService.getSeatAvailibility(trainId, date, travelClass);
        BookingRequest request = new BookingRequest();
        request.setTrainId(trainId);
        request.setTravelDate(date);
        request.setTravelClass(travelClass);
        request.setPassengers(List.of(new PassengerRequest()));
        addBookingFormModel(model, request, train, availability.getAvailableSeats());
        return "app/booking-form";
    }

    @PostMapping("/app/bookings")
    public String createBooking(@Valid @ModelAttribute("bookingRequest") BookingRequest request,
            BindingResult bindingResult, Model model, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            Train train = request.getTrainId() == null ? null : trainRepository.findById(request.getTrainId()).orElse(null);
            if (request.getPassengers() == null || request.getPassengers().isEmpty()) {
                request.setPassengers(List.of(new PassengerRequest()));
            }
            addBookingFormModel(model, request, train, 0);
            return "app/booking-form";
        }

        try {
            Train train = trainRepository.findById(request.getTrainId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Train not found"));
            TrainSeatAvailability availability = trainService.getSeatAvailibility(request.getTrainId(), request.getTravelDate(), request.getTravelClass());
            addBookingFormModel(model, request, train, availability.getAvailableSeats());
            BookingResponse booking = bookingService.initiateBooking(request);
            return "redirect:/app/bookings/" + booking.getBookingId() + "/payment";
        } catch (RuntimeException exception) {
            if (request.getTrainId() != null) {
                trainRepository.findById(request.getTrainId()).ifPresent(train -> addBookingFormModel(model, request, train, 0));
            }
            model.addAttribute("bookingError", exception.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return "app/booking-form";
        }
    }

    @GetMapping("/app/bookings/{bookingId}/confirmation")
    public String bookingConfirmation(@PathVariable Long bookingId, Model model) {
        BookingResponse booking = findMyBooking(bookingId);
        model.addAttribute("booking", booking);
        return "app/booking-confirmation";
    }

    @GetMapping("/app/bookings/{bookingId}/payment")
    public String paymentPage(@PathVariable Long bookingId, Model model) {
        BookingResponse booking = findMyBooking(bookingId);
        if (!"PENDING_PAYMENT".equals(booking.getStatus())) {
            return "redirect:/app/bookings/" + bookingId + "/confirmation";
        }
        model.addAttribute("booking", booking);
        return "app/payment";
    }

    @PostMapping("/app/bookings/{bookingId}/payment")
    public String processPayment(@PathVariable Long bookingId, @RequestParam boolean successful,
            @RequestParam String paymentMethod, RedirectAttributes redirectAttributes) {
        try {
            PaymentRequest request = new PaymentRequest();
            request.setSuccessful(successful);
            request.setPaymentMethod(paymentMethod);
            BookingResponse booking = bookingService.processDemoPayment(bookingId, request);
            if ("CONFIRMED".equals(booking.getStatus())) {
                return "redirect:/app/bookings/" + bookingId + "/confirmation";
            }
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Demo payment failed. No seats were charged to your booking.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/app/bookings";
    }

    @GetMapping("/app/bookings")
    public String myBookings(@RequestParam(defaultValue = "upcoming") String view,
            @RequestParam(required = false) String pnr, Model model) {
        LocalDate today = LocalDate.now();
        String pnrQuery = pnr == null ? "" : pnr.trim();
        List<BookingResponse> bookings = bookingService.getMyBookings();
        if (!pnrQuery.isBlank() && !pnrQuery.matches("\\d{10}")) {
            model.addAttribute("pnrError", "Enter a valid 10-digit PNR.");
            bookings = List.of();
        } else if (!pnrQuery.isBlank()) {
            bookings = bookings.stream().filter(booking -> pnrQuery.equals(booking.getPnr())).toList();
        }
        List<BookingResponse> filtered = switch (view.toLowerCase()) {
            case "past" -> bookings.stream().filter(booking -> booking.getTravelDate().isBefore(today)).toList();
            case "cancelled" -> bookings.stream()
                    .filter(booking -> "CANCELLED".equalsIgnoreCase(booking.getStatus())).toList();
            case "pending" -> bookings.stream()
                    .filter(booking -> "PENDING_PAYMENT".equalsIgnoreCase(booking.getStatus())).toList();
            case "payment_failed" -> bookings.stream()
                    .filter(booking -> "PAYMENT_FAILED".equalsIgnoreCase(booking.getStatus())).toList();
            case "all" -> bookings;
            default -> bookings.stream()
                    .filter(booking -> "CONFIRMED".equalsIgnoreCase(booking.getStatus()))
                    .filter(booking -> !booking.getTravelDate().isBefore(today)).toList();
        };
        model.addAttribute("bookings", filtered);
        model.addAttribute("activeView", view.toLowerCase());
        model.addAttribute("pnrQuery", pnrQuery);
        return "app/bookings";
    }

    @PostMapping("/app/bookings/{bookingId}/cancel")
    public String cancelBooking(@PathVariable Long bookingId, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(bookingId);
            redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled and seats returned.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/app/bookings";
    }

    @GetMapping("/app/profile")
    public String profile(Model model) {
        String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        model.addAttribute("profileUser", user);
        ProfileUpdateRequest profileRequest = new ProfileUpdateRequest();
        profileRequest.setEmail(user.getEmail());
        model.addAttribute("profileRequest", profileRequest);
        return "app/profile";
    }

    @PostMapping("/app/profile")
    public String updateProfile(@Valid @ModelAttribute("profileRequest") ProfileUpdateRequest request,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!bindingResult.hasErrors()) {
            String email = request.getEmail().trim();
            userRepository.findByEmailIgnoreCase(email)
                    .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                    .ifPresent(existingUser -> bindingResult.rejectValue("email", "duplicate",
                            "That email address is already in use."));
            request.setEmail(email);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("profileUser", user);
            return "app/profile";
        }

        user.setEmail(request.getEmail());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Your email address has been updated.");
        return "redirect:/app/profile";
    }

    private BookingResponse findMyBooking(Long bookingId) {
        return bookingService.getMyBookings().stream()
                .filter(booking -> booking.getBookingId().equals(bookingId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    }

    private void addBookingFormModel(Model model, BookingRequest request, Train train, int availableSeats) {
        model.addAttribute("bookingRequest", request);
        model.addAttribute("train", train);
        model.addAttribute("availableSeats", availableSeats);
        TravelClass travelClass = request.getTravelClass() == null ? TravelClass.SLEEPER : request.getTravelClass();
        model.addAttribute("travelClasses", TravelClass.values());
        model.addAttribute("travelClass", travelClass);
        model.addAttribute("farePerPassenger", train == null ? null : train.getFareForClass(travelClass));
        model.addAttribute("genders", Gender.values());
        model.addAttribute("dateLabel", request.getTravelDate() == null ? "Select a travel date"
                : request.getTravelDate().format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy")));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public String handlePageStatus(ResponseStatusException exception, Model model, HttpServletResponse response) {
        int status = exception.getStatusCode().value();
        response.setStatus(status);
        model.addAttribute("errorMessage", exception.getReason() == null ? "The requested item was not found." : exception.getReason());
        return status == HttpStatus.NOT_FOUND.value() ? "error/404" : "error/500";
    }

    @ExceptionHandler({ IllegalStateException.class, IllegalArgumentException.class })
    public String handlePageRuleError(RuntimeException exception, Model model, HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        model.addAttribute("errorMessage", exception.getMessage());
        return "error/400";
    }
}
