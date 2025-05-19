package spring.fitlinkbe.interfaces.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import spring.fitlinkbe.domain.attachment.AttachmentService;

@Component
@RequiredArgsConstructor
public class AttachmentScheduler {

    private final AttachmentService attachmentService;

    @Scheduled(cron = "0 0 0 * * *") // 매일 00:00:00에 실행
    public void cleanAttachments() {
        attachmentService.deletePendingAttachments();
    }
}
