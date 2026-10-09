package com.baladiati.smart.service;

import com.baladiati.smart.entity.MarriageContract;
import com.baladiati.smart.entity.ContractStatus;
import com.baladiati.smart.entity.User;
import com.baladiati.smart.repository.MarriageContractRepository;
import com.baladiati.smart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MarriageContractService {

    @Autowired
    private MarriageContractRepository contractRepository;

    @Autowired
    private UserRepository userRepository;

    public MarriageContract createContract(MarriageContract contract, String officiantEmail) {
        User officiant = userRepository.findByEmail(officiantEmail)
                .orElseThrow(() -> new IllegalArgumentException("Officiant not found"));
        
        contract.setOfficiant(officiant);
        contract.setStatus(ContractStatus.PENDING_REVIEW);
        contract.setCreationDate(LocalDateTime.now());
        
        return contractRepository.save(contract);
    }

    public List<MarriageContract> getOfficiantContracts(String officiantEmail) {
        User officiant = userRepository.findByEmail(officiantEmail)
                .orElseThrow(() -> new IllegalArgumentException("Officiant not found"));
        return contractRepository.findByOfficiantOrderByCreationDateDesc(officiant);
    }

    public List<MarriageContract> getAllContracts() {
        return contractRepository.findAllByOrderByCreationDateDesc();
    }

    public MarriageContract updateContractStatus(Long contractId, ContractStatus status, String missingDocNotes) {
        MarriageContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));
        
        contract.setStatus(status);
        if (status == ContractStatus.MISSING_DOCS) {
            contract.setMissingDocNotes(missingDocNotes);
        } else {
            contract.setMissingDocNotes(null);
        }
        
        return contractRepository.save(contract);
    }
}
