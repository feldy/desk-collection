package com.azs.deskcollection.controller;

import com.azs.deskcollection.model.Device;
import com.azs.deskcollection.model.User;
import com.azs.deskcollection.repository.UserRepository;
import com.azs.deskcollection.service.DeviceService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceService deviceService;
    private final UserRepository userRepository;

    public DeviceController(DeviceService deviceService, UserRepository userRepository) {
        this.deviceService = deviceService;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username).orElse(null);
        }
        return null;
    }

    @GetMapping
    public String listDevices(Model model) {
        User user = getCurrentUser();
        Optional<Device> deviceOpt = deviceService.getDevice(user);

        if (deviceOpt.isPresent()) {
            // Update status periodically/on-refresh
            Device device = deviceOpt.get();
            deviceService.updateStatus(device);
            model.addAttribute("device", device);
        } else {
            model.addAttribute("device", null);
        }

        return "devices";
    }

    @PostMapping("/add")
    public String addDevice(@RequestParam String name,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User user = getCurrentUser();
        try {
            deviceService.createDevice(user, name);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add device: " + e.getMessage());
        }
        return "redirect:/devices";
    }

    @PostMapping("/delete")
    public String deleteDevice() {
        User user = getCurrentUser();
        deviceService.deleteDevice(user);
        return "redirect:/devices";
    }

    @GetMapping(value = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] getQRCode() {
        User user = getCurrentUser();
        return deviceService.getQRCode(user);
    }
}
