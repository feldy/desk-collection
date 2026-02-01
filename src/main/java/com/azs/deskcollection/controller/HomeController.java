package com.azs.deskcollection.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;

@Controller
public class HomeController {

    @org.springframework.beans.factory.annotation.Autowired
    private com.azs.deskcollection.repository.UserRepository userRepository;

    @GetMapping("/")
    public String home(Principal principal) {
        if (principal != null) {
            return redirectBasedOnRole(principal);
        }
        return "home";
    }

    @GetMapping("/login")
    public String login(Principal principal) {
        if (principal != null) {
            return redirectBasedOnRole(principal);
        }
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal) {
        // Prevent Superadmin from accessing agent dashboard
        if (isSuperAdmin(principal)) {
            return "redirect:/admin/dashboard";
        }
        return "dashboard";
    }

    // admin/users mapping moved to AdminController or handled by routing

    // Helper methods
    private String redirectBasedOnRole(Principal principal) {
        if (isSuperAdmin(principal)) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/dashboard";
    }

    private boolean isSuperAdmin(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .map(u -> u.getRole() == com.azs.deskcollection.model.Role.SUPERADMIN)
                .orElse(false);
    }
}
