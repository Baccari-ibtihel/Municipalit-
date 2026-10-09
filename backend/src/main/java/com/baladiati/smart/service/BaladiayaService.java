package com.baladiati.smart.service;

import com.baladiati.smart.dto.NearestBaladiayaResponse;
import com.baladiati.smart.entity.Baladiya;
import com.baladiati.smart.repository.BaladiayaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BaladiayaService {

    @Autowired
    private BaladiayaRepository baladiayaRepository;

    /**
     * Find the nearest Baladiya to the given coordinates using Haversine formula.
     */
    public NearestBaladiayaResponse findNearest(double userLat, double userLng) {
        List<Baladiya> all = baladiayaRepository.findAll();
        if (all.isEmpty()) {
            throw new RuntimeException("No baladiyat found in database");
        }

        Baladiya nearest = null;
        double minDist = Double.MAX_VALUE;

        for (Baladiya b : all) {
            double dist = haversineKm(userLat, userLng, b.getLatitude(), b.getLongitude());
            if (dist < minDist) {
                minDist = dist;
                nearest = b;
            }
        }

        return buildResponse(nearest, minDist);
    }

    /**
     * Get queue status for a specific baladiya.
     */
    public NearestBaladiayaResponse getQueueStatus(Long id) {
        Baladiya b = baladiayaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Baladiya not found"));
        return buildResponse(b, null);
    }

    /**
     * Increment queue count (called by agent when citizen arrives).
     */
    @Transactional
    public int incrementQueue(Long id) {
        Baladiya b = baladiayaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Baladiya not found"));
        int newCount = b.getQueueCount() + 1;
        b.setQueueCount(newCount);
        baladiayaRepository.save(b);
        return newCount;
    }

    /**
     * Decrement queue count (called by agent when citizen is served).
     */
    @Transactional
    public int decrementQueue(Long id) {
        Baladiya b = baladiayaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Baladiya not found"));
        int newCount = Math.max(0, b.getQueueCount() - 1);
        b.setQueueCount(newCount);
        baladiayaRepository.save(b);
        return newCount;
    }

    /**
     * Find nearest baladiyat sorted by distance from the user, limited to top N results.
     */
    public List<NearestBaladiayaResponse> findAllSortedByDistance(double userLat, double userLng, int limit) {
        List<Baladiya> all = baladiayaRepository.findAll();
        var stream = all.stream()
                .map(b -> {
                    double dist = haversineKm(userLat, userLng, b.getLatitude(), b.getLongitude());
                    return buildResponse(b, dist);
                })
                .sorted((b1, b2) -> Double.compare(
                        b1.getDistanceKm() != null ? b1.getDistanceKm() : 0.0,
                        b2.getDistanceKm() != null ? b2.getDistanceKm() : 0.0
                ));
        if (limit > 0) {
            stream = stream.limit(limit);
        }
        return stream.toList();
    }

    // ─── Helpers ───────────────────────────────────────────────

    private NearestBaladiayaResponse buildResponse(Baladiya b, Double distanceKm) {
        boolean open = isCurrentlyOpen(b);
        int q = open ? (b.getQueueCount() != null ? b.getQueueCount() : 0) : 0;
        String crowd = open ? (q <= 3 ? "LOW" : (q <= 8 ? "MEDIUM" : "HIGH")) : "CLOSED";

        return NearestBaladiayaResponse.builder()
                .id(b.getId())
                .name(b.getName())
                .nameAr(b.getNameAr())
                .nameFr(b.getNameFr())
                .address(b.getAddress())
                .latitude(b.getLatitude())
                .longitude(b.getLongitude())
                .openingTime(b.getOpeningTime())
                .closingTime(b.getClosingTime())
                .workingDays(b.getWorkingDays())
                .queueCount(q)
                .isOpen(open)
                .distanceKm(distanceKm != null ? Math.round(distanceKm * 10.0) / 10.0 : null)
                .crowdStatus(crowd)
                .build();
    }

    /**
     * Check if baladiya is currently open based on local time and schedule.
     */
    private boolean isCurrentlyOpen(Baladiya b) {
        try {
            LocalTime now = LocalTime.now();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime open = LocalTime.parse(b.getOpeningTime(), fmt);
            LocalTime close = LocalTime.parse(b.getClosingTime(), fmt);
            return !now.isBefore(open) && now.isBefore(close);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Haversine formula — returns distance in kilometers.
     */
    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
