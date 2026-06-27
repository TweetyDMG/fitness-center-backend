package com.fitnesscenter.controller;

import com.fitnesscenter.entity.*;
import com.fitnesscenter.repository.UserRepository;
import com.fitnesscenter.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    private final ClientService clientService;
    private final UserRepository userRepository;

    public ClientController(ClientService clientService, UserRepository userRepository) {
        this.clientService = clientService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Get client profile")
    @GetMapping("/profile")
    public ResponseEntity<Client> getProfile() {
        Long clientId = getCurrentClientId();
        Client client = clientService.getClientProfile(clientId);
        return ResponseEntity.ok(client);
    }

    @Operation(summary = "Get client subscriptions")
    @GetMapping("/subscriptions")
    public ResponseEntity<List<Sale>> getSubscriptions() {
        Long clientId = getCurrentClientId();
        List<Sale> subscriptions = clientService.getClientSubscriptions(clientId);
        return ResponseEntity.ok(subscriptions);
    }

    @Operation(summary = "Get client preferences")
    @GetMapping("/preferences")
    public ResponseEntity<List<Preference>> getPreferences() {
        Long clientId = getCurrentClientId();
        List<Preference> preferences = clientService.getClientPreferences(clientId);
        return ResponseEntity.ok(preferences);
    }

    @Operation(summary = "Get client diseases")
    @GetMapping("/diseases")
    public ResponseEntity<List<Disease>> getDiseases() {
        Long clientId = getCurrentClientId();
        List<Disease> diseases = clientService.getClientDiseases(clientId);
        return ResponseEntity.ok(diseases);
    }

    @Operation(summary = "Get client visit history")
    @GetMapping("/visits")
    public ResponseEntity<List<RegistrationOfVisit>> getVisits() {
        Long clientId = getCurrentClientId();
        List<RegistrationOfVisit> visits = clientService.getClientVisits(clientId);
        return ResponseEntity.ok(visits);
    }

    @Operation(summary = "Get all available training schedule")
    @GetMapping("/schedule")
    public ResponseEntity<List<Schedule>> getSchedule() {
        List<Schedule> schedule = clientService.getClientSchedule();
        return ResponseEntity.ok(schedule);
    }

    @Operation(summary = "Register for a training session")
    @PostMapping("/visits/{scheduleId}")
    public ResponseEntity<Void> registerForVisit(@PathVariable Long scheduleId) {
        Long clientId = getCurrentClientId();
        clientService.registerForVisit(clientId, scheduleId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Cancel a registered visit")
    @DeleteMapping("/visits/{visitId}")
    public ResponseEntity<Void> cancelVisit(@PathVariable Long visitId) {
        Long clientId = getCurrentClientId();
        clientService.cancelVisit(clientId, visitId);
        return ResponseEntity.ok().build();
    }

    private Long getCurrentClientId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            throw new RuntimeException("Unknown principal type");
        }

        User user = userRepository.findByUsername(username);
        if (user == null || user.getClient() == null) {
            throw new RuntimeException("User or client not found");
        }

        return user.getClient().getId();
    }
}