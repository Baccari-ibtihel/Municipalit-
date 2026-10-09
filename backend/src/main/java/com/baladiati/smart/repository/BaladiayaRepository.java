package com.baladiati.smart.repository;

import com.baladiati.smart.entity.Baladiya;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BaladiayaRepository extends JpaRepository<Baladiya, Long> {
}
