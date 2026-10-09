package com.baladiati.smart.repository;

import com.baladiati.smart.entity.DocumentRequest;
import com.baladiati.smart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRequestRepository extends JpaRepository<DocumentRequest, Long> {
    List<DocumentRequest> findByCitizenOrderByCreationDateDesc(User citizen);
    List<DocumentRequest> findAllByOrderByCreationDateDesc();
}
