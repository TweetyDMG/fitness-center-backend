package com.fitnesscenter.service;

import com.fitnesscenter.config.JwtAuthenticationFilter;
import com.fitnesscenter.entity.Client;
import com.fitnesscenter.entity.User;
import com.fitnesscenter.repository.ClientRepository;
import com.fitnesscenter.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public AuthService(UserRepository userRepository, ClientRepository clientRepository,
                       PasswordEncoder passwordEncoder, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPasswordHash())) {
            return jwtAuthenticationFilter.generateToken(username);
        }
        throw new RuntimeException("Invalid credentials");
    }

    @Transactional
    public void register(String username, String password, String role,
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

        userRepository.save(user);
    }
}