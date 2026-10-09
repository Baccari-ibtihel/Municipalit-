package com.baladiati.smart.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "document_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // BIRTH, MARRIAGE, RESIDENCE, DEATH, OTHER

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RequestStatus status;

    private String details;

    @Column(name= "creation_date") 
    private LocalDateTime creationDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "citizen_id", nullable = false)
    private User citizen;

    @Column(name = "missing_doc_notes")
    private String missingDocNotes;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "request_id")
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();
}
