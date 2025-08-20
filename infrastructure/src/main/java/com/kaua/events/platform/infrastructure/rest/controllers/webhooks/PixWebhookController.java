package com.kaua.events.platform.infrastructure.rest.controllers.webhooks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhooks/pix")
public class PixWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PixWebhookController.class);

    @PostMapping
    public ResponseEntity<Void> receivePixWebhook(
            @RequestHeader Map<String, String> headers,
            @RequestBody Map<String, Object> payload
    ) {
        log.info("[PIX] Webhook recebido. Headers={} Payload={}", headers, payload);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
