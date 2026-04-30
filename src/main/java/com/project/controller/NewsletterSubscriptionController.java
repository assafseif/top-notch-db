package com.project.controller;

import com.project.dto.ApiResponse;
import com.project.dto.NewsletterSubscriptionRequest;
import com.project.service.NewsletterSubscriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/newsletter-subscriptions")
public class NewsletterSubscriptionController {

    @Autowired
    private NewsletterSubscriptionService newsletterSubscriptionService;

    @PostMapping
    public ApiResponse<Void> subscribe(@Valid @RequestBody NewsletterSubscriptionRequest request) {
        newsletterSubscriptionService.subscribe(request.getEmail());
        return ApiResponse.of("Email saved successfully.");
    }
}