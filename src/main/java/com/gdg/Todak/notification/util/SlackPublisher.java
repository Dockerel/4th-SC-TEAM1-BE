package com.gdg.Todak.notification.util;

import com.gdg.Todak.notification.dto.PublishNotificationRequest;
import com.slack.api.Slack;
import com.slack.api.webhook.WebhookResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class SlackPublisher {

    @Value("${slack.webhook.url}")
    private String slackWebhookUrl;

    public void publishSlackMessage(PublishNotificationRequest request) {
        Slack slack = Slack.getInstance();
        String errorPublishNotificationRequestString = request.toString();
        String errorMessage = String.format("Error Publishing Notification Request:\n%s", errorPublishNotificationRequestString);
        try {
            WebhookResponse response = slack.send(slackWebhookUrl, errorMessage);
            log.info("Slack Webhook Response: {}", response);
        } catch (IOException e) {
            log.error("Error Publishing Notification Request: {}", errorPublishNotificationRequestString, e);
        }
    }
}
