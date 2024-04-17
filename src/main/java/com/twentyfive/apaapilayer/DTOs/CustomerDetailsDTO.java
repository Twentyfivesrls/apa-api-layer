package com.twentyfive.apaapilayer.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDetailsDTO {
    private String id;                // Unique identifier for the customer
    private String firstName;         // First name of the customer
    private String lastName;          // Last name of the customer
    private String email;             // Email address of the customer
    private String phoneNumber;       // Phone number of the customer
    private String orderCount;           // Number of orders the customer has made
    private String totalSpent;        // Total amount spent by the customer
}
