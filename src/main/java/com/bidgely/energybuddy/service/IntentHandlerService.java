package com.bidgely.energybuddy.service;

import com.bidgely.energybuddy.dto.request.AlexaRequest;
import com.bidgely.energybuddy.dto.request.Slot;
import com.bidgely.energybuddy.dto.response.AlexaResponse;
import com.bidgely.energybuddy.dto.response.Card;
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
            default -> buildResponse(
                    "Sorry, I didn't understand that request.", true,
                    new Card("Simple", "\u26a1 Energy Buddy",
                            "Try asking:\n\u2022 what is my energy usage today\n\u2022 how much did I spend this month\n\u2022 give me an energy saving tip\n\u2022 what is using the most power"));
        };
    }

    private AlexaResponse handleLaunchRequest() {
        String speech = "Welcome to Energy Buddy! You can ask me about your energy usage, costs, top appliances, or get energy saving tips. What would you like to know?";
        Card card = new Card("Simple", "\u26a1 Welcome to Energy Buddy",
                "Ask me about:\n\u2022 Energy usage today or this month\n\u2022 Your monthly electricity bill\n\u2022 Energy saving tips\n\u2022 Which device uses the most power");
        return buildResponse(speech, false, card);
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
            default -> buildResponse(
                    "Sorry, I don't know how to handle that request. Try asking about your energy usage, cost, or for a saving tip.", true,
                    new Card("Simple", "\u26a1 Energy Buddy",
                            "Try asking:\n\u2022 what is my energy usage today\n\u2022 how much did I spend this month\n\u2022 give me an energy saving tip\n\u2022 what is using the most power"));
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
        String speech = dataService.getEnergyUsage(timePeriod, userId);

        String effectivePeriod = timePeriod != null ? timePeriod : "today";
        String yesterdaySpeech = dataService.getEnergyUsage("yesterday", userId);
        String todaySpeech = dataService.getEnergyUsage("today", userId);
        String cost = dataService.getEnergyCost(effectivePeriod, userId);
        String topAppliance = dataService.getTopAppliance(userId);

        // Extract numeric values from speech strings for card content
        String usageValue = extractNumber(speech);
        String yesterdayValue = extractNumber(yesterdaySpeech);
        String todayValue = extractNumber(todaySpeech);
        String costValue = extractCost(cost);
        String deviceInfo = extractDeviceInfo(topAppliance);

        StringBuilder content = new StringBuilder();
        content.append("Usage:         ").append(usageValue).append(" kWh\n");
        content.append("Cost:          ").append(costValue).append("\n");
        content.append("Top Device:    ").append(deviceInfo).append("\n");

        // Compute vs yesterday comparison
        try {
            double todayNum = Double.parseDouble(todayValue);
            double yesterdayNum = Double.parseDouble(yesterdayValue);
            if (yesterdayNum > 0) {
                double pctChange = ((todayNum - yesterdayNum) / yesterdayNum) * 100;
                String arrow = pctChange >= 0 ? "\u2191" : "\u2193";
                content.append("Vs Yesterday:  ").append(arrow).append(" ")
                        .append(String.format("%.0f", Math.abs(pctChange))).append("% ")
                        .append(pctChange >= 0 ? "higher" : "lower");
            }
        } catch (NumberFormatException ignored) {
        }

        Card card = new Card("Simple", "\u26a1 Energy Usage", content.toString());
        return buildResponse(speech, true, card);
    }

    private AlexaResponse handleEnergyCost(AlexaRequest alexaRequest, String userId) {
        String timePeriod = extractSlotValue(alexaRequest, "timePeriod");
        String speech = dataService.getEnergyCost(timePeriod, userId);

        String thisMonthCost = extractCost(dataService.getEnergyCost("this month", userId));
        String lastMonthCost = extractCost(dataService.getEnergyCost("last month", userId));

        StringBuilder content = new StringBuilder();
        content.append("This Month:    ").append(thisMonthCost).append("\n");
        content.append("Last Month:    ").append(lastMonthCost).append("\n");

        // Compute savings
        try {
            double thisNum = Double.parseDouble(thisMonthCost.replaceAll("[^0-9.]", ""));
            double lastNum = Double.parseDouble(lastMonthCost.replaceAll("[^0-9.]", ""));
            double diff = lastNum - thisNum;
            if (diff > 0) {
                content.append("Savings:       \u2193 \u20b9").append(String.format("%.0f", diff)).append(" saved");
            } else if (diff < 0) {
                content.append("Change:        \u2191 \u20b9").append(String.format("%.0f", Math.abs(diff))).append(" more");
            }
        } catch (NumberFormatException ignored) {
        }

        Card card = new Card("Simple", "\uD83D\uDCB0 Energy Cost", content.toString());
        return buildResponse(speech, true, card);
    }

    private AlexaResponse handleEnergySavingTip(String userId) {
        String speech = dataService.getEnergySavingTip(userId);
        Card card = new Card("Simple", "\uD83D\uDCA1 Energy Saving Tip", speech);
        return buildResponse(speech, true, card);
    }

    private AlexaResponse handleTopAppliance(String userId) {
        String speech = dataService.getTopAppliance(userId);

        String deviceName = extractDeviceName(speech);
        String runtime = extractRuntime(speech);

        StringBuilder content = new StringBuilder();
        content.append("Device:    ").append(deviceName).append("\n");
        content.append("Runtime:   ").append(runtime).append("\n");
        content.append("Impact:    ~40% of daily usage");

        Card card = new Card("Simple", "\uD83D\uDD0C Top Energy Device", content.toString());
        return buildResponse(speech, true, card);
    }

    private String extractSlotValue(AlexaRequest alexaRequest, String slotName) {
        Map<String, Slot> slots = alexaRequest.getRequest().getIntent().getSlots();
        if (slots != null && slots.containsKey(slotName)) {
            return slots.get(slotName).getValue();
        }
        return null;
    }

    private AlexaResponse buildResponse(String speechText, boolean shouldEndSession, Card card) {
        AlexaResponse alexaResponse = new AlexaResponse(speechText, shouldEndSession);
        alexaResponse.getResponse().setCard(card);
        return alexaResponse;
    }

    // --- Helper methods to extract data from speech strings ---

    private String extractNumber(String speech) {
        if (speech == null) return "0";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+\\.?\\d*)\\s*kilowatt").matcher(speech);
        return m.find() ? m.group(1) : "0";
    }

    private String extractCost(String speech) {
        if (speech == null) return "\u20b90";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\u20b9[\\d,]+)").matcher(speech);
        return m.find() ? m.group(1) : "\u20b90";
    }

    private String extractDeviceInfo(String speech) {
        if (speech == null) return "Unknown";
        // "...is the AC unit, which has been running for 9 hours."
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("is the (.+?), which.*?(\\d+ hours)").matcher(speech);
        return m.find() ? m.group(1) + " (" + m.group(2) + ")" : "Unknown";
    }

    private String extractDeviceName(String speech) {
        if (speech == null) return "Unknown";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("is the (.+?),").matcher(speech);
        return m.find() ? capitalize(m.group(1)) : "Unknown";
    }

    private String extractRuntime(String speech) {
        if (speech == null) return "Unknown";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+ hours) today").matcher(speech);
        return m.find() ? m.group(1) + " today" : "Unknown";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        StringBuilder result = new StringBuilder();
        for (String word : s.split(" ")) {
            if (!result.isEmpty()) result.append(" ");
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }
}
