package com.azs.deskcollection.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsappService {

    private final RestTemplate restTemplate;
    // Using localhost because Spring Boot is running on host, not in docker network
    private final String WAHA_URL = "http://localhost:3000";
    private final String API_KEY = "secret123";

    public WhatsappService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Api-Key", API_KEY);
        return headers;
    }

    public void sendText(String phone, String message) {
        sendText("default", phone, message);
    }

    public void sendText(String session, String phone, String message) {
        try {
            String chatId = formatPhone(phone);

            // 1. Simulate Typing (Human Presence)
            try {
                String typingUrl = WAHA_URL + "/api/startTyping";
                Map<String, Object> typingBody = new HashMap<>();
                typingBody.put("chatId", chatId);
                typingBody.put("session", session);
                HttpEntity<Map<String, Object>> typingRequest = new HttpEntity<>(typingBody, getHeaders());
                restTemplate.postForObject(typingUrl, typingRequest, String.class);
            } catch (Exception e) {
                // Ignore if typing API not supported or fails silently
                System.out.println("Typing simulation skipped: " + e.getMessage());
            }

            // 2. Random Delay (3-6 seconds) to mimic thinking/typing time
            int delay = 3000 + (int) (Math.random() * 3000);
            System.out.println("Anti-spam: Simulating typing for " + delay + "ms...");
            Thread.sleep(delay);

            // 3. Send Message
            String url = WAHA_URL + "/api/sendText";
            Map<String, Object> body = new HashMap<>();
            body.put("chatId", chatId);
            body.put("text", message);
            body.put("session", session);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, getHeaders());

            restTemplate.postForObject(url, request, String.class);
            System.out.println("Sent WhatsApp message to " + phone + " via " + session);

            // 4. Stop Typing
            try {
                String stopTypingUrl = WAHA_URL + "/api/stopTyping";
                Map<String, Object> stopTypingBody = new HashMap<>();
                stopTypingBody.put("chatId", chatId);
                stopTypingBody.put("session", session);
                HttpEntity<Map<String, Object>> stopTypingRequest = new HttpEntity<>(stopTypingBody, getHeaders());
                restTemplate.postForObject(stopTypingUrl, stopTypingRequest, String.class);
            } catch (Exception e) {
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to send WhatsApp message: " + e.getMessage());
        }
    }

    // --- SESSION MANAGEMENT ---

    public void startSession(String sessionName) {
        try {
            String url = WAHA_URL + "/api/sessions";
            Map<String, Object> body = new HashMap<>();
            body.put("name", sessionName);
            // Default config: omit proxy instead of explicit null which might cause issues
            body.put("config", new HashMap<>());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, getHeaders());

            restTemplate.postForObject(url, request, String.class);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            boolean repaired = false;
            if (e.getStatusCode().value() == 422) {
                // Check if it's "STARTING", "SCAN_QR_CODE" or "WORKING"
                // If it's "STOPPED" or "FAILED", or if getStatus returns "STOPPED" (meaning
                // 404/error), we repair it.
                String currentStatus = getSessionStatus(sessionName);
                if ("STOPPED".equals(currentStatus) || "FAILED".equals(currentStatus)) {
                    System.out.println(
                            "Session '" + sessionName + "' exists but is broken (" + currentStatus + "). Repairing...");

                    // Try to START it first (best for existing sessions)
                    try {
                        String startUrl = WAHA_URL + "/api/sessions/" + sessionName + "/start";
                        HttpEntity<String> startRequest = new HttpEntity<>("", getHeaders()); // Empty body
                        restTemplate.postForObject(startUrl, startRequest, String.class);
                        repaired = true;
                        System.out.println("Session started successfully (wakeup).");
                    } catch (Exception startEx) {
                        System.out.println(
                                "Start failed (" + startEx.getMessage() + "), retrying with Delete & Recreate...");

                        stopSession(sessionName); // DELETE

                        try {
                            Thread.sleep(2000); // Wait for delete to propagate
                        } catch (InterruptedException ie) {
                        }

                        try {
                            // Re-create request variables properly
                            String repairUrl = WAHA_URL + "/api/sessions";
                            Map<String, Object> repairBody = new HashMap<>();
                            repairBody.put("name", sessionName);
                            repairBody.put("config", new HashMap<>());
                            HttpEntity<Map<String, Object>> repairRequest = new HttpEntity<>(repairBody, getHeaders());

                            restTemplate.postForObject(repairUrl, repairRequest, String.class); // RE-CREATE
                            repaired = true;
                            System.out.println("Session recreated successfully.");
                        } catch (Exception ex) {
                            System.err.println("Failed to recreate session: " + ex.getMessage());
                        }
                    }
                } else {
                    System.out.println(
                            "Session already exists and seems healthy (" + currentStatus + "), ignoring error.");
                    repaired = true;
                }
            }

            if (!repaired) {
                System.err.println("Failed to start session: " + e.getResponseBodyAsString());
                throw e;
            }
        } catch (Exception e) {
            System.err.println("Failed to start session: " + e.getMessage());
        }
    }

    public void stopSession(String sessionName) {
        try {
            String url = WAHA_URL + "/api/sessions/" + sessionName;

            // For DELETE requests with headers, we need to use exchange()
            HttpEntity<Void> request = new HttpEntity<>(getHeaders());
            restTemplate.exchange(url, org.springframework.http.HttpMethod.DELETE, request, Void.class);
        } catch (Exception e) {
            System.err.println("Failed to stop session: " + e.getMessage());
        }
    }

    public byte[] getScreenshot(String sessionName) {
        try {
            // GET /api/screenshot?session={name}
            String url = WAHA_URL + "/api/screenshot?session=" + sessionName;

            HttpEntity<Void> request = new HttpEntity<>(getHeaders());
            return restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, request, byte[].class).getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public String getSessionStatus(String sessionName) {
        try {
            // GET /api/sessions/{name}
            String url = WAHA_URL + "/api/sessions/" + sessionName;

            HttpEntity<Void> request = new HttpEntity<>(getHeaders());
            Map<String, Object> response = restTemplate
                    .exchange(url, org.springframework.http.HttpMethod.GET, request, Map.class).getBody();

            return (String) response.get("status");
        } catch (Exception e) {
            return "STOPPED";
        }
    }

    private String formatPhone(String phone) {
        if (phone == null)
            return "";
        String clean = phone.replaceAll("\\D", "");
        if (clean.startsWith("0"))
            clean = "62" + clean.substring(1);
        if (!clean.endsWith("@c.us"))
            clean = clean + "@c.us";
        return clean;
    }
}
