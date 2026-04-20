package com.project.controller;

import com.project.dto.ApiResponse;
import com.project.dto.OrderDto;
import com.project.dto.OrderStatusUpdateRequest;
import com.project.service.OrderService;
import com.project.service.StoreConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private StoreConfigurationService storeConfigurationService;

    @PostMapping
    public ApiResponse<OrderDto> create(@RequestBody OrderDto order) {
        return ApiResponse.of("Order saved successfully.", orderService.create(order));
    }

    @GetMapping("/{id}")
    public OrderDto getById(@PathVariable String id) {
        return orderService.getByPublicId(id);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('orders.view') or hasAuthority('wholesale.view')")
    public Page<OrderDto> getAll(
            Authentication authentication,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size
    ) {
        return orderService.getVisibleOrders(
                authentication,
                type,
                status,
            PageRequest.of(page, storeConfigurationService.resolvePageSize(size), Sort.by(Sort.Direction.DESC, "createdAt"))
        );
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('orders.manage') or hasAuthority('wholesale.manage')")
    public ApiResponse<OrderDto> updateStatus(
            @PathVariable String id,
            @RequestBody OrderStatusUpdateRequest request,
            Authentication authentication
    ) {
        return ApiResponse.of(
                "Order status updated successfully.",
                orderService.updateStatus(id, request.getStatus(), authentication)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('orders.manage') or hasAuthority('wholesale.manage')")
    public ApiResponse<Void> delete(@PathVariable String id, Authentication authentication) {
        orderService.delete(id, authentication);
        return ApiResponse.of("Order deleted successfully.");
    }
}