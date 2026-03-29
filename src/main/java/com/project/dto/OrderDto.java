package com.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private String id;
    private String type;
    private List<OrderItemDto> items;
    private OrderShippingDto shipping;
    private OrderBusinessDto business;
    private double subtotal;
    private double discount;
    private double total;
    private String status;
    private Date createdAt;
}