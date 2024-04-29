package com.twentyfive.apaapilayer.DTOs;

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
    private String note;
    private boolean enabled;

    public CustomerDTO (CustomerAPA capa){
        this.id=capa.getId();
        this.idKeycloak=capa.getIdKeycloak();
        this.firstName=capa.getName();
        this.lastName=capa.getSurname();
        this.email=capa.getEmail();
        this.phoneNumber=capa.getPhoneNumber();
        this.note=capa.getNote();
        this.enabled=capa.isEnabled();
    }

    public CustomerAPA toCustomerAPA (){
        CustomerAPA capa= new CustomerAPA();
        capa.setId(this.getId());
        capa.setIdKeycloak(this.idKeycloak);
        capa.setName(this.getFirstName());
        capa.setSurname(this.getLastName());
        capa.setEmail(this.getEmail());
        capa.setPhoneNumber(this.getPhoneNumber());
        capa.setEnabled(this.enabled);
        capa.setNote(this.note);
        capa.setCart(new Cart());
        return capa;
    }
}
