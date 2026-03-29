package com.bidgely.energybuddy.controller;

import com.bidgely.energybuddy.dto.request.AlexaRequest;
import com.bidgely.energybuddy.dto.response.AlexaResponse;
import com.bidgely.energybuddy.service.IntentHandlerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AlexaWebhookController {

    private static final Logger log = LoggerFactory.getLogger(AlexaWebhookController.class);

    private final IntentHandlerService intentHandlerService;
    private final ObjectMapper objectMapper;

    public AlexaWebhookController(IntentHandlerService intentHandlerService, ObjectMapper objectMapper) {
        this.intentHandlerService = intentHandlerService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/alexa/webhook")
    public ResponseEntity<AlexaResponse> handleAlexaRequest(@RequestBody AlexaRequest alexaRequest) {
        try {
            log.info("Incoming Alexa request: {}", objectMapper.writeValueAsString(alexaRequest));
        } catch (Exception e) {
            log.warn("Could not serialize request for logging", e);
        }

        AlexaResponse response = intentHandlerService.handleRequest(alexaRequest);

        try {
            log.info("Outgoing Alexa response: {}", objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            log.warn("Could not serialize response for logging", e);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }
}
