package com.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderShippingDto {
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String city;
    private String zip;
    private String country;
}