package com.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreConfigurationDto {
    private String whatsappNumber;
    private Integer defaultPageSize;
    private Double shippingPrice;
    private String promoBannerMessage;
    private String couponCode;
    private Integer couponDiscountPercentage;
}