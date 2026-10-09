package com.baladiati.smart.service;

import com.baladiati.smart.entity.IssueReport;
import com.baladiati.smart.entity.ReportStatus;
import com.baladiati.smart.entity.User;
import com.baladiati.smart.repository.ReportRepository;
import com.baladiati.smart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    public IssueReport createReport(IssueReport report, String citizenEmail) {
        User citizen = userRepository.findByEmail(citizenEmail)
                .orElseThrow(() -> new IllegalArgumentException("Citizen not found"));
        
        report.setCitizen(citizen);
        report.setStatus(ReportStatus.RECEIVED);
        report.setCreationDate(LocalDateTime.now());
        
        return reportRepository.save(report);
    }

    public List<IssueReport> getCitizenReports(String citizenEmail) {
        User citizen = userRepository.findByEmail(citizenEmail)
                .orElseThrow(() -> new IllegalArgumentException("Citizen not found"));
        return reportRepository.findByCitizenOrderByCreationDateDesc(citizen);
    }

    public List<IssueReport> getAllReports() {
        return reportRepository.findAllByOrderByCreationDateDesc();
    }

    public IssueReport updateReportStatus(Long reportId, ReportStatus status) {
        IssueReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        
        report.setStatus(status);
        return reportRepository.save(report);
    }
}
