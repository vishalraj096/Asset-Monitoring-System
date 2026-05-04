package com.app.assetmonitoringsystem.service;

import com.app.assetmonitoringsystem.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class AlertConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(AlertConsumerService.class);
    private final EmailNotificationService emailNotificationService;

    public AlertConsumerService(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.ALERT_QUEUE)
    public void consumeAlertMessage(String message) {
        logger.info("=== ALERT NOTIFICATION RECEIVED ===");
        logger.info("Alert: {}", message);
        logger.info("===================================");
        try {
            emailNotificationService.sendAlertEmail("Alert Notification", message);
        } catch (Exception e) {
            logger.warn("Email notification failed (non-critical): {}", e.getMessage());
        }
    }
}
