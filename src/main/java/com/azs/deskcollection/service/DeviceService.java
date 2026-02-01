package com.azs.deskcollection.service;

import com.azs.deskcollection.model.Device;
import com.azs.deskcollection.model.DeviceStatus;
import com.azs.deskcollection.model.User;
import com.azs.deskcollection.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final WhatsappService whatsappService;

    public DeviceService(DeviceRepository deviceRepository, WhatsappService whatsappService) {
        this.deviceRepository = deviceRepository;
        this.whatsappService = whatsappService;
    }

    public Optional<Device> getDevice(User user) {
        return deviceRepository.findByUser(user);
    }

    @Transactional
    public Device createDevice(User user, String name) {
        if (deviceRepository.findByUser(user).isPresent()) {
            throw new IllegalStateException("Max 1 device allowed per user");
        }

        // WAHA Core only supports 'default' session
        String sessionName = "default";

        // Check if ANY device is already using 'default' session (single tenant
        // limitation of WAHA Core)
        if (deviceRepository.findBySessionName(sessionName).isPresent()) {
            throw new IllegalStateException(
                    "System limit: Only 1 WhatsApp device allowed in WAHA Core (default session is busy).");
        }

        Device device = new Device();
        device.setName(name);
        device.setSessionName(sessionName);
        device.setUser(user);
        device.setStatus(DeviceStatus.STARTING);

        deviceRepository.save(device); // Save first

        // Call WAHA to start session
        whatsappService.startSession(sessionName);

        return device;
    }

    @Transactional
    public void deleteDevice(User user) {
        Device device = deviceRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Device not found"));

        whatsappService.stopSession(device.getSessionName());
        deviceRepository.delete(device);
    }

    public byte[] getQRCode(User user) {
        Device device = deviceRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Device not found"));

        return whatsappService.getScreenshot(device.getSessionName());
    }

    public void updateStatus(Device device) {
        String statusStr = whatsappService.getSessionStatus(device.getSessionName());

        // Auto-heal: If session is stopped or broken, try to start/repair it
        if ("STOPPED".equals(statusStr) || "FAILED".equals(statusStr)) {
            System.out.println("Auto-healing session for device: " + device.getName());
            try {
                whatsappService.startSession(device.getSessionName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                } // Wait for status change
                // Re-fetch status after repair attempt
                statusStr = whatsappService.getSessionStatus(device.getSessionName());
            } catch (Exception e) {
                System.err.println("Auto-heal failed: " + e.getMessage());
            }
        }

        // Map string to enum (simplified)
        DeviceStatus status = mapStatus(statusStr);
        if (device.getStatus() != status) {
            device.setStatus(status);
            deviceRepository.save(device);
        }
    }

    private DeviceStatus mapStatus(String waStatus) {
        if (waStatus == null)
            return DeviceStatus.STOPPED;
        switch (waStatus.toUpperCase()) {
            case "WORKING":
                return DeviceStatus.WORKING;
            case "SCAN_QR_CODE":
                return DeviceStatus.SCAN_QR;
            case "STARTING":
                return DeviceStatus.STARTING;
            case "FAILED":
                return DeviceStatus.FAILED;
            default:
                return DeviceStatus.STOPPED;
        }
    }
}
