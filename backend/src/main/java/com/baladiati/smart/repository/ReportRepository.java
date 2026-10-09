package com.baladiati.smart.repository;

import com.baladiati.smart.entity.IssueReport;
import com.baladiati.smart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<IssueReport, Long> {
    List<IssueReport> findByCitizenOrderByCreationDateDesc(User citizen);
    List<IssueReport> findAllByOrderByCreationDateDesc();
}
