package spring.fitlinkbe.interfaces.controller.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.ConfirmSubscriptionRequest;
import spring.fitlinkbe.application.auth.AuthFacade;
import spring.fitlinkbe.interfaces.controller.auth.dto.SnsAlertDto;
import spring.fitlinkbe.interfaces.controller.auth.dto.SnsEmailNotificationDto;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SnsController {

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    private final AuthFacade authFacade;

    private static class SnsTypeConstants {
        private static final String SUBSCRIPTION_CONFIRMATION = "SubscriptionConfirmation";
        private static final String NOTIFICATION = "Notification";
    }

    private static class NotificationTypeConstants {
        private static final String RECEIVED = "Received";
    }

    /**
     * SNS Topic 구독
     */
    @PostMapping(value = "/v1/sns")
    public void test(@RequestBody String body) throws JsonProcessingException {
        SnsAlertDto snsAlertDto = objectMapper.readValue(body, SnsAlertDto.class);

        try {
            if (snsAlertDto.type().equals(SnsTypeConstants.SUBSCRIPTION_CONFIRMATION)) {
                confirmSubscription(snsAlertDto);
            } else if (snsAlertDto.type().equals(SnsTypeConstants.NOTIFICATION)) {
                handleNotification(snsAlertDto);
            }
        } catch (Exception e) {
            log.error("Error processing sns: {}", e.getMessage());
        }
    }

    private void handleNotification(SnsAlertDto dto) throws JsonProcessingException {
        SnsEmailNotificationDto snsEmailNotificationDto = objectMapper.readValue(dto.message(), SnsEmailNotificationDto.class);

        if (snsEmailNotificationDto.notificationType().equals(NotificationTypeConstants.RECEIVED)) {
            authFacade.verifySnsEmail(snsEmailNotificationDto);
        }
    }

    private void confirmSubscription(SnsAlertDto dto) {
        ConfirmSubscriptionRequest request = ConfirmSubscriptionRequest.builder()
                .token(dto.token())
                .topicArn(dto.topicArn())
                .build();

        snsClient.confirmSubscription(request);
    }
}
