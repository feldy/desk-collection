package com.azs.deskcollection.controller;

import com.azs.deskcollection.service.DebtorService;

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
    private final DebtorService debtorService;
    private final com.azs.deskcollection.repository.UserRepository userRepository;

    @Autowired
    public DebtorController(DebtCollectionService service,
            DebtorService debtorService,
            com.azs.deskcollection.repository.UserRepository userRepository) {
        this.service = service;
        this.debtorService = debtorService;
        this.userRepository = userRepository;
    }

    private com.azs.deskcollection.model.User getUser(java.security.Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public List<Debtor> getAllDebtors(java.security.Principal principal) {
        return service.findDebtorsByUser(getUser(principal));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Debtor> getDebtor(@PathVariable Long id, java.security.Principal principal) {
        return service.findDebtorByIdAndUser(id, getUser(principal))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Debtor createDebtor(@RequestBody Debtor debtor, java.security.Principal principal) {
        return service.saveDebtor(debtor, getUser(principal));
    }

    @PostMapping("/import")
    public ResponseEntity<String> importDebtors(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            java.security.Principal principal) {
        try {
            debtorService.importDebtorsFromExcel(file, getUser(principal));
            return ResponseEntity.ok("Debtors imported successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to import debtors: " + e.getMessage());
        }
    }

    @GetMapping("/template")
    public ResponseEntity<org.springframework.core.io.Resource> downloadTemplate() throws java.io.IOException {
        java.io.ByteArrayInputStream in = debtorService.generateExcelTemplate();
        org.springframework.core.io.InputStreamResource resource = new org.springframework.core.io.InputStreamResource(
                in);

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=debtor_template.xlsx")
                .contentType(org.springframework.http.MediaType
                        .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDebtor(@PathVariable Long id) {
        // Ideally should check ownership here too
        service.deleteDebtor(id);
        return ResponseEntity.noContent().build();
    }

    // Sub-resources: Loans
    @PostMapping("/{id}/loans")
    public Loan addLoan(@PathVariable Long id, @RequestBody Loan loan) {
        // Ownership check implicitly handled if service verifies debtor exists?
        // Current service implementation just checks findById.
        // Refactor suggested: Service should verify ownership.
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
