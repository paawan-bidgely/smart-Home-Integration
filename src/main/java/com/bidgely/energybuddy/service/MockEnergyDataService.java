package com.bidgely.energybuddy.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class MockEnergyDataService {

    private static final Map<String, Double> USAGE_DATA = Map.of(
            "today", 12.4,
            "yesterday", 13.5,
            "this week", 87.2,
            "last month", 342.8,
            "this month", 156.3
    );

    private static final Map<String, String> COST_DATA = Map.of(
            "today", "₹180",
            "this month", "₹2340",
            "last month", "₹3100"
    );

    private static final List<String> ENERGY_TIPS = List.of(
            "Your AC ran 6 hours today. Setting it 2 degrees higher saves around 15 percent on your bill.",
            "Try running your washing machine at night. Off peak hours can reduce energy costs significantly.",
            "Your fridge is your highest always on appliance. Make sure the seal is tight to avoid energy loss."
    );

    private final Random random = new Random();

    public String getEnergyUsage(String timePeriod) {
        if (timePeriod == null) {
            timePeriod = "today";
        }
        Double usage = USAGE_DATA.get(timePeriod.toLowerCase());
        if (usage != null) {
            return "Your energy usage " + timePeriod + " is " + usage + " kilowatt hours.";
        }
        return "Sorry, I don't have usage data for " + timePeriod + ". Try asking for today, yesterday, this week, this month, or last month.";
    }

    public String getEnergyCost(String timePeriod) {
        if (timePeriod == null) {
            timePeriod = "today";
        }
        String cost = COST_DATA.get(timePeriod.toLowerCase());
        if (cost != null) {
            return "Your energy cost for " + timePeriod + " is " + cost + ".";
        }
        return "Sorry, I don't have cost data for " + timePeriod + ". Try asking for today, this month, or last month.";
    }

    public String getEnergySavingTip() {
        return ENERGY_TIPS.get(random.nextInt(ENERGY_TIPS.size()));
    }

    public String getTopAppliance() {
        return "Your top energy consuming appliance today is the AC unit, which has been running for 6 hours.";
    }
}
