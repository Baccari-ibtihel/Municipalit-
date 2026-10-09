package com.baladiati.smart.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "marriage_contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarriageContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "groom_name", nullable = false)
    private String groomName;

    @Column(name = "bride_name", nullable = false)
    private String brideName;

    @Column(name = "contract_date", nullable = false)
    private LocalDate contractDate;

    private String place;

    @Column(name = "witness_details")
    private String witnessDetails;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ContractStatus status;

    @Column(name = "missing_doc_notes")
    private String missingDocNotes;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "officiant_id", nullable = false)
    private User officiant;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "contract_id")
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();
}
