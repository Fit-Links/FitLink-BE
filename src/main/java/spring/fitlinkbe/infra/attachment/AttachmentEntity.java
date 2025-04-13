package spring.fitlinkbe.infra.attachment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import spring.fitlinkbe.domain.attachment.model.Attachment;
import spring.fitlinkbe.infra.common.personaldetail.PersonalDetailEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "attachment")
public class AttachmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attachmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_detail_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private PersonalDetailEntity personalDetail;

    private String origFileName;
    private String uuid;
    private String uploadFilePath;
    private long fileSize;
    private String fileExtension;

    @Column(name = "personal_detail_id", insertable = false, updatable = false)
    private Long personalDetailId;

    public static AttachmentEntity of(Attachment attachment, EntityManager em) {
        return AttachmentEntity.builder()
                .attachmentId(attachment.getAttachmentId())
                .personalDetail(attachment.getPersonalDetailId() != null ?
                        em.getReference(PersonalDetailEntity.class, attachment.getPersonalDetailId()) : null)
                .origFileName(attachment.getOrigFileName())
                .uuid(attachment.getUuid())
                .uploadFilePath(attachment.getUploadFilePath())
                .fileSize(attachment.getFileSize())
                .fileExtension(attachment.getFileExtension())
                .build();
    }

    public Attachment toDomain() {
        return Attachment.builder()
                .attachmentId(attachmentId)
                .personalDetailId(personalDetailId)
                .origFileName(origFileName)
                .uuid(uuid)
                .uploadFilePath(uploadFilePath)
                .fileSize(fileSize)
                .fileExtension(fileExtension)
                .build();
    }
}
