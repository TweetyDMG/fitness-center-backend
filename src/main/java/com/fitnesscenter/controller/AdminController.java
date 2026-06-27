package com.fitnesscenter.controller;

import com.fitnesscenter.entity.*;
import com.fitnesscenter.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Users
    @Operation(summary = "Get all users")
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @Operation(summary = "Create a new user")
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody RegisterRequest request) {
        User createdUser = adminService.createUser(
                request.getUsername(),
                request.getPassword(),
                request.getRole(),
                request.getFirstname(),
                request.getLastname(),
                request.getPatronymic(),
                request.getPhone(),
                request.getGender(),
                request.getEmail(),
                request.getPassport()
        );
        return ResponseEntity.ok(createdUser);
    }

    @Operation(summary = "Update a user")
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UpdateRequest request) {
        User updatedUser = adminService.updateUser(id, request.getUsername(), request.getRole(), request.getClient());
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Delete a user")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    // Clients
    @Operation(summary = "Get all clients")
    @GetMapping("/clients")
    public ResponseEntity<List<Client>> getAllClients() {
        return ResponseEntity.ok(adminService.getAllClients());
    }

    @Operation(summary = "Create a new client")
    @PostMapping("/clients")
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        return ResponseEntity.ok(adminService.createClient(client));
    }

    @Operation(summary = "Update a client")
    @PutMapping("/clients/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable Long id, @RequestBody Client client) {
        return ResponseEntity.ok(adminService.updateClient(id, client));
    }

    @Operation(summary = "Delete a client")
    @DeleteMapping("/clients/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        adminService.deleteClient(id);
        return ResponseEntity.ok().build();
    }

    // Discounts
    @Operation(summary = "Get all discounts")
    @GetMapping("/discounts")
    public ResponseEntity<List<Discount>> getAllDiscounts() {
        return ResponseEntity.ok(adminService.getAllDiscounts());
    }

    @Operation(summary = "Create a new discount")
    @PostMapping("/discounts")
    public ResponseEntity<Discount> createDiscount(@RequestBody Discount discount) {
        return ResponseEntity.ok(adminService.createDiscount(discount));
    }

    @Operation(summary = "Update a discount")
    @PutMapping("/discounts/{id}")
    public ResponseEntity<Discount> updateDiscount(@PathVariable Long id, @RequestBody Discount discount) {
        return ResponseEntity.ok(adminService.updateDiscount(id, discount));
    }

    @Operation(summary = "Delete a discount")
    @DeleteMapping("/discounts/{id}")
    public ResponseEntity<Void> deleteDiscount(@PathVariable Long id) {
        adminService.deleteDiscount(id);
        return ResponseEntity.ok().build();
    }

    // Diseases
    @Operation(summary = "Get all diseases")
    @GetMapping("/diseases")
    public ResponseEntity<List<Disease>> getAllDiseases() {
        return ResponseEntity.ok(adminService.getAllDiseases());
    }

    @Operation(summary = "Create a new disease")
    @PostMapping("/diseases")
    public ResponseEntity<Disease> createDisease(@RequestBody Disease disease) {
        return ResponseEntity.ok(adminService.createDisease(disease));
    }

    @Operation(summary = "Update a disease")
    @PutMapping("/diseases/{id}")
    public ResponseEntity<Disease> updateDisease(@PathVariable Long id, @RequestBody Disease disease) {
        return ResponseEntity.ok(adminService.updateDisease(id, disease));
    }

    @Operation(summary = "Delete a disease")
    @DeleteMapping("/diseases/{id}")
    public ResponseEntity<Void> deleteDisease(@PathVariable Long id) {
        adminService.deleteDisease(id);
        return ResponseEntity.ok().build();
    }

    // Disease Types
    @Operation(summary = "Get all disease types")
    @GetMapping("/disease-types")
    public ResponseEntity<List<DiseaseType>> getAllDiseaseTypes() {
        return ResponseEntity.ok(adminService.getAllDiseaseTypes());
    }

    @Operation(summary = "Create a new disease type")
    @PostMapping("/disease-types")
    public ResponseEntity<DiseaseType> createDiseaseType(@RequestBody DiseaseType diseaseType) {
        return ResponseEntity.ok(adminService.createDiseaseType(diseaseType));
    }

    @Operation(summary = "Update a disease type")
    @PutMapping("/disease-types/{id}")
    public ResponseEntity<DiseaseType> updateDiseaseType(@PathVariable Long id, @RequestBody DiseaseType diseaseType) {
        return ResponseEntity.ok(adminService.updateDiseaseType(id, diseaseType));
    }

    @Operation(summary = "Delete a disease type")
    @DeleteMapping("/disease-types/{id}")
    public ResponseEntity<Void> deleteDiseaseType(@PathVariable Long id) {
        adminService.deleteDiseaseType(id);
        return ResponseEntity.ok().build();
    }

    // Fitness Centers
    @Operation(summary = "Get all fitness centers")
    @GetMapping("/fitness-centers")
    public ResponseEntity<List<FitnessCenter>> getAllFitnessCenters() {
        return ResponseEntity.ok(adminService.getAllFitnessCenters());
    }

    @Operation(summary = "Create a new fitness center")
    @PostMapping("/fitness-centers")
    public ResponseEntity<FitnessCenter> createFitnessCenter(@RequestBody FitnessCenter fitnessCenter) {
        return ResponseEntity.ok(adminService.createFitnessCenter(fitnessCenter));
    }

    @Operation(summary = "Update a fitness center")
    @PutMapping("/fitness-centers/{id}")
    public ResponseEntity<FitnessCenter> updateFitnessCenter(@PathVariable Long id, @RequestBody FitnessCenter fitnessCenter) {
        return ResponseEntity.ok(adminService.updateFitnessCenter(id, fitnessCenter));
    }

    @Operation(summary = "Delete a fitness center")
    @DeleteMapping("/fitness-centers/{id}")
    public ResponseEntity<Void> deleteFitnessCenter(@PathVariable Long id) {
        adminService.deleteFitnessCenter(id);
        return ResponseEntity.ok().build();
    }

    // Preference Types
    @Operation(summary = "Get all preference types")
    @GetMapping("/preference-types")
    public ResponseEntity<List<PreferenceType>> getAllPreferenceTypes() {
        return ResponseEntity.ok(adminService.getAllPreferenceTypes());
    }

    @Operation(summary = "Create a new preference type")
    @PostMapping("/preference-types")
    public ResponseEntity<PreferenceType> createPreferenceType(@RequestBody PreferenceType preferenceType) {
        return ResponseEntity.ok(adminService.createPreferenceType(preferenceType));
    }

    @Operation(summary = "Update a preference type")
    @PutMapping("/preference-types/{id}")
    public ResponseEntity<PreferenceType> updatePreferenceType(@PathVariable Long id, @RequestBody PreferenceType preferenceType) {
        return ResponseEntity.ok(adminService.updatePreferenceType(id, preferenceType));
    }

    @Operation(summary = "Delete a preference type")
    @DeleteMapping("/preference-types/{id}")
    public ResponseEntity<Void> deletePreferenceType(@PathVariable Long id) {
        adminService.deletePreferenceType(id);
        return ResponseEntity.ok().build();
    }

    // Preferences
    @Operation(summary = "Get all preferences")
    @GetMapping("/preferences")
    public ResponseEntity<List<Preference>> getAllPreferences() {
        return ResponseEntity.ok(adminService.getAllPreferences());
    }

    @Operation(summary = "Create a new preference")
    @PostMapping("/preferences")
    public ResponseEntity<Preference> createPreference(@RequestBody Preference preference) {
        return ResponseEntity.ok(adminService.createPreference(preference));
    }

    @Operation(summary = "Update a preference")
    @PutMapping("/preferences/{id}")
    public ResponseEntity<Preference> updatePreference(@PathVariable Long id, @RequestBody Preference preference) {
        return ResponseEntity.ok(adminService.updatePreference(id, preference));
    }

    @Operation(summary = "Delete a preference")
    @DeleteMapping("/preferences/{id}")
    public ResponseEntity<Void> deletePreference(@PathVariable Long id) {
        adminService.deletePreference(id);
        return ResponseEntity.ok().build();
    }

    // Price History
    @Operation(summary = "Get all price history")
    @GetMapping("/price-history")
    public ResponseEntity<List<PriceHistory>> getAllPriceHistory() {
        return ResponseEntity.ok(adminService.getAllPriceHistory());
    }

    @Operation(summary = "Create a new price history")
    @PostMapping("/price-history")
    public ResponseEntity<PriceHistory> createPriceHistory(@RequestBody PriceHistory priceHistory) {
        return ResponseEntity.ok(adminService.createPriceHistory(priceHistory));
    }

    @Operation(summary = "Update a price history")
    @PutMapping("/price-history/{id}")
    public ResponseEntity<PriceHistory> updatePriceHistory(@PathVariable Long id, @RequestBody PriceHistory priceHistory) {
        return ResponseEntity.ok(adminService.updatePriceHistory(id, priceHistory));
    }

    @Operation(summary = "Delete a price history")
    @DeleteMapping("/price-history/{id}")
    public ResponseEntity<Void> deletePriceHistory(@PathVariable Long id) {
        adminService.deletePriceHistory(id);
        return ResponseEntity.ok().build();
    }

    // Registration of Visits
    @Operation(summary = "Get all registration of visits")
    @GetMapping("/visits")
    public ResponseEntity<List<RegistrationOfVisit>> getAllVisits() {
        return ResponseEntity.ok(adminService.getAllVisits());
    }

    @Operation(summary = "Create a new visit registration")
    @PostMapping("/visits")
    public ResponseEntity<RegistrationOfVisit> createVisit(@RequestBody RegistrationOfVisit visit) {
        return ResponseEntity.ok(adminService.createVisit(visit));
    }

    @Operation(summary = "Update a visit registration")
    @PutMapping("/visits/{id}")
    public ResponseEntity<RegistrationOfVisit> updateVisit(@PathVariable Long id, @RequestBody RegistrationOfVisit visit) {
        return ResponseEntity.ok(adminService.updateVisit(id, visit));
    }

    @Operation(summary = "Delete a visit registration")
    @DeleteMapping("/visits/{id}")
    public ResponseEntity<Void> deleteVisit(@PathVariable Long id) {
        adminService.deleteVisit(id);
        return ResponseEntity.ok().build();
    }

    // Sales
    @Operation(summary = "Get all sales")
    @GetMapping("/sales")
    public ResponseEntity<List<Sale>> getAllSales() {
        return ResponseEntity.ok(adminService.getAllSales());
    }

    @Operation(summary = "Create a new sale")
    @PostMapping("/sales")
    public ResponseEntity<Sale> createSale(@RequestBody Sale sale) {
        return ResponseEntity.ok(adminService.createSale(sale));
    }

    @Operation(summary = "Update a sale")
    @PutMapping("/sales/{id}")
    public ResponseEntity<Sale> updateSale(@PathVariable Long id, @RequestBody Sale sale) {
        return ResponseEntity.ok(adminService.updateSale(id, sale));
    }

    @Operation(summary = "Delete a sale")
    @DeleteMapping("/sales/{id}")
    public ResponseEntity<Void> deleteSale(@PathVariable Long id) {
        adminService.deleteSale(id);
        return ResponseEntity.ok().build();
    }

    // Schedule
    @Operation(summary = "Get all schedule entries")
    @GetMapping("/schedule")
    public ResponseEntity<List<Schedule>> getAllSchedules() {
        return ResponseEntity.ok(adminService.getAllSchedules());
    }

    @Operation(summary = "Create a new schedule entry")
    @PostMapping("/schedule")
    public ResponseEntity<Schedule> createSchedule(@RequestBody Schedule schedule) {
        return ResponseEntity.ok(adminService.createSchedule(schedule));
    }

    @Operation(summary = "Update a schedule entry")
    @PutMapping("/schedule/{id}")
    public ResponseEntity<Schedule> updateSchedule(@PathVariable Long id, @RequestBody Schedule schedule) {
        return ResponseEntity.ok(adminService.updateSchedule(id, schedule));
    }

    @Operation(summary = "Delete a schedule entry")
    @DeleteMapping("/schedule/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        adminService.deleteSchedule(id);
        return ResponseEntity.ok().build();
    }

    // Services
    @Operation(summary = "Get all services")
    @GetMapping("/services")
    public ResponseEntity<List<FitnessService>> getAllFitnessServices() {
        return ResponseEntity.ok(adminService.getAllFitnessServices());
    }

    @Operation(summary = "Create a new service")
    @PostMapping("/services")
    public ResponseEntity<FitnessService> createFitnessService(@RequestBody FitnessService fitnessService) {
        return ResponseEntity.ok(adminService.createFitnessService(fitnessService));
    }

    @Operation(summary = "Update a service")
    @PutMapping("/services/{id}")
    public ResponseEntity<FitnessService> updateFitnessService(@PathVariable Long id, @RequestBody FitnessService fitnessService) {
        return ResponseEntity.ok(adminService.updateFitnessService(id, fitnessService));
    }

    @Operation(summary = "Delete a service")
    @DeleteMapping("/services/{id}")
    public ResponseEntity<Void> deleteFitnessService(@PathVariable Long id) {
        adminService.deleteFitnessService(id);
        return ResponseEntity.ok().build();
    }

    // Subscriptions
    @Operation(summary = "Get all subscriptions")
    @GetMapping("/subscriptions")
    public ResponseEntity<List<Subscription>> getAllSubscriptions() {
        return ResponseEntity.ok(adminService.getAllSubscriptions());
    }

    @Operation(summary = "Create a new subscription")
    @PostMapping("/subscriptions")
    public ResponseEntity<Subscription> createSubscription(@RequestBody Subscription subscription) {
        return ResponseEntity.ok(adminService.createSubscription(subscription));
    }

    @Operation(summary = "Update a subscription")
    @PutMapping("/subscriptions/{id}")
    public ResponseEntity<Subscription> updateSubscription(@PathVariable Long id, @RequestBody Subscription subscription) {
        return ResponseEntity.ok(adminService.updateSubscription(id, subscription));
    }

    @Operation(summary = "Delete a subscription")
    @DeleteMapping("/subscriptions/{id}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable Long id) {
        adminService.deleteSubscription(id);
        return ResponseEntity.ok().build();
    }

    // Trainers
    @Operation(summary = "Get all trainers")
    @GetMapping("/trainers")
    public ResponseEntity<List<Trainer>> getAllTrainers() {
        return ResponseEntity.ok(adminService.getAllTrainers());
    }

    @Operation(summary = "Create a new trainer")
    @PostMapping("/trainers")
    public ResponseEntity<Trainer> createTrainer(@RequestBody Trainer trainer) {
        return ResponseEntity.ok(adminService.createTrainer(trainer));
    }

    @Operation(summary = "Update a trainer")
    @PutMapping("/trainers/{id}")
    public ResponseEntity<Trainer> updateTrainer(@PathVariable Long id, @RequestBody Trainer trainer) {
        return ResponseEntity.ok(adminService.updateTrainer(id, trainer));
    }

    @Operation(summary = "Delete a trainer")
    @DeleteMapping("/trainers/{id}")
    public ResponseEntity<Void> deleteTrainer(@PathVariable Long id) {
        adminService.deleteTrainer(id);
        return ResponseEntity.ok().build();
    }
}