package com.venkatesh.irctc.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.venkatesh.irctc.dto.SeatAvailabilityRequest;
import com.venkatesh.irctc.dto.TrainUpdateRequest;
import com.venkatesh.irctc.entity.Train;
import com.venkatesh.irctc.entity.TravelClass;
import com.venkatesh.irctc.service.TrainSeatAvailabilityService;
import com.venkatesh.irctc.service.TrainService;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class AdminPageController {

    private final TrainService trainService;
    private final TrainSeatAvailabilityService availabilityService;

    public AdminPageController(TrainService trainService, TrainSeatAvailabilityService availabilityService) {
        this.trainService = trainService;
        this.availabilityService = availabilityService;
    }

    @GetMapping("/app/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("trainCount", trainService.getAllTrains().size());
        return "app/admin/dashboard";
    }

    @GetMapping("/app/admin/trains")
    public String trains(Model model) {
        model.addAttribute("trains", trainService.getAllTrains());
        return "app/admin/trains";
    }

    @GetMapping("/app/admin/trains/new")
    public String newTrain(Model model) {
        model.addAttribute("train", new Train());
        return "app/admin/add-train";
    }

    @GetMapping("/app/admin/trains/{trainId}/edit")
    public String editTrain(@PathVariable long trainId, Model model) {
        Train train = trainService.getTrain(trainId);
        TrainUpdateRequest request = new TrainUpdateRequest();
        request.setTrainNumber(train.getTrainNumber());
        request.setTrainName(train.getTrainName());
        request.setSource(train.getSource());
        request.setDestination(train.getDestination());
        request.setSleeperFare(train.getSleeperFare());
        request.setAcFare(train.getAcFare());
        model.addAttribute("train", train);
        model.addAttribute("travelClasses", TravelClass.values());
        model.addAttribute("trainUpdateRequest", request);
        return "app/admin/edit-train";
    }

    @PostMapping("/app/admin/trains/{trainId}")
    public String updateTrain(@PathVariable long trainId,
            @Valid @ModelAttribute("trainUpdateRequest") TrainUpdateRequest request,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        Train train = trainService.getTrain(trainId);
        if (!bindingResult.hasErrors()) {
            try {
                trainService.updateTrain(trainId, request);
                redirectAttributes.addFlashAttribute("successMessage", "Train details updated.");
                return "redirect:/app/admin/trains";
            } catch (RuntimeException exception) {
                model.addAttribute("formError", exception.getMessage());
            }
        }
        model.addAttribute("train", train);
        return "app/admin/edit-train";
    }

    @PostMapping("/app/admin/trains")
    public String addTrain(@Valid @ModelAttribute("train") Train train,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "app/admin/add-train";
        }
        try {
            trainService.addTrain(train);
            redirectAttributes.addFlashAttribute("successMessage", "Train added successfully.");
            return "redirect:/app/admin/trains";
        } catch (RuntimeException exception) {
            model.addAttribute("formError", exception.getMessage());
            return "app/admin/add-train";
        }
    }

    @GetMapping("/app/admin/availability/new")
    public String newAvailability(Model model) {
        addAvailabilityFormModel(model, new SeatAvailabilityRequest());
        return "app/admin/availability-form";
    }

    @PostMapping("/app/admin/availability")
    public String addAvailability(@Valid @ModelAttribute("availabilityRequest") SeatAvailabilityRequest request,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes,
            HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            addAvailabilityFormModel(model, request);
            return "app/admin/availability-form";
        }
        try {
            availabilityService.createSeatAvailability(request);
            redirectAttributes.addFlashAttribute("successMessage", "Seat availability saved.");
            return "redirect:/app/admin/availability/new";
        } catch (RuntimeException exception) {
            addAvailabilityFormModel(model, request);
            model.addAttribute("formError", exception.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return "app/admin/availability-form";
        }
    }

    @GetMapping("/app/forbidden")
    public String forbidden() {
        return "error/403";
    }

    private void addAvailabilityFormModel(Model model, SeatAvailabilityRequest request) {
        model.addAttribute("availabilityRequest", request);
        model.addAttribute("trains", trainService.getAllTrains());
        model.addAttribute("travelClasses", TravelClass.values());
    }
}
