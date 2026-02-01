package com.azs.deskcollection.controller;

import com.azs.deskcollection.service.DebtorService;
import com.azs.deskcollection.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/debtors")
public class DebtorViewController {

    private final DebtorService debtorService;
    private final UserRepository userRepository;

    @Autowired
    public DebtorViewController(DebtorService debtorService, UserRepository userRepository) {
        this.debtorService = debtorService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String debtorsPage(Model model, Principal principal) {
        var user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Use findDebtorsByUser which already exists in Service (via
        // DebtCollectionService or DebtorService?)
        // DebtorService currently only has getAllDebtors and import.
        // We really should use DebtCollectionService for the "findDebtorsByUser" logic
        // or move it.
        // Let's check DebtCollectionService again. It has findDebtorsByUser.
        // But the import logic is in DebtorService.
        // Ideally we should consolidate or just use DebtCollectionService here if it
        // has what we need.
        // Wait, DebtorService was created specifically for import.
        // Let's check if we should add findDebtorsByUser to DebtorService or just
        // inject DebtCollectionService.
        // For cleaner refactoring later, maybe move findDebtorsByUser to DebtorService?
        // For now, to avoid breaking changes, let's inject DebtCollectionService.

        // Actually, let's look at DebtorService again. It has debtorRepository. We can
        // just add findDebtorsByUser there.
        // It's cleaner than injecting another service.

        model.addAttribute("debtors", debtorService.getDebtorsByUser(user));
        return "debtors"; // We'll create a shared view or a user-specific one
    }
}
