package com.azs.deskcollection.controller;

import com.azs.deskcollection.service.WhatsappService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsappController {

    private final WhatsappService whatsappService;

    public WhatsappController(WhatsappService whatsappService) {
        this.whatsappService = whatsappService;
    }

    @PostMapping("/send")
    public String sendMessage(@RequestBody Map<String, String> payload) {
        String phone = payload.get("phone");
        String message = payload.get("message");
        whatsappService.sendText(phone, message);
        return "Message queued";
    }
}
