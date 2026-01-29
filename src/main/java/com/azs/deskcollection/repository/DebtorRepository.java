package com.azs.deskcollection.repository;

import com.azs.deskcollection.model.Debtor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DebtorRepository extends JpaRepository<Debtor, Long> {
}
