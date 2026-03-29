package com.project.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.dto.OrderBusinessDto;
import com.project.dto.OrderDto;
import com.project.dto.OrderItemDto;
import com.project.dto.OrderShippingDto;
import com.project.entity.CustomerOrder;
import com.project.repository.CustomerOrderRepository;
import com.project.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Set<String> VALID_TYPES = Set.of("cart", "wholesale");
    private static final Set<String> VALID_STATUSES = Set.of("pending", "confirmed", "shipped", "completed", "cancelled");
    private static final TypeReference<List<OrderItemDto>> ORDER_ITEMS_TYPE = new TypeReference<>() {};

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public OrderDto create(OrderDto dto) {
        validateForCreate(dto);

        CustomerOrder order = CustomerOrder.builder()
                .publicId(resolvePublicId(dto))
                .type(normalizeType(dto.getType()))
                .status(normalizeStatus(dto.getStatus()))
                .subtotal(dto.getSubtotal())
                .discount(dto.getDiscount())
                .total(dto.getTotal())
                .itemsJson(writeJson(dto.getItems()))
                .shippingJson(writeNullableJson(dto.getShipping()))
                .businessJson(writeNullableJson(dto.getBusiness()))
                .createdAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : new Date())
                .build();

        return toDto(customerOrderRepository.save(order));
    }

    @Override
    public Page<OrderDto> getVisibleOrders(Authentication authentication, String type, String status, PageRequest pageRequest) {
        Set<String> authorities = getAuthorities(authentication);
        boolean canViewCart = authorities.contains("orders.view");
        boolean canViewWholesale = authorities.contains("wholesale.view");

        if (!canViewCart && !canViewWholesale) {
            throw new AccessDeniedException("You do not have permission to view orders.");
        }

        String normalizedType = normalizeRequestedType(type, authorities);
        String normalizedStatus = normalizeRequestedStatus(status);

        Specification<CustomerOrder> specification = Specification.where(matchesVisibleTypes(authorities, normalizedType))
                .and(matchesStatus(normalizedStatus));

        Page<CustomerOrder> page = customerOrderRepository.findAll(specification, pageRequest);
        List<OrderDto> dtos = page.stream().map(this::toDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }

    @Override
    public OrderDto updateStatus(String publicId, String status, Authentication authentication) {
        CustomerOrder order = findByPublicId(publicId);
        ensureCanManage(order.getType(), getAuthorities(authentication));
        order.setStatus(normalizeStatus(status));
        return toDto(customerOrderRepository.save(order));
    }

    @Override
    public void delete(String publicId, Authentication authentication) {
        CustomerOrder order = findByPublicId(publicId);
        ensureCanManage(order.getType(), getAuthorities(authentication));
        customerOrderRepository.delete(order);
    }

    private CustomerOrder findByPublicId(String publicId) {
        return customerOrderRepository.findByPublicId(publicId)
                .orElseThrow(() -> new NoSuchElementException("Order not found."));
    }

    private OrderDto toDto(CustomerOrder order) {
        return OrderDto.builder()
                .id(order.getPublicId())
                .type(order.getType())
                .items(readItems(order.getItemsJson()))
                .shipping(readNullableJson(order.getShippingJson(), OrderShippingDto.class))
                .business(readNullableJson(order.getBusinessJson(), OrderBusinessDto.class))
                .subtotal(order.getSubtotal())
                .discount(order.getDiscount())
                .total(order.getTotal())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private void validateForCreate(OrderDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Order details are required.");
        }

        String type = normalizeType(dto.getType());

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must include at least one item.");
        }
        if (dto.getSubtotal() < 0 || dto.getDiscount() < 0 || dto.getTotal() < 0) {
            throw new IllegalArgumentException("Order totals must be zero or greater.");
        }

        if ("cart".equals(type)) {
            if (dto.getShipping() == null) {
                throw new IllegalArgumentException("Shipping details are required for cart orders.");
            }
            if (isBlank(dto.getShipping().getFirstName()) || isBlank(dto.getShipping().getLastName())) {
                throw new IllegalArgumentException("Shipping first and last name are required.");
            }
        }

        if ("wholesale".equals(type)) {
            if (dto.getBusiness() == null) {
                throw new IllegalArgumentException("Business details are required for wholesale orders.");
            }
            if (isBlank(dto.getBusiness().getCompanyName()) || isBlank(dto.getBusiness().getContactName())) {
                throw new IllegalArgumentException("Company and contact name are required for wholesale orders.");
            }
        }

        normalizeStatus(dto.getStatus());
    }

    private void ensureCanManage(String type, Set<String> authorities) {
        String normalizedType = normalizeType(type);
        boolean allowed = "cart".equals(normalizedType)
                ? authorities.contains("orders.manage")
                : authorities.contains("wholesale.manage");

        if (!allowed) {
            throw new AccessDeniedException("You do not have permission to manage this order.");
        }
    }

    private boolean canViewType(String type, Set<String> authorities) {
        String normalizedType = normalizeType(type);
        return "cart".equals(normalizedType)
                ? authorities.contains("orders.view")
                : authorities.contains("wholesale.view");
    }

    private Specification<CustomerOrder> matchesVisibleTypes(Set<String> authorities, String type) {
        return (root, query, criteriaBuilder) -> {
            if (type != null) {
                return criteriaBuilder.equal(root.get("type"), type);
            }

            boolean canViewCart = authorities.contains("orders.view");
            boolean canViewWholesale = authorities.contains("wholesale.view");

            if (canViewCart && canViewWholesale) {
                return criteriaBuilder.conjunction();
            }
            if (canViewCart) {
                return criteriaBuilder.equal(root.get("type"), "cart");
            }
            return criteriaBuilder.equal(root.get("type"), "wholesale");
        };
    }

    private Specification<CustomerOrder> matchesStatus(String status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    private Set<String> getAuthorities(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return Set.of();
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    private String normalizeType(String type) {
        String normalized = type == null ? "" : type.trim().toLowerCase(Locale.ROOT);
        if (!VALID_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("Order type must be either cart or wholesale.");
        }
        return normalized;
    }

    private String normalizeStatus(String status) {
        String normalized = status == null || status.isBlank()
                ? "pending"
                : status.trim().toLowerCase(Locale.ROOT);

        if (!VALID_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Order status is invalid.");
        }
        return normalized;
    }

    private String normalizeRequestedType(String type, Set<String> authorities) {
        if (type == null || type.isBlank() || "all".equalsIgnoreCase(type.trim())) {
            return null;
        }

        String normalizedType = normalizeType(type);
        if (!canViewType(normalizedType, authorities)) {
            throw new AccessDeniedException("You do not have permission to view this order type.");
        }
        return normalizedType;
    }

    private String normalizeRequestedStatus(String status) {
        if (status == null || status.isBlank() || "all".equalsIgnoreCase(status.trim())) {
            return null;
        }
        return normalizeStatus(status);
    }

    private String resolvePublicId(OrderDto dto) {
        String requestedId = dto.getId() == null ? "" : dto.getId().trim();
        if (!requestedId.isEmpty() && !customerOrderRepository.existsByPublicId(requestedId)) {
            return requestedId;
        }

        String prefix = "cart".equals(normalizeType(dto.getType())) ? "ORD" : "WHL";
        String candidate;
        do {
            candidate = prefix + "-" + System.currentTimeMillis();
        } while (customerOrderRepository.existsByPublicId(candidate));
        return candidate;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to store order details.");
        }
    }

    private String writeNullableJson(Object value) {
        return value == null ? null : writeJson(value);
    }

    private List<OrderItemDto> readItems(String json) {
        try {
            return objectMapper.readValue(json, ORDER_ITEMS_TYPE);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to read stored order items.");
        }
    }

    private <T> T readNullableJson(String json, Class<T> type) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to read stored order details.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}