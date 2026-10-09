package com.baladiati.smart.controller;

import com.baladiati.smart.entity.MarriageContract;
import com.baladiati.smart.service.MarriageContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/officiant")
public class OfficiantController {

    @Autowired
    private MarriageContractService contractService;

    @GetMapping("/contracts")
    public ResponseEntity<?> getMyContracts(Authentication authentication) {
        try {
            return ResponseEntity.ok(contractService.getOfficiantContracts(authentication.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/contracts")
    public ResponseEntity<?> createContract(@RequestBody MarriageContract contract, Authentication authentication) {
        try {
            return ResponseEntity.ok(contractService.createContract(contract, authentication.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
