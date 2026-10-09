package com.baladiati.smart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Column(name = "national_id")
    private String nationalId;
    
    private String phone;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Champs pour l'activation du compte agent
    @Column(name = "activation_code")
    private String activationCode;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true; // true par défaut pour citoyen/officiant seedés

    @Column(name = "activation_expiry")
    private LocalDateTime activationExpiry;
}
