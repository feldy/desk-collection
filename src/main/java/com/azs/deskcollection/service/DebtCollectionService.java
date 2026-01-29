package com.azs.deskcollection.service;

import com.azs.deskcollection.model.Debtor;
import com.azs.deskcollection.model.Interaction;
import com.azs.deskcollection.model.Loan;
import com.azs.deskcollection.repository.DebtorRepository;
import com.azs.deskcollection.repository.InteractionRepository;
import com.azs.deskcollection.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DebtCollectionService {

    private final DebtorRepository debtorRepository;
    private final LoanRepository loanRepository;
    private final InteractionRepository interactionRepository;

    @Autowired
    public DebtCollectionService(DebtorRepository debtorRepository,
            LoanRepository loanRepository,
            InteractionRepository interactionRepository) {
        this.debtorRepository = debtorRepository;
        this.loanRepository = loanRepository;
        this.interactionRepository = interactionRepository;
    }

    // Debtor Operations
    public List<Debtor> findAllDebtors() {
        return debtorRepository.findAll();
    }

    public Optional<Debtor> findDebtorById(Long id) {
        return debtorRepository.findById(id);
    }

    public Debtor saveDebtor(Debtor debtor) {
        return debtorRepository.save(debtor);
    }

    public void deleteDebtor(Long id) {
        debtorRepository.deleteById(id);
    }

    // Loan Operations
    public Loan addLoan(Long debtorId, Loan loan) {
        Debtor debtor = debtorRepository.findById(debtorId)
                .orElseThrow(() -> new RuntimeException("Debtor not found"));
        loan.setDebtor(debtor);
        return loanRepository.save(loan);
    }

    public List<Loan> findLoansByDebtor(Long debtorId) {
        return loanRepository.findByDebtorId(debtorId);
    }

    // Interaction Operations
    public Interaction logInteraction(Long debtorId, Interaction interaction) {
        Debtor debtor = debtorRepository.findById(debtorId)
                .orElseThrow(() -> new RuntimeException("Debtor not found"));
        interaction.setDebtor(debtor);
        return interactionRepository.save(interaction);
    }

    public List<Interaction> findInteractionsByDebtor(Long debtorId) {
        return interactionRepository.findByDebtorId(debtorId);
    }
}
