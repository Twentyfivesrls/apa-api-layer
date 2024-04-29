package com.twentyfive.apaapilayer.utils;

import com.twentyfive.apaapilayer.models.CustomerAPA;
import com.twentyfive.twentyfivemodel.models.partenupModels.Utente;
import org.keycloak.representations.idm.CredentialRepresentation;
import twentyfive.twentyfiveadapter.dto.keycloakDto.KeycloakUser;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeycloakUtilities {
    public static KeycloakUser createUserForKeycloak(CustomerAPA customerAPA) {
        KeycloakUser user = new KeycloakUser();
        user.setEmail(customerAPA.getEmail());
        user.setFirstName(customerAPA.getName());
        user.setLastName(customerAPA.getSurname());
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("phoneNumber", Collections.singletonList(customerAPA.getPhoneNumber()));
        attributes.put("note", Collections.singletonList(customerAPA.getNote()));
        user.setAttributes(attributes);
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType("password");
        //FIXME vedere come fare registrazione Keycloak
        credential.setValue("prova");
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));
        user.setEnabled(true);
        return user;
    }
    public static KeycloakUser updateUserForKeycloak(CustomerAPA customerAPA) {
        KeycloakUser user = new KeycloakUser();
        user.setFirstName(customerAPA.getName());
        user.setLastName(customerAPA.getSurname());
        user.setEnabled(customerAPA.isEnabled());
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("phoneNumber", Collections.singletonList(customerAPA.getPhoneNumber()));
        attributes.put("note", Collections.singletonList(customerAPA.getNote()));
        user.setAttributes(attributes);
        return user;
    }


}
