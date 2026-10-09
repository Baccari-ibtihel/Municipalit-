package com.baladiati.smart.service;

import com.baladiati.smart.entity.DocumentRequest;
import com.baladiati.smart.entity.RequestStatus;
import com.baladiati.smart.entity.User;
import com.baladiati.smart.repository.DocumentRequestRepository;
import com.baladiati.smart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentRequestService {

    @Autowired
    private DocumentRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    public DocumentRequest createRequest(DocumentRequest request, String citizenEmail) {
        User citizen = userRepository.findByEmail(citizenEmail)
                .orElseThrow(() -> new IllegalArgumentException("Citizen not found"));
        
        request.setCitizen(citizen);
        request.setStatus(RequestStatus.SENT);
        request.setCreationDate(LocalDateTime.now());
        
        return requestRepository.save(request);
    }

    public List<DocumentRequest> getCitizenRequests(String citizenEmail) {
        User citizen = userRepository.findByEmail(citizenEmail)
                .orElseThrow(() -> new IllegalArgumentException("Citizen not found"));
        return requestRepository.findByCitizenOrderByCreationDateDesc(citizen);
    }

    public List<DocumentRequest> getAllRequests() {
        return requestRepository.findAllByOrderByCreationDateDesc();
    }

    public DocumentRequest updateRequestStatus(Long requestId, RequestStatus status, String missingDocNotes) {
        DocumentRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        request.setStatus(status);
        if (status == RequestStatus.INCOMPLETE) {
            request.setMissingDocNotes(missingDocNotes);
        } else {
            request.setMissingDocNotes(null);
        }
        
        return requestRepository.save(request);
    }
}
