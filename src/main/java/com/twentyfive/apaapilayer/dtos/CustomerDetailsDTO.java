package com.twentyfive.apaapilayer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDetailsDTO {
    private String id;                  // Unique identifier for the customer
    private String firstName;           // First name of the customer
    private String lastName;            // Last name of the customer
    private String idKeycloak;          // Last name of the customer
    private String email;               // Email address of the customer
    private String role;                // Customer Role
    private String phoneNumber;         // Phone number of the customer
    private String completedOrdersCount;// Number of orders the customer has made
    private String activeOrdersCount;
    private String totalSpent;          // Total amount spent by the customer
    private boolean enabled;
    private String note;
}
