package spring.fitlinkbe.interfaces.controller.sns.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SnsAlertDto(
        @JsonProperty("Type") String type,
        @JsonProperty("MessageId") String messageId,
        @JsonProperty("Token") String token,
        @JsonProperty("TopicArn") String topicArn,
        @JsonProperty("Subject") String subject,
        @JsonProperty("Message") String message,
        @JsonProperty("SubscribeURL") String subscribeURL,
        @JsonProperty("Timestamp") String timestamp,
        @JsonProperty("SignatureVersion") String signatureVersion,
        @JsonProperty("Signature") String signature,
        @JsonProperty("SigningCertURL") String signingCertURL,
        @JsonProperty("UnsubscribeURL") String unsubscribeURL
) {

}
