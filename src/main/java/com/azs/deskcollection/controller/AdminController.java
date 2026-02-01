package com.azs.deskcollection.controller;

import com.azs.deskcollection.model.User;
import com.azs.deskcollection.repository.InteractionRepository;
import com.azs.deskcollection.repository.UserRepository;
import com.azs.deskcollection.service.DebtorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    private final InteractionRepository interactionRepository;
    private final UserRepository userRepository;
    private final DebtorService debtorService;

    @Autowired
    public AdminController(InteractionRepository interactionRepository, UserRepository userRepository,
            DebtorService debtorService) {
        this.interactionRepository = interactionRepository;
        this.userRepository = userRepository;
        this.debtorService = debtorService;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        try {
            // Fetch raw stats: [username, count]
            List<Object[]> stats = interactionRepository
                    .countInteractionsByType(com.azs.deskcollection.model.Interaction.Type.WHATSAPP);

            // Convert to Map for easy lookup
            Map<String, Long> usageMap = stats.stream()
                    .collect(Collectors.toMap(
                            row -> (String) row[0],
                            row -> (Long) row[1]));

            // Get all users to show even those with 0 messages
            List<User> users = userRepository.findAll();

            // Create a wrapper/DTO list to pass to view
            List<Map<String, Object>> report = users.stream()
                    .filter(u -> u.getRole() == com.azs.deskcollection.model.Role.USER) // Filter only Agents
                    .map(u -> Map.<String, Object>of(
                            "username", u.getUsername(),
                            "role", u.getRole().name(),
                            "whatsappCount", usageMap.getOrDefault(u.getUsername(), 0L)))
                    .collect(Collectors.toList());

            model.addAttribute("report", report);
        } catch (Exception e) {
            e.printStackTrace(); // Log error
            model.addAttribute("error", "Failed to load dashboard data: " + e.getMessage());
            model.addAttribute("report", List.of());
        }
        return "admin/dashboard";
    }

    @GetMapping("/admin/users")
    public String usersPage() {
        return "admin/users";
    }

    @GetMapping("/admin/debtors")
    public String debtorsPage(Model model) {
        model.addAttribute("debtors", debtorService.getAllDebtors());
        return "admin/debtors";
    }
}
