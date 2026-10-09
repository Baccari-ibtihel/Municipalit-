package com.baladiati.smart.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearestBaladiayaResponse {
    private Long id;
    private String name;
    private String nameAr;
    private String nameFr;
    private String address;
    private Double latitude;
    private Double longitude;
    private String openingTime;
    private String closingTime;
    private String workingDays;
    private Integer queueCount;
    private boolean isOpen;
    private Double distanceKm;
    private String crowdStatus; // LOW, MEDIUM, HIGH
}
