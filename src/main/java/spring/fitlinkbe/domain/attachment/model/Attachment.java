package spring.fitlinkbe.domain.attachment.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class Attachment {
    private Long attachmentId;
    private Long personalDetailId;
    private String origFileName;
    private String uuid;
    private String uploadFilePath;
    private long fileSize;
    private String fileExtension;

    @Builder.Default
    private Boolean isUploaded = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void updatePersonalDetailId(Long personalDetailId) {
        this.personalDetailId = personalDetailId;
        this.isUploaded = true;
    }
}
