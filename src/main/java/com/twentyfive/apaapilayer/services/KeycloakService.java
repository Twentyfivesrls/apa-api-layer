package com.twentyfive.apaapilayer.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twentyfive.apaapilayer.clients.TwentyfiveKeycloakClientController;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.dto.keycloakDto.TokenRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeycloakService {
    private final TwentyfiveKeycloakClientController twentyfiveKeycloakClientController;

    @Value("${keycloak.clientId}")
    protected String clientId;

    @Value("${twenty.internal}")
    protected String clientSecret;
    @Value("${keycloak.username}")
    protected String usernameAdmin;
    @Value("${keycloak.password}")
    protected String passwordAdmin;
    @Value("${twenty.username}")
    protected String username;
    public String getAccessToken() {
        // Creazione dei parametri x-www-form-urlencoded
        Map<String, String> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("grant_type", "password");
        params.put("username", usernameAdmin);
        params.put("password", passwordAdmin);

        TokenRequest request = new TokenRequest(clientId, clientSecret, "password", usernameAdmin, passwordAdmin);

        // Chiamata al client Feign per ottenere l'accessToken
        ResponseEntity<Object> response = twentyfiveKeycloakClientController.getToken(request);

        // Estrai l'accessToken dalla risposta
        ObjectMapper objectMapper = new ObjectMapper();
        Map responseMap = objectMapper.convertValue(response.getBody(), Map.class);
        return (String) responseMap.get("access_token");
    }

    public Map<String, List<String>> getEmailSettings(){
        String accessToken = this.getAccessToken();
        String authorizationHeader = "Bearer " + accessToken;
        UserRepresentation userRepresentation = twentyfiveKeycloakClientController.getUserFromUsername(authorizationHeader, username).getBody().get(0);
        return userRepresentation.getAttributes();
    }
}
