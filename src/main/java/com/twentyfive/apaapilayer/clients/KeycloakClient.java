package com.twentyfive.apaapilayer.clients;

import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twentyfive.twentyfiveadapter.dto.keycloakDto.KeycloakRole;
import twentyfive.twentyfiveadapter.dto.keycloakDto.PasswordUpdateKeycloak;
import twentyfive.twentyfiveadapter.dto.keycloakDto.TokenRequest;

import java.util.List;

@FeignClient(name = "CustomerController", url = "http://80.211.123.141:8080")
public interface KeycloakClient {

    @RequestMapping(method = RequestMethod.POST, value="/realms/Twentyfive-internal/protocol/openid-connect/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<Object> getToken(@RequestBody TokenRequest params);

    @RequestMapping(method = RequestMethod.GET, value = "/admin/realms/Twentyfive-internal/users", produces = "application/json")
    ResponseEntity<List<UserRepresentation>> getAllUsers(@RequestHeader("Authorization") String accessToken);

    @RequestMapping(method = RequestMethod.GET, value = "/admin/realms/Twentyfive-internal/users", produces = "application/json")
    ResponseEntity<List<UserRepresentation>> getUserFromEmail(@RequestHeader("Authorization") String accessToken, @RequestParam String email);

    @RequestMapping(method = RequestMethod.GET, value="/admin/realms/Twentyfive-internal/ui-ext/brute-force-user")
    ResponseEntity<List<UserRepresentation>> search(@RequestHeader("Authorization") String accessToken, @RequestParam String search);

    @RequestMapping(method = RequestMethod.POST, value = "/admin/realms/Twentyfive-internal/users", produces = "application/json")
    ResponseEntity<Object> add(@RequestHeader("Authorization") String accessToken, @RequestBody UserRepresentation user);
    @RequestMapping(method = RequestMethod.PUT, value ="/admin/realms/Twentyfive-internal/users/{id}")
    ResponseEntity<UserRepresentation> update(@RequestHeader("Authorization") String accessToken, @PathVariable("id") String id,@RequestBody UserRepresentation user);
    @RequestMapping(method = RequestMethod.DELETE, value = "/admin/realms/Twentyfive-internal/users/{id}", produces = "application/json")
    ResponseEntity<Object> delete(@RequestHeader("Authorization") String accessToken, @PathVariable String id);

    @RequestMapping(method = RequestMethod.PUT, value = "/admin/realms/Twentyfive-internal/users/{id}/reset-password", produces = "application/json")
    ResponseEntity<Object> updatePassword(@RequestHeader("Authorization") String accessToken, @PathVariable String id, @RequestBody PasswordUpdateKeycloak newPassword);
    @RequestMapping(method = RequestMethod.GET, value = "/admin/realms/${keycloak.realm}/ui-ext/available-roles/users/{id}", produces = "application/json")
    ResponseEntity<List<KeycloakRole>> getAvailableRoles(@RequestHeader("Authorization") String accessToken,
                                                         @PathVariable String id,
                                                         @RequestParam(value = "first", defaultValue = "0") String first,
                                                         @RequestParam(value = "max", defaultValue = "100") String max,
                                                         @RequestParam(value = "search", defaultValue = "") String search);

    @RequestMapping(method = RequestMethod.POST, value = "/admin/realms/${keycloak.realm}/users/{id}/role-mappings/clients/{clientIdRole}", produces = "application/json")
    ResponseEntity<Object> addRoleToUser(@RequestHeader("Authorization") String accessToken,
                                         @PathVariable String id,
                                         @PathVariable String clientIdRole,
                                         @RequestBody List<RoleRepresentation> roles);

    @RequestMapping(method = RequestMethod.DELETE, value = "/admin/realms/${keycloak.realm}/users/{id}/role-mappings/clients/{clientIdRole}", produces = "application/json")
    ResponseEntity<Object> deleteRoleToUser(@RequestHeader("Authorization") String accessToken,
                                            @PathVariable String id,
                                            @PathVariable String clientIdRole,
                                            @RequestBody List<RoleRepresentation> roles);
    @RequestMapping(method = RequestMethod.GET, value = "/admin/realms/${keycloak.realm}/users/{id}/role-mappings", produces = "application/json")
    ResponseEntity<Object> getUserRole(@RequestHeader("Authorization") String accessToken, @PathVariable String id);
}
