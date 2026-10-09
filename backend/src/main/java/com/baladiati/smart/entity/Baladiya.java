package com.baladiati.smart.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "baladiyat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Baladiya {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;        // ex: بلدية أريانة / Mairie d'Ariana

    @Column(nullable = false)
    private String nameAr;      // Nom en arabe

    @Column(nullable = false)
    private String nameFr;      // Nom en français

    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    /** Heure d'ouverture: ex "08:00" */
    @Column(name = "opening_time")
    private String openingTime;

    /** Heure de fermeture: ex "16:00" */
    @Column(name = "closing_time")
    private String closingTime;

    /** Jours ouvrables: ex "Lun-Ven" */
    @Column(name = "working_days")
    private String workingDays;

    /** Nombre de personnes actuellement dans la file d'attente */
    @Column(name = "queue_count", nullable = false)
    @Builder.Default
    private Integer queueCount = 0;
}
