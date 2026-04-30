package com.project.service.impl;

import com.project.entity.NewsletterSubscription;
import com.project.repository.NewsletterSubscriptionRepository;
import com.project.service.NewsletterSubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class NewsletterSubscriptionServiceImpl implements NewsletterSubscriptionService {

    @Autowired
    private NewsletterSubscriptionRepository newsletterSubscriptionRepository;

    @Override
    public void subscribe(String email) {
        String normalizedEmail = normalizeEmail(email);

        if (newsletterSubscriptionRepository.existsByEmail(normalizedEmail)) {
            return;
        }

        newsletterSubscriptionRepository.save(NewsletterSubscription.builder()
                .email(normalizedEmail)
                .build());
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email address is required.");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (normalizedEmail.isEmpty()) {
            throw new IllegalArgumentException("Email address is required.");
        }

        return normalizedEmail;
    }
}