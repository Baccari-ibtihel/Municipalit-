package com.baladiati.smart.controller;

import com.baladiati.smart.entity.DocumentRequest;
import com.baladiati.smart.entity.IssueReport;
import com.baladiati.smart.service.DocumentRequestService;
import com.baladiati.smart.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/citizen")
public class CitizenController {

    @Autowired
    private DocumentRequestService requestService;

    @Autowired
    private ReportService reportService;

    @GetMapping("/requests")
    public ResponseEntity<?> getMyRequests(Authentication authentication) {
        try {
            return ResponseEntity.ok(requestService.getCitizenRequests(authentication.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/requests")
    public ResponseEntity<?> createRequest(@RequestBody DocumentRequest request, Authentication authentication) {
        try {
            return ResponseEntity.ok(requestService.createRequest(request, authentication.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/reports")
    public ResponseEntity<?> getMyReports(Authentication authentication) {
        try {
            return ResponseEntity.ok(reportService.getCitizenReports(authentication.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reports")
    public ResponseEntity<?> createReport(@RequestBody IssueReport report, Authentication authentication) {
        try {
            return ResponseEntity.ok(reportService.createReport(report, authentication.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
