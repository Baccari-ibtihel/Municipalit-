package com.baladiati.smart.controller;

import com.baladiati.smart.dto.NearestBaladiayaResponse;
import com.baladiati.smart.service.BaladiayaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class BaladiayaController {

    @Autowired
    private BaladiayaService baladiayaService;

    /**
     * Get list of all baladiyat sorted by distance from user position — PUBLIC endpoint.
     * GET /api/public/baladiyat/nearby?lat=36.8&lng=10.18
     */
    @GetMapping("/public/baladiyat/nearby")
    public ResponseEntity<?> getNearbyBaladiyat(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            return ResponseEntity.ok(baladiayaService.findAllSortedByDistance(lat, lng, limit));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get nearest baladiya to a given position — PUBLIC endpoint (no auth needed).
     * GET /api/public/nearest-baladiya?lat=36.8&lng=10.18
     */
    @GetMapping("/public/nearest-baladiya")
    public ResponseEntity<?> getNearestBaladiya(
            @RequestParam double lat,
            @RequestParam double lng) {
        try {
            NearestBaladiayaResponse response = baladiayaService.findNearest(lat, lng);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Get queue status for a specific baladiya — PUBLIC endpoint.
     * GET /api/public/baladiya/{id}/queue
     */
    @GetMapping("/public/baladiya/{id}/queue")
    public ResponseEntity<?> getQueueStatus(@PathVariable Long id) {
        try {
            NearestBaladiayaResponse response = baladiayaService.getQueueStatus(id);
            return ResponseEntity.ok(
                Map.of(
                    "queueCount", response.getQueueCount(),
                    "isOpen", response.isOpen()
                )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Increment queue (citizen arrived) — AGENT only.
     * POST /api/agent/baladiya/{id}/queue/increment
     */
    @PostMapping("/agent/baladiya/{id}/queue/increment")
    public ResponseEntity<?> incrementQueue(@PathVariable Long id) {
        try {
            int newCount = baladiayaService.incrementQueue(id);
            return ResponseEntity.ok(Map.of("queueCount", newCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Decrement queue (citizen served) — AGENT only.
     * POST /api/agent/baladiya/{id}/queue/decrement
     */
    @PostMapping("/agent/baladiya/{id}/queue/decrement")
    public ResponseEntity<?> decrementQueue(@PathVariable Long id) {
        try {
            int newCount = baladiayaService.decrementQueue(id);
            return ResponseEntity.ok(Map.of("queueCount", newCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
