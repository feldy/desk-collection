package com.azs.deskcollection.controller;

import com.azs.deskcollection.model.Debtor;
import com.azs.deskcollection.model.Interaction;
import com.azs.deskcollection.model.Loan;
import com.azs.deskcollection.service.DebtCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/debtors")
public class DebtorController {

    private final DebtCollectionService service;

    @Autowired
    public DebtorController(DebtCollectionService service) {
        this.service = service;
    }

    @GetMapping
    public List<Debtor> getAllDebtors() {
        return service.findAllDebtors();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Debtor> getDebtor(@PathVariable Long id) {
        return service.findDebtorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Debtor createDebtor(@RequestBody Debtor debtor) {
        return service.saveDebtor(debtor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDebtor(@PathVariable Long id) {
        service.deleteDebtor(id);
        return ResponseEntity.noContent().build();
    }

    // Sub-resources: Loans
    @PostMapping("/{id}/loans")
    public Loan addLoan(@PathVariable Long id, @RequestBody Loan loan) {
        return service.addLoan(id, loan);
    }

    @GetMapping("/{id}/loans")
    public List<Loan> getLoans(@PathVariable Long id) {
        return service.findLoansByDebtor(id);
    }

    // Sub-resources: Interactions
    @PostMapping("/{id}/interactions")
    public Interaction addInteraction(@PathVariable Long id, @RequestBody Interaction interaction) {
        return service.logInteraction(id, interaction);
    }

    @GetMapping("/{id}/interactions")
    public List<Interaction> getInteractions(@PathVariable Long id) {
        return service.findInteractionsByDebtor(id);
    }
}
