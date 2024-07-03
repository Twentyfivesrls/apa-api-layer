package com.twentyfive.apaapilayer.dtos;

import com.twentyfive.apaapilayer.models.CustomerAPA;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.Cart;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDTO {
    private String id;                // Unique identifier for the customer
    private String idKeycloak;        //
    private String firstName;         // First name of the customer
    private String lastName;          // Last name of the customer
    private String email;             // Email address of the customer
    private String phoneNumber;       // Phone number of the customer
    private String role;              // Customer's role
    private String note;
    private boolean enabled;

    public CustomerDTO (CustomerAPA capa){
        this.id=capa.getId();
        this.idKeycloak=capa.getIdKeycloak();
        this.firstName=capa.getFirstName();
        this.lastName=capa.getLastName();
        this.email=capa.getEmail();
        this.phoneNumber=capa.getPhoneNumber();
        this.role=capa.getRole();
        this.note=capa.getNote();
        this.enabled=capa.isEnabled();
    }
}
