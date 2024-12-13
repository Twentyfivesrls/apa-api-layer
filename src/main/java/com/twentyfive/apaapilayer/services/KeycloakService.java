package com.twentyfive.apaapilayer.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twentyfive.apaapilayer.clients.KeycloakExtClient;
import com.twentyfive.apaapilayer.clients.KeycloakIntClient;
import com.twentyfive.apaapilayer.dtos.ApaRole;
import com.twentyfive.apaapilayer.exceptions.InvalidKeycloakIdRequestException;
import com.twentyfive.apaapilayer.models.CustomerAPA;
import com.twentyfive.apaapilayer.utils.JwtUtilities;
import com.twentyfive.apaapilayer.utils.KeycloakUtilities;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.dto.keycloakDto.KeycloakRole;
import twentyfive.twentyfiveadapter.dto.keycloakDto.KeycloakUser;
import twentyfive.twentyfiveadapter.dto.keycloakDto.TokenRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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
    @Value("${keycloak.apaId}")
    protected String apaClientId;
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
        ResponseEntity<Object> response = keycloakExtClient.getToken(request);

        // Estrai l'accessToken dalla risposta
        ObjectMapper objectMapper = new ObjectMapper();
        Map responseMap = objectMapper.convertValue(response.getBody(), Map.class);
        return (String) responseMap.get("access_token");
    }

    public void add(CustomerAPA customerAPA) {
        String accessToken = this.getAccessToken();
        String authorizationHeader = "Bearer " + accessToken;
        KeycloakUser keycloakUser = KeycloakUtilities.createUserForKeycloak(customerAPA);
        ResponseEntity<Object> result = keycloakExtClient.add(authorizationHeader, keycloakUser);
        String[] stringArray = result.getHeaders().get("location").get(0).split("/");
        String id = stringArray[stringArray.length - 1];
        customerAPA.setIdKeycloak(id);
        addRoleToUser(authorizationHeader,id,customerAPA);
    }
    public void update(CustomerAPA customerAPA) throws IOException {
        String accessToken = this.getAccessToken();
        String authorizationHeader = "Bearer " + accessToken;
        List<String> roles = JwtUtilities.getRoles();
        if(!roles.contains("admin")){ //Allora è un customer, controlliamo se sta provando a modificare se stesso
            if(!(JwtUtilities.getIdKeycloak().equals(customerAPA.getIdKeycloak()))){
                throw new InvalidKeycloakIdRequestException();
            }
        }
        addRoleToUser(authorizationHeader, customerAPA.getIdKeycloak(), customerAPA);
        keycloakExtClient.update(authorizationHeader, customerAPA.getIdKeycloak(), KeycloakUtilities.updateUserForKeycloak(customerAPA)).getBody();
    }

    public void sendPasswordResetEmail(String userId) {
        String accessToken = this.getAccessToken();

        // Define the actions to be executed, in this case, UPDATE_PASSWORD
        List<String> actions = Collections.singletonList("UPDATE_PASSWORD");

        // Call the Feign client method to send the reset email
        keycloakExtClient.resetPassword("Bearer " + accessToken, userId, actions);
    }

    public void deleteUser(String idKeycloak) {
        String accessToken = this.getAccessToken();
        String authorizationHeader = "Bearer " + accessToken;
        keycloakExtClient.deleteUser(realm,idKeycloak,authorizationHeader);
    }

    public void addRoleToUser(String authorizationHeader, String id,CustomerAPA customerAPA){
        ResponseEntity<List<ApaRole>> ruoliApa = keycloakExtClient.getApaRoles(authorizationHeader);
        List<ApaRole> listaRuoli = ruoliApa.getBody();
        List<ApaRole> ruoliSelezionati = listaRuoli.stream().filter(element -> element.getName().equals(customerAPA.getRole())).toList();
        keycloakExtClient.addRoleToUser(authorizationHeader, id, ruoliSelezionati.stream().map(ApaRole::toRoleRepresentation).collect(Collectors.toList()));
    }

    public void removeRole(String idKeycloak, String role) {
        String accessToken = this.getAccessToken();
        String authorizationHeader = "Bearer " + accessToken;
        ResponseEntity<List<ApaRole>> ruoliApa = keycloakExtClient.getApaRoles(authorizationHeader);
        List<ApaRole> listaRuoli = ruoliApa.getBody();
        List<ApaRole> ruoliSelezionati = listaRuoli.stream().filter(element -> element.getName().equals(role)).toList();
        keycloakExtClient.deleteRoleToUser(authorizationHeader, idKeycloak, ruoliSelezionati.stream().map(ApaRole::toRoleRepresentation).collect(Collectors.toList()));
    }
}
