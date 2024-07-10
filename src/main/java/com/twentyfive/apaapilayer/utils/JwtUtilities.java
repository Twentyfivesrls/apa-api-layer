package com.twentyfive.apaapilayer.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JwtUtilities {
    public static List<String> getRoles() throws IOException {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization").split(" ")[1];

        DecodedJWT decoded = JWT.decode(token);
        String payload = new String(java.util.Base64.getDecoder().decode(decoded.getPayload()));

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(payload);
        JsonNode apaAppNode = rootNode.path("resource_access").path("apa-app").path("roles");

        List<String> roles = new ArrayList<>();
        if (apaAppNode.isArray()) {
            Iterator<JsonNode> elements = apaAppNode.elements();
            while (elements.hasNext()) {
                roles.add(elements.next().asText());
            }
        }

        return roles;
    }
}
