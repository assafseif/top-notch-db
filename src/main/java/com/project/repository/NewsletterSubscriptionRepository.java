package com.project.repository;

import com.project.entity.NewsletterSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscription, Long> {
    boolean existsByEmail(String email);
}