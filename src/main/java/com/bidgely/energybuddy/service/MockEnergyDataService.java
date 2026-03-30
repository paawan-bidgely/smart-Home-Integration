package com.bidgely.energybuddy.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MockEnergyDataService {

    private static final String HEAVY_USER = "c63113dc-4947-4db6-8346-59b6c7983c09";
    private static final String LIGHT_USER = "d74224ed-5058-5ec7-9457-60c7d8094d10";

    // Usage data per user per time period
    private static final Map<String, Map<String, Double>> USAGE_DATA = Map.of(
            HEAVY_USER, Map.of(
                    "today", 18.6, "yesterday", 17.2, "this week", 124.3,
                    "this month", 423.5, "last month", 489.2),
            LIGHT_USER, Map.of(
                    "today", 7.2, "yesterday", 6.8, "this week", 48.1,
                    "this month", 187.3, "last month", 201.4),
            "demo", Map.of(
                    "today", 12.4, "yesterday", 13.5, "this week", 87.2,
                    "this month", 156.3, "last month", 342.8)
    );

    private static final Map<String, String> COST_DATA = Map.of(
            HEAVY_USER, "₹3200",
            LIGHT_USER, "₹1450",
            "demo", "₹2340"
    );

    private static final Map<String, String> TOP_APPLIANCE_DATA = Map.of(
            HEAVY_USER, "Your top energy consuming appliance today is the AC unit, which has been running for 9 hours.",
            LIGHT_USER, "Your top energy consuming appliance today is the geyser, which has been running for 2 hours.",
            "demo", "Your top energy consuming appliance today is the AC unit, which has been running for 6 hours."
    );

    private static final Map<String, String> TIP_DATA = Map.of(
            HEAVY_USER, "Your AC ran 9 hours today. Setting it 2 degrees higher saves about 15% on your bill.",
            LIGHT_USER, "Your geyser uses a lot of energy. Try setting a timer to run it only when needed.",
            "demo", "Try running your washing machine at night. Off peak hours reduce energy costs significantly."
    );

    public String getEnergyUsage(String timePeriod, String userId) {
        if (timePeriod == null) {
            timePeriod = "today";
        }
        Map<String, Double> userData = USAGE_DATA.getOrDefault(userId, USAGE_DATA.get("demo"));
        Double usage = userData.get(timePeriod.toLowerCase());
        if (usage != null) {
            return "Your energy usage " + timePeriod + " is " + usage + " kilowatt hours.";
        }
        return "Sorry, I don't have usage data for " + timePeriod + ". Try asking for today, yesterday, this week, this month, or last month.";
    }

    public String getEnergyCost(String timePeriod, String userId) {
        String cost = COST_DATA.getOrDefault(userId, COST_DATA.get("demo"));
        return "Your energy cost for this month is " + cost + ".";
    }

    public String getEnergySavingTip(String userId) {
        return TIP_DATA.getOrDefault(userId, TIP_DATA.get("demo"));
    }

    public String getTopAppliance(String userId) {
        return TOP_APPLIANCE_DATA.getOrDefault(userId, TOP_APPLIANCE_DATA.get("demo"));
    }
}
