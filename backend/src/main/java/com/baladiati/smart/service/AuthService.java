package com.baladiati.smart.service;

import com.baladiati.smart.config.JwtTokenProvider;
import com.baladiati.smart.dto.AgentActivationRequest;
import com.baladiati.smart.dto.AgentRegistrationRequest;
import com.baladiati.smart.dto.AuthResponse;
import com.baladiati.smart.dto.LoginRequest;
import com.baladiati.smart.dto.RegisterRequest;
import com.baladiati.smart.entity.Role;
import com.baladiati.smart.entity.User;
import com.baladiati.smart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmailService emailService;

    public User registerCitizen(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        User citizen = User.builder()
                .name(request.getName())
                .nationalId(request.getNationalId())
                .phone(request.getPhone())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_CITIZEN)
                .isActive(true)
                .build();

        return userRepository.save(citizen);
    }

    // ─── Inscription Agent ─────────────────────────────────────────────────────

    public String registerAgent(AgentRegistrationRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
        }

        // Générer un code unique à 6 chiffres
        String code = String.format("%06d", new Random().nextInt(999999));

        User agent = User.builder()
                .name(request.getName())
                .nationalId(request.getNationalId())
                .phone(request.getPhone())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_AGENT)
                .isActive(false)                                   // compte inactif jusqu'à activation
                .activationCode(code)
                .activationExpiry(LocalDateTime.now().plusMinutes(15))
                .build();

        userRepository.save(agent);

        // Envoyer le code par email
        emailService.sendActivationCode(request.getEmail(), request.getName(), code);

        return "Code d'activation envoyé à " + request.getEmail();
    }

    // ─── Activation Agent ──────────────────────────────────────────────────────

    public String activateAgent(AgentActivationRequest request) {
        User agent = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Aucun compte trouvé avec cet email."));

        if (agent.isActive()) {
            throw new IllegalArgumentException("Ce compte est déjà activé.");
        }

        if (agent.getActivationCode() == null || !agent.getActivationCode().equals(request.getActivationCode())) {
            throw new IllegalArgumentException("Code d'activation incorrect.");
        }

        if (agent.getActivationExpiry() == null || LocalDateTime.now().isAfter(agent.getActivationExpiry())) {
            throw new IllegalArgumentException("Le code d'activation a expiré. Veuillez vous réinscrire.");
        }

        if (!passwordEncoder.matches(request.getPassword(), agent.getPassword())) {
            throw new IllegalArgumentException("Mot de passe incorrect.");
        }

        // Activer le compte mais conserver le code d'activation (qui devient le matricule pro)
        agent.setActive(true);
        agent.setActivationExpiry(null);
        userRepository.save(agent);

        return "Compte agent activé avec succès ! Vous pouvez maintenant vous connecter.";
    }

    // ─── Connexion ─────────────────────────────────────────────────────────────

    public AuthResponse authenticateUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé."));

        if (!user.isActive()) {
            throw new IllegalArgumentException("Votre compte n'est pas encore activé. Vérifiez votre email.");
        }

        // Vérification de sécurité supplémentaire pour les professionnels
        if (user.getRole() == Role.ROLE_AGENT || user.getRole() == Role.ROLE_OFFICIANT) {
            String uniqueCode = request.getUniqueCode();
            if (uniqueCode == null || uniqueCode.trim().isEmpty()) {
                throw new IllegalArgumentException("L'identifiant unique (code à 6 chiffres) est requis pour les professionnels.");
            }
            if (!uniqueCode.equals(user.getActivationCode())) {
                throw new IllegalArgumentException("Identifiant unique incorrect.");
            }
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(jwt)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}

