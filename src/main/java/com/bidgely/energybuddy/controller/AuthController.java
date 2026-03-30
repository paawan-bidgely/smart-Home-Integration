package com.bidgely.energybuddy.controller;

import com.bidgely.energybuddy.service.TokenStore;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private static final String VALID_CLIENT_ID = "energy-buddy-client";
    private static final String VALID_CLIENT_SECRET = "energy-buddy-secret-hackathon";

    private final TokenStore tokenStore;

    public AuthController(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @GetMapping("/login")
    public String login(@RequestParam String state,
                        @RequestParam("redirect_uri") String redirectUri,
                        @RequestParam("client_id") String clientId,
                        @RequestParam("response_type") String responseType,
                        Model model) {
        log.info("/auth/login called with state={}", state);
        model.addAttribute("state", state);
        model.addAttribute("redirectUri", redirectUri);
        return "login";
    }

    @PostMapping("/submit")
    public String submit(@RequestParam String utility,
                         @RequestParam String accountNumber,
                         @RequestParam String state,
                         @RequestParam String redirectUri,
                         Model model) {
        log.info("/auth/submit: utility={} account={}", utility, accountNumber);

        String uuid = tokenStore.getBidgelyUUID(utility, accountNumber);
        log.info("/auth/submit: UUID found={}", uuid);

        if (uuid == null) {
            model.addAttribute("errorMessage", "Account not found. Please check your account number and try again.");
            return "error";
        }

        String authCode = UUID.randomUUID().toString();
        tokenStore.storeAuthCode(authCode, uuid);

        return "redirect:" + redirectUri + "?code=" + authCode + "&state=" + state;
    }

    @PostMapping(value = "/token", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> token(
            @RequestParam("grant_type") String grantType,
            @RequestParam("code") String authCode,
            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "client_secret", required = false) String clientSecret,
            HttpServletRequest request) {

        log.info("/auth/token: authCode received={}", authCode);

        // Extract credentials from Basic Auth header if present
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String base64 = authHeader.substring(6);
            String decoded = new String(Base64.getDecoder().decode(base64));
            String[] parts = decoded.split(":", 2);
            clientId = parts[0];
            clientSecret = parts[1];
        }

        // Validate client credentials
        if (!VALID_CLIENT_ID.equals(clientId) || !VALID_CLIENT_SECRET.equals(clientSecret)) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_client"));
        }

        // Exchange auth code for access token
        String uuid = tokenStore.getUUIDByAuthCode(authCode);
        if (uuid == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid_grant"));
        }

        String accessToken = UUID.randomUUID().toString();
        tokenStore.storeAccessToken(accessToken, uuid);
        log.info("/auth/token: accessToken generated={}", accessToken);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("access_token", accessToken);
        response.put("token_type", "Bearer");
        response.put("expires_in", 3600);

        return ResponseEntity.ok(response);
    }
}
