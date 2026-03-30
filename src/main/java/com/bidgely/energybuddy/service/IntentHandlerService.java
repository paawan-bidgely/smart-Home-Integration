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
    private final TokenStore tokenStore;

    public IntentHandlerService(MockEnergyDataService dataService, TokenStore tokenStore) {
        this.dataService = dataService;
        this.tokenStore = tokenStore;
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
        String accessToken = extractAccessToken(alexaRequest);
        String userId = resolveUserId(accessToken);
        log.info("Handling intent: {} | accessToken present={} uuid={}", intentName, accessToken != null, userId);

        return switch (intentName) {
            case "GetEnergyUsageIntent" -> handleEnergyUsage(alexaRequest, userId);
            case "GetEnergyCostIntent" -> handleEnergyCost(alexaRequest, userId);
            case "GetEnergySavingTipIntent" -> handleEnergySavingTip(userId);
            case "GetTopApplianceIntent" -> handleTopAppliance(userId);
            default -> buildResponse("Sorry, I don't know how to handle that request. Try asking about your energy usage, cost, or for a saving tip.", true);
        };
    }

    private String extractAccessToken(AlexaRequest alexaRequest) {
        if (alexaRequest.getSession() != null
                && alexaRequest.getSession().getUser() != null) {
            return alexaRequest.getSession().getUser().getAccessToken();
        }
        return null;
    }

    private String resolveUserId(String accessToken) {
        if (accessToken == null) return "demo";
        String uuid = tokenStore.getUUIDByAccessToken(accessToken);
        return uuid != null ? uuid : "demo";
    }

    private AlexaResponse handleEnergyUsage(AlexaRequest alexaRequest, String userId) {
        String timePeriod = extractSlotValue(alexaRequest, "timePeriod");
        return buildResponse(dataService.getEnergyUsage(timePeriod, userId), true);
    }

    private AlexaResponse handleEnergyCost(AlexaRequest alexaRequest, String userId) {
        String timePeriod = extractSlotValue(alexaRequest, "timePeriod");
        return buildResponse(dataService.getEnergyCost(timePeriod, userId), true);
    }

    private AlexaResponse handleEnergySavingTip(String userId) {
        return buildResponse(dataService.getEnergySavingTip(userId), true);
    }

    private AlexaResponse handleTopAppliance(String userId) {
        return buildResponse(dataService.getTopAppliance(userId), true);
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
