package com.project.service;

import com.project.dto.OrderDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;

public interface OrderService {
    OrderDto create(OrderDto dto);

    OrderDto getByPublicId(String publicId);

    Page<OrderDto> getVisibleOrders(Authentication authentication, String type, String status, PageRequest pageRequest);

    OrderDto updateStatus(String publicId, String status, Authentication authentication);

    void delete(String publicId, Authentication authentication);
}