package spring.fitlinkbe.interfaces.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import spring.fitlinkbe.domain.attachment.AttachmentRepository;
import spring.fitlinkbe.domain.attachment.model.Attachment;
import spring.fitlinkbe.integration.common.BaseIntegrationTest;
import spring.fitlinkbe.integration.common.TestDataHandler;
import spring.fitlinkbe.support.utils.ClockUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AttachmentSchedulerTest extends BaseIntegrationTest {

    @Autowired
    TestDataHandler testDataHandler;

    @Autowired
    AttachmentScheduler attachmentScheduler;

    @Autowired
    AttachmentRepository attachmentRepository;

    @Nested
    @DisplayName("미업로드 첨부파일 삭제 테스트")
    class DeletePendingAttachmentsTest {

        @Test
        @DisplayName("미업로드 첨부파일 삭제 - 10분 초과 경과")
        void deletePendingAttachments() {
            // given
            Attachment attachment1 = testDataHandler.createAttachment();
            Attachment attachment2 = testDataHandler.createAttachment();
            Attachment attachment3 = testDataHandler.createAttachment();

            // when
            // 10분이 지났을 때
            MockedStatic<ClockUtils> clockUtilsMockedStatic = Mockito.mockStatic(ClockUtils.class);
            clockUtilsMockedStatic.when(ClockUtils::localDateTimeNow).thenReturn(LocalDateTime.now().plusMinutes(11));

            attachmentScheduler.cleanAttachments();

            // then
            // 10분이 지났으므로, 첨부파일이 삭제되어야 한다.
            assertTrue(attachmentRepository.findById(attachment1.getAttachmentId()).isEmpty());
            assertTrue(attachmentRepository.findById(attachment2.getAttachmentId()).isEmpty());
            assertTrue(attachmentRepository.findById(attachment3.getAttachmentId()).isEmpty());

            clockUtilsMockedStatic.close();
        }

        @Test
        @DisplayName("미업로드 첨부파일 삭제 - 10분 미만 경과")
        void deletePendingAttachmentsLessThan10Minutes() {
            // given
            Attachment attachment1 = testDataHandler.createAttachment();
            Attachment attachment2 = testDataHandler.createAttachment();
            Attachment attachment3 = testDataHandler.createAttachment();

            // when
            // 10분이 지나지 않았을 때
            MockedStatic<ClockUtils> clockUtilsMockedStatic = Mockito.mockStatic(ClockUtils.class);
            clockUtilsMockedStatic.when(ClockUtils::localDateTimeNow).thenReturn(LocalDateTime.now().plusMinutes(9));

            attachmentScheduler.cleanAttachments();

            // then
            // 10분이 지나지 않았으므로, 첨부파일이 삭제되지 않아야 한다.
            assertTrue(attachmentRepository.findById(attachment1.getAttachmentId()).isPresent());
            assertTrue(attachmentRepository.findById(attachment2.getAttachmentId()).isPresent());
            assertTrue(attachmentRepository.findById(attachment3.getAttachmentId()).isPresent());

            clockUtilsMockedStatic.close();
        }
    }

}
