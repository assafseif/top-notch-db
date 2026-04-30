package com.project.service.impl;

import com.project.dto.StoreConfigurationDto;
import com.project.entity.StoreConfiguration;
import com.project.repository.StoreConfigurationRepository;
import com.project.service.StoreConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StoreConfigurationServiceImpl implements StoreConfigurationService {
    private static final String DEFAULT_WHATSAPP_NUMBER = "+96170238705";
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final double DEFAULT_SHIPPING_PRICE = 0D;
    private static final String DEFAULT_PROMO_BANNER_MESSAGE = "Free shipping on orders over USD 100.00 - Use COUPON for 10% off";
    private static final String DEFAULT_COUPON_CODE = "COUPON";
    private static final int DEFAULT_COUPON_DISCOUNT_PERCENTAGE = 10;
    private static final String WHATSAPP_NUMBER_FIELD = "whatsapp_number";
    private static final String PAGINATION_FIELD = "pagination";
    private static final String SHIPPING_PRICE_FIELD = "shipping_price";
    private static final String PROMO_BANNER_MESSAGE_FIELD = "promo_banner_message";
    private static final String COUPON_CODE_FIELD = "coupon_code";
    private static final String COUPON_DISCOUNT_PERCENTAGE_FIELD = "coupon_discount_percentage";

    @Autowired
    private StoreConfigurationRepository storeConfigurationRepository;

    @Override
    public StoreConfigurationDto getCurrent() {
        return StoreConfigurationDto.builder()
                .whatsappNumber(getConfigurationValue(WHATSAPP_NUMBER_FIELD, DEFAULT_WHATSAPP_NUMBER))
                .defaultPageSize(resolveConfiguredPageSize())
            .shippingPrice(resolveConfiguredShippingPrice())
                .promoBannerMessage(getConfigurationValue(PROMO_BANNER_MESSAGE_FIELD, DEFAULT_PROMO_BANNER_MESSAGE))
                .couponCode(getConfigurationValue(COUPON_CODE_FIELD, DEFAULT_COUPON_CODE))
                .couponDiscountPercentage(resolveConfiguredCouponDiscountPercentage())
                .build();
    }

    @Override
    public StoreConfigurationDto update(StoreConfigurationDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Configuration details are required.");
        }

        String whatsappNumber = dto.getWhatsappNumber() == null ? "" : dto.getWhatsappNumber().trim();
        if (whatsappNumber.isEmpty()) {
            throw new IllegalArgumentException("WhatsApp number is required.");
        }

        Integer defaultPageSize = dto.getDefaultPageSize();
        if (defaultPageSize == null || defaultPageSize < 1) {
            throw new IllegalArgumentException("Default page size must be at least 1.");
        }

        Double shippingPrice = dto.getShippingPrice();
        if (shippingPrice == null || shippingPrice < 0) {
            throw new IllegalArgumentException("Shipping price must be zero or greater.");
        }

        String promoBannerMessage = dto.getPromoBannerMessage() == null ? "" : dto.getPromoBannerMessage().trim();
        if (promoBannerMessage.isEmpty()) {
            throw new IllegalArgumentException("Promo banner message is required.");
        }

        String couponCode = dto.getCouponCode() == null ? "" : dto.getCouponCode().trim();
        if (couponCode.isEmpty()) {
            throw new IllegalArgumentException("Coupon code is required.");
        }

        Integer couponDiscountPercentage = dto.getCouponDiscountPercentage();
        if (couponDiscountPercentage == null || couponDiscountPercentage < 1 || couponDiscountPercentage > 100) {
            throw new IllegalArgumentException("Coupon discount percentage must be between 1 and 100.");
        }

        upsertConfigurationValue(WHATSAPP_NUMBER_FIELD, whatsappNumber);
        upsertConfigurationValue(PAGINATION_FIELD, String.valueOf(defaultPageSize));
        upsertConfigurationValue(SHIPPING_PRICE_FIELD, String.valueOf(shippingPrice));
        upsertConfigurationValue(PROMO_BANNER_MESSAGE_FIELD, promoBannerMessage);
        upsertConfigurationValue(COUPON_CODE_FIELD, couponCode);
        upsertConfigurationValue(COUPON_DISCOUNT_PERCENTAGE_FIELD, String.valueOf(couponDiscountPercentage));
        return getCurrent();
    }

    @Override
    public int resolvePageSize(Integer requestedSize) {
        if (requestedSize != null && requestedSize > 0) {
            return requestedSize;
        }

        return resolveConfiguredPageSize();
    }

    private int resolveConfiguredPageSize() {
        String configuredValue = getConfigurationValue(PAGINATION_FIELD, String.valueOf(DEFAULT_PAGE_SIZE));
        try {
            int pageSize = Integer.parseInt(configuredValue);
            return pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        } catch (NumberFormatException exception) {
            return DEFAULT_PAGE_SIZE;
        }
    }

    private int resolveConfiguredCouponDiscountPercentage() {
        String configuredValue = getConfigurationValue(COUPON_DISCOUNT_PERCENTAGE_FIELD, String.valueOf(DEFAULT_COUPON_DISCOUNT_PERCENTAGE));
        try {
            int couponDiscountPercentage = Integer.parseInt(configuredValue);
            return couponDiscountPercentage >= 1 && couponDiscountPercentage <= 100
                    ? couponDiscountPercentage
                    : DEFAULT_COUPON_DISCOUNT_PERCENTAGE;
        } catch (NumberFormatException exception) {
            return DEFAULT_COUPON_DISCOUNT_PERCENTAGE;
        }
    }

    private double resolveConfiguredShippingPrice() {
        String configuredValue = getConfigurationValue(SHIPPING_PRICE_FIELD, String.valueOf(DEFAULT_SHIPPING_PRICE));
        try {
            double shippingPrice = Double.parseDouble(configuredValue);
            return shippingPrice >= 0 ? shippingPrice : DEFAULT_SHIPPING_PRICE;
        } catch (NumberFormatException exception) {
            return DEFAULT_SHIPPING_PRICE;
        }
    }

    private String getConfigurationValue(String fieldName, String defaultValue) {
        return storeConfigurationRepository.findByFieldName(fieldName)
                .map(StoreConfiguration::getFieldValue)
                .filter(value -> value != null && !value.trim().isEmpty())
                .orElseGet(() -> upsertConfigurationValue(fieldName, defaultValue).getFieldValue());
    }

    private StoreConfiguration upsertConfigurationValue(String fieldName, String fieldValue) {
        StoreConfiguration configuration = storeConfigurationRepository.findByFieldName(fieldName)
                .orElseGet(() -> StoreConfiguration.builder().fieldName(fieldName).build());
        configuration.setFieldValue(fieldValue);
        return storeConfigurationRepository.save(configuration);
    }
}