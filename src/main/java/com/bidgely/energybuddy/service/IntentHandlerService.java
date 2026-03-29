package com.bidgely.energybuddy.service;

import com.bidgely.energybuddy.dto.request.AlexaRequest;
import com.bidgely.energybuddy.dto.request.Slot;
import com.bidgely.energybuddy.dto.response.AlexaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class IntentHandlerService {

    private static final Logger log = LoggerFactory.getLogger(IntentHandlerService.class);

    private final MockEnergyDataService dataService;

    public IntentHandlerService(MockEnergyDataService dataService) {
        this.dataService = dataService;
    }

    public AlexaResponse handleRequest(AlexaRequest alexaRequest) {
        String requestType = alexaRequest.getRequest().getType();
        log.info("Handling request type: {}", requestType);

        return switch (requestType) {
            case "LaunchRequest" -> handleLaunchRequest();
            case "SessionEndedRequest" -> handleSessionEnded();
            case "IntentRequest" -> handleIntentRequest(alexaRequest);
            default -> buildResponse("Sorry, I didn't understand that request.", true);
        };
    }

    private AlexaResponse handleLaunchRequest() {
        return buildResponse(
                "Welcome to Energy Buddy! You can ask me about your energy usage, costs, top appliances, or get energy saving tips. What would you like to know?",
                false
        );
    }

    private AlexaResponse handleSessionEnded() {
        return new AlexaResponse();
    }

    private AlexaResponse handleIntentRequest(AlexaRequest alexaRequest) {
        String intentName = alexaRequest.getRequest().getIntent().getName();
        log.info("Handling intent: {}", intentName);

        return switch (intentName) {
            case "GetEnergyUsageIntent" -> handleEnergyUsage(alexaRequest);
            case "GetEnergyCostIntent" -> handleEnergyCost(alexaRequest);
            case "GetEnergySavingTipIntent" -> handleEnergySavingTip();
            case "GetTopApplianceIntent" -> handleTopAppliance();
            default -> buildResponse("Sorry, I don't know how to handle that request. Try asking about your energy usage, cost, or for a saving tip.", true);
        };
    }

    private AlexaResponse handleEnergyUsage(AlexaRequest alexaRequest) {
        String timePeriod = extractSlotValue(alexaRequest, "timePeriod");
        return buildResponse(dataService.getEnergyUsage(timePeriod), true);
    }

    private AlexaResponse handleEnergyCost(AlexaRequest alexaRequest) {
        String timePeriod = extractSlotValue(alexaRequest, "timePeriod");
        return buildResponse(dataService.getEnergyCost(timePeriod), true);
    }

    private AlexaResponse handleEnergySavingTip() {
        return buildResponse(dataService.getEnergySavingTip(), true);
    }

    private AlexaResponse handleTopAppliance() {
        return buildResponse(dataService.getTopAppliance(), true);
    }

    private String extractSlotValue(AlexaRequest alexaRequest, String slotName) {
        Map<String, Slot> slots = alexaRequest.getRequest().getIntent().getSlots();
        if (slots != null && slots.containsKey(slotName)) {
            return slots.get(slotName).getValue();
        }
        return null;
    }

    private AlexaResponse buildResponse(String speechText, boolean shouldEndSession) {
        return new AlexaResponse(speechText, shouldEndSession);
    }
}
