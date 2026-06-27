package com.fitnesscenter.controller;

import com.fitnesscenter.entity.*;
import com.fitnesscenter.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainer")
public class TrainerController {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    // --- CLIENTS ---

    @Operation(summary = "Get all clients")
    @GetMapping("/clients")
    public ResponseEntity<List<Client>> getAllClients() {
        List<Client> clients = trainerService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @Operation(summary = "Get client preferences")
    @GetMapping("/clients/{clientId}/preferences")
    public ResponseEntity<List<Preference>> getClientPreferences(@PathVariable Long clientId) {
        List<Preference> preferences = trainerService.getClientPreferences(clientId);
        return ResponseEntity.ok(preferences);
    }

    @Operation(summary = "Get client diseases")
    @GetMapping("/clients/{clientId}/diseases")
    public ResponseEntity<List<Disease>> getClientDiseases(@PathVariable Long clientId) {
        List<Disease> diseases = trainerService.getClientDiseases(clientId);
        return ResponseEntity.ok(diseases);
    }

    // --- SCHEDULE ---

    @Operation(summary = "Get trainer's schedule")
    @GetMapping("/schedule")
    public ResponseEntity<List<Schedule>> getSchedule(@RequestParam Long trainerId) {
        List<Schedule> schedule = trainerService.getMySchedule(trainerId);
        return ResponseEntity.ok(schedule);
    }

    @Operation(summary = "Add new training session to schedule")
    @PostMapping("/schedule")
    public ResponseEntity<Schedule> addSchedule(@RequestBody Schedule schedule) {
        Schedule created = trainerService.createSchedule(schedule);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Delete training session from schedule")
    @DeleteMapping("/schedule/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long scheduleId) {
        trainerService.deleteScheduleById(scheduleId);
        return ResponseEntity.ok().build();
    }

    // --- VISITS ---

    @Operation(summary = "Get list of clients registered for a training session")
    @GetMapping("/schedule/{scheduleId}/registrations")
    public ResponseEntity<List<RegistrationOfVisit>> getClientsOnTraining(@PathVariable Long scheduleId) {
        List<RegistrationOfVisit> registrations = trainerService.getVisitsByScheduleId(scheduleId);
        return ResponseEntity.ok(registrations);
    }

    // --- RECOMMENDATIONS ---

    @Operation(summary = "Get recommendations for a client")
    @GetMapping("/clients/{clientId}/recommendations")
    public ResponseEntity<List<Recommendation>> getClientRecommendations(@PathVariable Long clientId) {
        List<Recommendation> recommendations = trainerService.getClientRecommendations(clientId);
        return ResponseEntity.ok(recommendations);
    }

    @Operation(summary = "Add a new recommendation for a client")
    @PostMapping("/clients/{clientId}/recommendations")
    public ResponseEntity<Recommendation> addRecommendation(@PathVariable Long clientId,
                                                            @RequestBody Recommendation recommendation) {
        recommendation.setClientId(clientId);
        Recommendation saved = trainerService.addRecommendation(recommendation);
        return ResponseEntity.ok(saved);
    }
}