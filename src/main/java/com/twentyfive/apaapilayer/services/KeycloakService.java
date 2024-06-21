package com.twentyfive.apaapilayer.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twentyfive.apaapilayer.clients.KeycloakExtClient;
import com.twentyfive.apaapilayer.clients.KeycloakIntClient;
import com.twentyfive.apaapilayer.models.CustomerAPA;
import com.twentyfive.apaapilayer.utils.KeycloakUtilities;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.dto.keycloakDto.KeycloakRole;
import twentyfive.twentyfiveadapter.dto.keycloakDto.KeycloakUser;
import twentyfive.twentyfiveadapter.dto.keycloakDto.TokenRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KeycloakService {
    private final KeycloakExtClient keycloakExtClient;
    private final KeycloakIntClient keycloakIntClient;

    @Value("${keycloak.clientId}")
    protected String clientId;

    @Value("${twenty.internal}")
    protected String clientSecretTF;
    @Value("${keycloak.credentials.secret}")
    protected String clientSecret;
    @Value("${keycloak.username}")
    protected String usernameAdmin;
    @Value("${keycloak.password}")
    protected String passwordAdmin;
    @Value("${twenty.username}")
    protected String username;
    @Value("${keycloak.realm}")
    protected String realm;

    public String getAccessTokenTF() {
        // Creazione dei parametri x-www-form-urlencoded
        Map<String, String> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("client_secret", clientSecretTF);
        params.put("grant_type", "password");
        params.put("username", usernameAdmin);
        params.put("password", passwordAdmin);

        TokenRequest request = new TokenRequest(clientId, clientSecretTF, "password", usernameAdmin, passwordAdmin);

        // Chiamata al client Feign per ottenere l'accessToken
        ResponseEntity<Object> response = keycloakIntClient.getTokenFromTwentyfiveInternal(request);

        // Estrai l'accessToken dalla risposta
        ObjectMapper objectMapper = new ObjectMapper();
        Map responseMap = objectMapper.convertValue(response.getBody(), Map.class);
        return (String) responseMap.get("access_token");
    }

    public Map<String, List<String>> getEmailSettings(){
        String accessToken = this.getAccessTokenTF();
        String authorizationHeader = "Bearer " + accessToken;
        UserRepresentation userRepresentation = keycloakIntClient.getUserFromUsernameTwentyfiveInternal(authorizationHeader, username).getBody().get(0);
        return userRepresentation.getAttributes();
    }

    public String getAccessToken(String clientId, String clientSecret, String username, String password) {
        // Creazione dei parametri x-www-form-urlencoded
        Map<String, String> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("grant_type", "password");
        params.put("username", username);
        params.put("password", password);

        TokenRequest request = new TokenRequest(clientId, clientSecret, "password", username, password);

        // Chiamata al client Feign per ottenere l'accessToken
        ResponseEntity<Object> response = keycloakExtClient.getToken(request);

        // Estrai l'accessToken dalla risposta
        ObjectMapper objectMapper = new ObjectMapper();
        Map responseMap = objectMapper.convertValue(response.getBody(), Map.class);
        return (String) responseMap.get("access_token");
    }

    public void add(CustomerAPA customerAPA) {
        String accessToken = this.getAccessToken(clientId, clientSecret, "adminrealm", "password");
        String authorizationHeader = "Bearer " + accessToken;
        KeycloakUser keycloakUser = KeycloakUtilities.createUserForKeycloak(customerAPA);
        ResponseEntity<Object> result = keycloakExtClient.add(authorizationHeader, keycloakUser);
        String[] stringArray = result.getHeaders().get("location").get(0).split("/");
        String id = stringArray[stringArray.length - 1];
        customerAPA.setIdKeycloak(id);
        ResponseEntity<List<KeycloakRole>> ruoli = keycloakExtClient.getAvailableRoles(authorizationHeader, id, "0", "100", "");
        List<KeycloakRole> listaRuoli = ruoli.getBody();
        List<KeycloakRole> ruoliSelezionati = listaRuoli.stream().filter(element -> element.getRole().equals("customer")).toList();
        String clientIdRole = ruoliSelezionati.get(0).getClientId();
        keycloakExtClient.addRoleToUser(authorizationHeader, id, clientIdRole, ruoliSelezionati.stream().map(KeycloakRole::toRoleRepresentation).collect(Collectors.toList()));
    }
    public void update(CustomerAPA customerAPA) {
        String accessToken = this.getAccessToken(clientId, clientSecret, "adminrealm", "password");
        String authorizationHeader = "Bearer " + accessToken;
        keycloakExtClient.update(authorizationHeader, customerAPA.getIdKeycloak(), KeycloakUtilities.updateUserForKeycloak(customerAPA)).getBody();
    }

    public void sendPasswordResetEmail(String userId) {
        String accessToken = this.getAccessToken(clientId, clientSecret, "adminrealm", "password");

        // Define the actions to be executed, in this case, UPDATE_PASSWORD
        List<String> actions = Collections.singletonList("UPDATE_PASSWORD");

        // Call the Feign client method to send the reset email
        keycloakExtClient.resetPassword("Bearer " + accessToken, userId, actions);
    }

    public void deleteUser(String idKeycloak) {
        String accessToken = this.getAccessToken(clientId, clientSecret, "adminrealm", "password");
        String authorizationHeader = "Bearer " + accessToken;
        keycloakExtClient.deleteUser(realm,idKeycloak,authorizationHeader);
    }
}
