package com.fitnesscenter.controller;

import com.fitnesscenter.dto.ClientFilterRequest;
import com.fitnesscenter.dto.ScheduleRequest;
import com.fitnesscenter.dto.SaleFilterRequest;
import com.fitnesscenter.entity.Client;
import com.fitnesscenter.entity.Schedule;
import com.fitnesscenter.entity.Sale;
import com.fitnesscenter.entity.Trainer;
import com.fitnesscenter.exception.InvalidRequestException;
import com.fitnesscenter.service.ManagerService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    private final ManagerService managerService;

    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    // --- CLIENTS ---

    @GetMapping("/clients")
    public ResponseEntity<Page<Client>> getAllClients(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastname") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder) {

        ClientFilterRequest filter = new ClientFilterRequest();
        filter.setName(name);
        filter.setPhone(phone);
        filter.setEmail(email);
        filter.setSortField(sortField);
        filter.setSortOrder(sortOrder);

        Page<Client> clients = managerService.getAllClientsWithFilter(filter, page, size);
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/clients/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable Long id) {
        Client client = managerService.getClientById(id);
        return ResponseEntity.ok(client);
    }

    @PutMapping("/clients/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable Long id, @RequestBody Client updatedClient) {
        Client client = managerService.updateClient(id, updatedClient);
        return ResponseEntity.ok(client);
    }

    // --- SALES ---

    @GetMapping("/sales")
    public ResponseEntity<Page<Sale>> getAllSales(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long fitnessCenterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startDate") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder) {

        if ((fromDate != null && toDate != null) && fromDate.isAfter(toDate)) {
            throw new InvalidRequestException("Дата начала не может быть позже даты окончания");
        }

        SaleFilterRequest filter = new SaleFilterRequest();
        filter.setClientId(clientId);
        filter.setFitnessCenterId(fitnessCenterId);
        filter.setFromDate(fromDate);
        filter.setToDate(toDate);
        filter.setSortField(sortField);
        filter.setSortOrder(sortOrder);

        Page<Sale> sales = managerService.getAllSalesWithFilter(filter, page, size);
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/sales/{id}")
    public ResponseEntity<Sale> getSaleById(@PathVariable Long id) {
        Sale sale = managerService.getSaleById(id);
        return ResponseEntity.ok(sale);
    }

    @PostMapping("/sales")
    public ResponseEntity<Sale> createSale(@RequestBody SaleRequest saleRequest) {
        Sale sale = managerService.createSale(saleRequest);
        return ResponseEntity.ok(sale);
    }

    // --- TRAINERS ---

    @GetMapping("/trainers")
    public ResponseEntity<List<Trainer>> getAllTrainers() {
        List<Trainer> trainers = managerService.getAllTrainers();
        return ResponseEntity.ok(trainers);
    }

    @GetMapping("/trainers/{id}")
    public ResponseEntity<Trainer> getTrainerById(@PathVariable Long id) {
        Trainer trainer = managerService.getTrainerById(id);
        return ResponseEntity.ok(trainer);
    }

    // --- SCHEDULE ---

    @GetMapping("/trainers/{trainerId}/schedule")
    public ResponseEntity<List<Schedule>> getTrainerSchedule(@PathVariable Long trainerId) {
        List<Schedule> schedule = managerService.getScheduleByTrainerId(trainerId);
        return ResponseEntity.ok(schedule);
    }

    @PostMapping("/trainers/{trainerId}/schedule")
    public ResponseEntity<Schedule> addScheduleToTrainer(@PathVariable Long trainerId,
                                                         @RequestBody ScheduleRequest request) {
        Schedule schedule = managerService.addScheduleToTrainer(trainerId, request);
        return ResponseEntity.ok(schedule);
    }

    // --- CLIENT - TRAINER ASSIGNMENT ---

    @PostMapping("/trainers/{trainerId}/assign-client/{clientId}/schedule/{scheduleId}")
    public ResponseEntity<Void> assignClientToTrainer(
            @PathVariable Long trainerId,
            @PathVariable Long clientId,
            @PathVariable Long scheduleId) {
        managerService.assignClientToTrainer(trainerId, clientId, scheduleId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/visits/{visitId}")
    public ResponseEntity<Void> cancelClientVisit(@PathVariable Long visitId) {
        managerService.cancelClientVisit(visitId);
        return ResponseEntity.ok().build();
    }
}