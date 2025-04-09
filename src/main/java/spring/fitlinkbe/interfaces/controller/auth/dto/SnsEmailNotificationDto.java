package spring.fitlinkbe.interfaces.controller.auth.dto;


import java.util.List;

public record SnsEmailNotificationDto(
        String notificationType,
        Mail mail,
        Receipt receipt,
        String content
) {
    public record Mail(
            String timestamp,
            String source,
            String messageId,
            List<String> destination,
            boolean headersTruncated,
            List<Header> headers,
            CommonHeaders commonHeaders
    ) {
    }

    public record Header(
            String name,
            String value
    ) {
    }

    public record CommonHeaders(
            String returnPath,
            List<String> from,
            String date,
            List<String> to,
            String messageId,
            String subject
    ) {
    }

    public record Receipt(
            String timestamp,
            int processingTimeMillis,
            List<String> recipients,
            Verdict spamVerdict,
            Verdict virusVerdict,
            Verdict spfVerdict,
            Verdict dkimVerdict,
            Verdict dmarcVerdict,
            Action action
    ) {
    }

    public record Verdict(
            String status
    ) {
    }

    public record Action(
            String type,
            String topicArn,
            String encoding
    ) {
    }
}

