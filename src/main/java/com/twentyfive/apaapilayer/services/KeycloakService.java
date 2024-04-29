package com.twentyfive.apaapilayer.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twentyfive.apaapilayer.clients.KeycloakClient;
import com.twentyfive.apaapilayer.models.CustomerAPA;
import com.twentyfive.apaapilayer.utils.KeycloakUtilities;
import com.twentyfive.twentyfivemodel.models.partenupModels.Utente;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.dto.keycloakDto.KeycloakRole;
import twentyfive.twentyfiveadapter.dto.keycloakDto.KeycloakUser;
import twentyfive.twentyfiveadapter.dto.keycloakDto.TokenRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KeycloakService {
    private final KeycloakClient keycloakClient;

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
        ResponseEntity<Object> response = keycloakClient.getTokenFromTwentyfiveInternal(request);

        // Estrai l'accessToken dalla risposta
        ObjectMapper objectMapper = new ObjectMapper();
        Map responseMap = objectMapper.convertValue(response.getBody(), Map.class);
        return (String) responseMap.get("access_token");
    }

    public Map<String, List<String>> getEmailSettings(){
        String accessToken = this.getAccessTokenTF();
        String authorizationHeader = "Bearer " + accessToken;
        UserRepresentation userRepresentation = keycloakClient.getUserFromUsernameTwentyfiveInternal(authorizationHeader, username).getBody().get(0);
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
        ResponseEntity<Object> response = keycloakClient.getToken(request);

        // Estrai l'accessToken dalla risposta
        ObjectMapper objectMapper = new ObjectMapper();
        Map responseMap = objectMapper.convertValue(response.getBody(), Map.class);
        return (String) responseMap.get("access_token");
    }

    public void add(CustomerAPA customerAPA) {
        String accessToken = this.getAccessToken(clientId, clientSecret, "adminrealm", "password");
        String authorizationHeader = "Bearer " + accessToken;
        ResponseEntity<Object> result = keycloakClient.add(authorizationHeader, KeycloakUtilities.createUserForKeycloak(customerAPA));
        String[] stringArray = result.getHeaders().get("location").get(0).split("/");
        String id = stringArray[stringArray.length - 1];
        customerAPA.setIdKeycloak(id);
        ResponseEntity<List<KeycloakRole>> ruoli = keycloakClient.getAvailableRoles(authorizationHeader, id, "0", "100", "");
        List<KeycloakRole> listaRuoli = ruoli.getBody();
        List<KeycloakRole> ruoliSelezionati = listaRuoli.stream().filter(element -> element.getRole().equals("cliente")).toList();
        String clientIdRole = ruoliSelezionati.get(0).getClientId();
        keycloakClient.addRoleToUser(authorizationHeader, id, clientIdRole, ruoliSelezionati.stream().map(KeycloakRole::toRoleRepresentation).collect(Collectors.toList()));
    }
    public void update(CustomerAPA customerAPA) {
        String accessToken = this.getAccessToken(clientId, clientSecret, "adminrealm", "password");
        String authorizationHeader = "Bearer " + accessToken;
        keycloakClient.update(authorizationHeader, customerAPA.getIdKeycloak(), KeycloakUtilities.updateUserForKeycloak(customerAPA)).getBody();
    }
}
