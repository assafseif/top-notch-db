package com.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto {
    private String productName;
    private String itemCode;
    private String size;
    private String color;
    private int quantity;
    private double unitPrice;
    private String notes;
}