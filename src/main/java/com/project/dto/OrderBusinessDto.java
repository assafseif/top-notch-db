package com.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderBusinessDto {
    private String companyName;
    private String contactName;
    private String email;
    private String phone;
    private String businessType;
    private String orderVolume;
    private String message;
}