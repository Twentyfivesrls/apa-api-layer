package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.keycloak.representations.idm.RoleRepresentation;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApaRole {
    private String id;
    private String name;
    private String description;

    public RoleRepresentation toRoleRepresentation() {
        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setId(this.id);
        roleRepresentation.setName(this.name);
        roleRepresentation.setDescription(this.description);
        return roleRepresentation;
    }
}
