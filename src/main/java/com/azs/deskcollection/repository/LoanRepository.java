package com.azs.deskcollection.repository;

import com.azs.deskcollection.model.Loan;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByDebtorId(Long debtorId);
}
