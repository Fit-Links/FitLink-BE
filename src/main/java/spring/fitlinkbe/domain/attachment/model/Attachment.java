package spring.fitlinkbe.domain.attachment.model;

import lombok.Builder;
import lombok.Getter;

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
}
