package com.fitnesscenter.service;

import com.fitnesscenter.entity.*;
import com.fitnesscenter.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final DiscountRepository discountRepository;
    private final DiseaseRepository diseaseRepository;
    private final DiseaseTypeRepository diseaseTypeRepository;
    private final FitnessCenterRepository fitnessCenterRepository;
    private final PreferenceTypeRepository preferenceTypeRepository;
    private final PreferenceRepository preferenceRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final RegistrationOfVisitRepository visitRepository;
    private final SaleRepository saleRepository;
    private final ScheduleRepository scheduleRepository;
    private final ServiceRepository serviceRepository;
    private final TrainerRepository trainerRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository, ClientRepository clientRepository,
                        SubscriptionRepository subscriptionRepository, DiscountRepository discountRepository,
                        DiseaseRepository diseaseRepository, DiseaseTypeRepository diseaseTypeRepository,
                        FitnessCenterRepository fitnessCenterRepository, PreferenceTypeRepository preferenceTypeRepository,
                        PreferenceRepository preferenceRepository, PriceHistoryRepository priceHistoryRepository,
                        RegistrationOfVisitRepository visitRepository, SaleRepository saleRepository,
                        ScheduleRepository scheduleRepository, ServiceRepository serviceRepository,
                        TrainerRepository trainerRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.discountRepository = discountRepository;
        this.diseaseRepository = diseaseRepository;
        this.diseaseTypeRepository = diseaseTypeRepository;
        this.fitnessCenterRepository = fitnessCenterRepository;
        this.preferenceTypeRepository = preferenceTypeRepository;
        this.preferenceRepository = preferenceRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.visitRepository = visitRepository;
        this.saleRepository = saleRepository;
        this.scheduleRepository = scheduleRepository;
        this.serviceRepository = serviceRepository;
        this.trainerRepository = trainerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User createUser(String username, String password, String role,
                           String firstname, String lastname, String patronymic,
                           String phone, String gender, String email, String passport) {
        if (userRepository.findByUsername(username) != null) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);

        if ("CLIENT".equals(role)) {
            Client client = new Client();
            client.setFirstname(firstname);
            client.setLastname(lastname);
            client.setPatronymic(patronymic);
            client.setPhone(phone);
            client.setGender(gender);
            client.setEmail(email);
            client.setPassport(passport);
            client = clientRepository.save(client);
            user.setClient(client);
        }

        if ("TRAINER".equals(role)){
            Trainer trainer = new Trainer();
            trainer.setFirstname(firstname);
            trainer.setPatronymic(patronymic);
            trainer.setLastname(lastname);
            trainer.setPhoneNumber(phone);
            //trainer.setAddress(address);
            trainer = trainerRepository.save(trainer);
            user.setTrainer(trainer);
        }

        return userRepository.save(user);
    }

    public User updateUser(Long id, String username, String role, Client client) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setUsername(username);
        user.setRole(role);
        user.setClient(client);
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Clients
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client createClient(Client client) {
        return clientRepository.save(client);
    }

    public Client updateClient(Long id, Client client) {
        Client existing = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        existing.setFirstname(client.getFirstname());
        existing.setLastname(client.getLastname());
        existing.setPatronymic(client.getPatronymic());
        existing.setPhone(client.getPhone());
        existing.setGender(client.getGender());
        existing.setEmail(client.getEmail());
        existing.setPassport(client.getPassport());
        return clientRepository.save(existing);
    }

    @Transactional
    public void deleteClient(Long id) {
        // Проверка зависимостей
        List<Sale> sales = saleRepository.findByClientId(id);
        List<Long> saleIds = sales.stream().map(Sale::getId).toList();
        List<RegistrationOfVisit> visits = visitRepository.findBySaleIdIn(saleIds);
        List<Disease> diseases = diseaseRepository.findByClientId(id);

        if (!sales.isEmpty() || !visits.isEmpty() || !diseases.isEmpty()) {
            throw new RuntimeException("Cannot delete client with associated sales, visits, or diseases");
        }

        clientRepository.deleteById(id);
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Discounts
    public List<Discount> getAllDiscounts() {
        return discountRepository.findAll();
    }

    public Discount createDiscount(Discount discount) {
        return discountRepository.save(discount);
    }

    public Discount updateDiscount(Long id, Discount discount) {
        Discount existing = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found"));
        existing.setName(discount.getName());
        existing.setPercentage(discount.getPercentage());
        return discountRepository.save(existing);
    }

    public void deleteDiscount(Long id) {
        discountRepository.deleteById(id);
    }

    // Diseases
    public List<Disease> getAllDiseases() {
        return diseaseRepository.findAll();
    }

    public Disease createDisease(Disease disease) {
        return diseaseRepository.save(disease);
    }

    public Disease updateDisease(Long id, Disease disease) {
        Disease existing = diseaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Disease not found"));
        existing.setDiseaseType(disease.getDiseaseType());
        existing.setClient(disease.getClient());
        return diseaseRepository.save(existing);
    }

    public void deleteDisease(Long id) {
        diseaseRepository.deleteById(id);
    }

    // Disease Types
    public List<DiseaseType> getAllDiseaseTypes() {
        return diseaseTypeRepository.findAll();
    }

    public DiseaseType createDiseaseType(DiseaseType diseaseType) {
        return diseaseTypeRepository.save(diseaseType);
    }

    public DiseaseType updateDiseaseType(Long id, DiseaseType diseaseType) {
        DiseaseType existing = diseaseTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DiseaseType not found"));
        existing.setDiseaseName(diseaseType.getDiseaseName());
        return diseaseTypeRepository.save(existing);
    }

    public void deleteDiseaseType(Long id) {
        diseaseTypeRepository.deleteById(id);
    }

    // Fitness Centers
    public List<FitnessCenter> getAllFitnessCenters() {
        return fitnessCenterRepository.findAll();
    }

    public FitnessCenter createFitnessCenter(FitnessCenter fitnessCenter) {
        return fitnessCenterRepository.save(fitnessCenter);
    }

    public FitnessCenter updateFitnessCenter(Long id, FitnessCenter fitnessCenter) {
        FitnessCenter existing = fitnessCenterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FitnessCenter not found"));
        existing.setName(fitnessCenter.getName());
        existing.setAddress(fitnessCenter.getAddress());
        return fitnessCenterRepository.save(existing);
    }

    public void deleteFitnessCenter(Long id) {
        fitnessCenterRepository.deleteById(id);
    }

    // Preference Types
    public List<PreferenceType> getAllPreferenceTypes() {
        return preferenceTypeRepository.findAll();
    }

    public PreferenceType createPreferenceType(PreferenceType preferenceType) {
        return preferenceTypeRepository.save(preferenceType);
    }

    public PreferenceType updatePreferenceType(Long id, PreferenceType preferenceType) {
        PreferenceType existing = preferenceTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PreferenceType not found"));
        existing.setPreferenceName(preferenceType.getPreferenceName());
        return preferenceTypeRepository.save(existing);
    }

    public void deletePreferenceType(Long id) {
        preferenceTypeRepository.deleteById(id);
    }

    // Preferences
    public List<Preference> getAllPreferences() {
        return preferenceRepository.findAll();
    }

    public Preference createPreference(Preference preference) {
        return preferenceRepository.save(preference);
    }

    public Preference updatePreference(Long id, Preference preference) {
        Preference existing = preferenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Preference not found"));
        existing.setId(preference.getId());
        existing.setClient(preference.getClient());
        return preferenceRepository.save(existing);
    }

    public void deletePreference(Long id) {
        preferenceRepository.deleteById(id);
    }

    // Price History
    public List<PriceHistory> getAllPriceHistory() {
        return priceHistoryRepository.findAll();
    }

    public PriceHistory createPriceHistory(PriceHistory priceHistory) {
        return priceHistoryRepository.save(priceHistory);
    }

    public PriceHistory updatePriceHistory(Long id, PriceHistory priceHistory) {
        PriceHistory existing = priceHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PriceHistory not found"));
        existing.setSubscription(priceHistory.getSubscription());
        existing.setPrice(priceHistory.getPrice());
        existing.setStartDate(priceHistory.getStartDate());
        existing.setEndDate(priceHistory.getEndDate());
        return priceHistoryRepository.save(existing);
    }

    public void deletePriceHistory(Long id) {
        priceHistoryRepository.deleteById(id);
    }

    // Registration of Visits
    public List<RegistrationOfVisit> getAllVisits() {
        return visitRepository.findAll();
    }

    public RegistrationOfVisit createVisit(RegistrationOfVisit visit) {
        return visitRepository.save(visit);
    }

    public RegistrationOfVisit updateVisit(Long id, RegistrationOfVisit visit) {
        RegistrationOfVisit existing = visitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visit not found"));
        existing.setVisitDate(visit.getVisitDate());
        existing.setScheduleId(visit.getScheduleId());
        existing.setSaleId(visit.getSaleId());
        return visitRepository.save(existing);
    }

    public void deleteVisit(Long id) {
        visitRepository.deleteById(id);
    }

    // Sales
    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    public Sale createSale(Sale sale) {
        return saleRepository.save(sale);
    }

    public Sale updateSale(Long id, Sale sale) {
        Sale existing = saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
        existing.setBankCardNum(sale.getBankCardNum());
        existing.setStartDate(sale.getStartDate());
        existing.setEndDate(sale.getEndDate());
        existing.setClient(sale.getClient());
        existing.setDiscount(sale.getDiscount());
        existing.setSubscription(sale.getSubscription());
        existing.setFitnessCenter(sale.getFitnessCenter());
        return saleRepository.save(existing);
    }

    public void deleteSale(Long id) {
        saleRepository.deleteById(id);
    }

    // Schedule
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    public Schedule createSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    public Schedule updateSchedule(Long id, Schedule schedule) {
        Schedule existing = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        existing.setDate(schedule.getDate());
        existing.setStartTime(schedule.getStartTime());
        existing.setEndTime(schedule.getEndTime());
        existing.setTrainer(schedule.getTrainer());
        return scheduleRepository.save(existing);
    }

    public void deleteSchedule(Long id) {
        scheduleRepository.deleteById(id);
    }

    // Services
    public List<FitnessService> getAllFitnessServices() {
        return serviceRepository.findAll();
    }

    public FitnessService createFitnessService(FitnessService service) {
        return serviceRepository.save(service);
    }

    public FitnessService updateFitnessService(Long id, FitnessService service) {
        FitnessService existing = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        existing.setName(service.getName());
        existing.setStartTime(service.getStartTime());
        existing.setEndTime(service.getEndTime());
        return serviceRepository.save(existing);
    }

    public void deleteFitnessService(Long id) {
        serviceRepository.deleteById(id);
    }

    // Subscriptions
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    public Subscription createSubscription(Subscription subscription) {
        return subscriptionRepository.save(subscription);
    }

    public Subscription updateSubscription(Long id, Subscription subscription) {
        Subscription existing = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        existing.setName(subscription.getName());
        existing.setPrice(subscription.getPrice());
        existing.setDuration(subscription.getDuration());
        existing.setValidity(subscription.getValidity());
        existing.setNumberOfVisits(subscription.getNumberOfVisits());
        existing.setNumberOfDay(subscription.getNumberOfDay());
        existing.setFitnessService(subscription.getFitnessService());
        return subscriptionRepository.save(existing);
    }

    public void deleteSubscription(Long id) {
        subscriptionRepository.deleteById(id);
    }

    // Trainers
    public List<Trainer> getAllTrainers() {
        return trainerRepository.findAll();
    }

    public Trainer createTrainer(Trainer trainer) {
        return trainerRepository.save(trainer);
    }

    public Trainer updateTrainer(Long id, Trainer trainer) {
        Trainer existing = trainerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));
        existing.setFirstname(trainer.getFirstname());
        existing.setPatronymic(trainer.getPatronymic());
        existing.setLastname(trainer.getLastname());
        existing.setPhoneNumber(trainer.getPhoneNumber());
        existing.setAddress(trainer.getAddress());
        return trainerRepository.save(existing);
    }

    public void deleteTrainer(Long id) {
        trainerRepository.deleteById(id);
    }
}