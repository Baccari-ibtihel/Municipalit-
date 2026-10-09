package com.baladiati.smart.repository;

import com.baladiati.smart.entity.MarriageContract;
import com.baladiati.smart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MarriageContractRepository extends JpaRepository<MarriageContract, Long> {
    List<MarriageContract> findByOfficiantOrderByCreationDateDesc(User officiant);
    List<MarriageContract> findAllByOrderByCreationDateDesc();
}
