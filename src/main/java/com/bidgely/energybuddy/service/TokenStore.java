package com.bidgely.energybuddy.service;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TokenStore {

    private final Map<String, String> accountToBidgelyUUID = new HashMap<>() {{
        put("BESCOM:12345", "c63113dc-4947-4db6-8346-59b6c7983c09");
        put("BESCOM:67890", "d74224ed-5058-5ec7-9457-60c7d8094d10");
    }};

    private final Map<String, String> authCodeStore = new HashMap<>();
    private final Map<String, String> accessTokenStore = new HashMap<>();

    public String getBidgelyUUID(String utility, String accountNumber) {
        return accountToBidgelyUUID.get(utility + ":" + accountNumber);
    }

    public void storeAuthCode(String authCode, String bidgelyUUID) {
        authCodeStore.put(authCode, bidgelyUUID);
    }

    public String getUUIDByAuthCode(String authCode) {
        return authCodeStore.remove(authCode);
    }

    public void storeAccessToken(String accessToken, String bidgelyUUID) {
        accessTokenStore.put(accessToken, bidgelyUUID);
    }

    public String getUUIDByAccessToken(String accessToken) {
        return accessTokenStore.get(accessToken);
    }
}
