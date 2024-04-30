package com.twentyfive.apaapilayer.utils;

import com.twentyfive.apaapilayer.models.CustomerAPA;
import org.keycloak.representations.idm.CredentialRepresentation;
import twentyfive.twentyfiveadapter.dto.keycloakDto.KeycloakUser;

import java.util.*;

public class KeycloakUtilities {
    public static KeycloakUser createUserForKeycloak(CustomerAPA customerAPA) {
        KeycloakUser user = new KeycloakUser();
        user.setEmail(customerAPA.getEmail());
        user.setFirstName(customerAPA.getFirstName());
        user.setLastName(customerAPA.getLastName());
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("phoneNumber", Collections.singletonList(customerAPA.getPhoneNumber()));
        attributes.put("note", Collections.singletonList(customerAPA.getNote()));
        user.setAttributes(attributes);
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType("password");
        credential.setValue(String.valueOf(UUID.randomUUID()));
        credential.setTemporary(true);
        user.setCredentials(List.of(credential));
        user.setEnabled(true);
        return user;
    }
    public static KeycloakUser updateUserForKeycloak(CustomerAPA customerAPA) {
        KeycloakUser user = new KeycloakUser();
        user.setFirstName(customerAPA.getFirstName());
        user.setLastName(customerAPA.getLastName());
        user.setEnabled(customerAPA.isEnabled());
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("phoneNumber", Collections.singletonList(customerAPA.getPhoneNumber()));
        attributes.put("note", Collections.singletonList(customerAPA.getNote()));
        user.setAttributes(attributes);
        return user;
    }


}
