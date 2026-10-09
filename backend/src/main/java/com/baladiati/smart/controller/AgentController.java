package com.baladiati.smart.controller;

import com.baladiati.smart.entity.RequestStatus;
import com.baladiati.smart.entity.ReportStatus;
import com.baladiati.smart.entity.ContractStatus;
import com.baladiati.smart.service.DocumentRequestService;
import com.baladiati.smart.service.ReportService;
import com.baladiati.smart.service.MarriageContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    @Autowired
    private DocumentRequestService requestService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private MarriageContractService contractService;

    @GetMapping("/requests")
    public ResponseEntity<?> getAllRequests() {
        return ResponseEntity.ok(requestService.getAllRequests());
    }

    @PutMapping("/requests/{id}/status")
    public ResponseEntity<?> updateRequestStatus(
            @PathVariable Long id,
            @RequestParam RequestStatus status,
            @RequestParam(required = false) String missingDocNotes) {
        try {
            return ResponseEntity.ok(requestService.updateRequestStatus(id, status, missingDocNotes));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/reports")
    public ResponseEntity<?> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @PutMapping("/reports/{id}/status")
    public ResponseEntity<?> updateReportStatus(
            @PathVariable long id,
            @RequestParam ReportStatus status) {
        try {
            return ResponseEntity.ok(reportService.updateReportStatus(id, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/contracts")
    public ResponseEntity<?> getAllContracts() {
        return ResponseEntity.ok(contractService.getAllContracts());
    }

    @PutMapping("/contracts/{id}/status")
    public ResponseEntity<?> updateContractStatus(
            @PathVariable Long id,
            @RequestParam ContractStatus status,
            @RequestParam(required = false) String missingDocNotes) {
        try {
            return ResponseEntity.ok(contractService.updateContractStatus(id, status, missingDocNotes));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        long totalRequests = requestService.getAllRequests().size();
        long pendingRequests = requestService.getAllRequests().stream()
                .filter(r -> r.getStatus() == RequestStatus.SENT || r.getStatus() == RequestStatus.VERIFYING || r.getStatus() == RequestStatus.INCOMPLETE)
                .count();

        long totalReports = reportService.getAllReports().size();
        long unresolvedReports = reportService.getAllReports().stream()
                .filter(r -> r.getStatus() != ReportStatus.SOLVED)
                .count();

        long totalContracts = contractService.getAllContracts().size();
        long pendingContracts = contractService.getAllContracts().stream()
                .filter(c -> c.getStatus() == ContractStatus.PENDING_REVIEW || c.getStatus() == ContractStatus.MISSING_DOCS)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRequests", totalRequests);
        stats.put("pendingRequests", pendingRequests);
        stats.put("totalReports", totalReports);
        stats.put("unresolvedReports", unresolvedReports);
        stats.put("totalContracts", totalContracts);
        stats.put("pendingContracts", pendingContracts);

        return ResponseEntity.ok(stats);
    }
}
