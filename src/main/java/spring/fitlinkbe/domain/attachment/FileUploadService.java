package spring.fitlinkbe.domain.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.support.config.AwsProperties;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final AwsProperties awsProperties;

    private final Set<String> validContentTypes = Set.of("image/jpeg", "image/png", "image/jpg");

    public String getPresignedUrl(String uploadFileName, String contentLength, String contentType) {
        validateContentTypeAndContentLength(contentType, contentLength);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(awsProperties.getBucketName())
                .key(uploadFileName)
                .contentLength(Long.parseLong(contentLength))
                .contentType(contentType)
                .build();

        PutObjectPresignRequest putObjectPresignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5)) // 5 minute
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedPutObjectRequest =
                s3Presigner.presignPutObject(putObjectPresignRequest);
        s3Presigner.close();

        return presignedPutObjectRequest.url().toString();
    }

    private void validateContentTypeAndContentLength(String contentType, String contentLength) {
        if (!validContentTypes.contains(contentType)) {
            throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
        }

        int length = Integer.parseInt(contentLength);
        int MAX_UPLOAD_SIZE_MB = 10; // 10MB

        if (length > MAX_UPLOAD_SIZE_MB * 1024 * 1024) {
            throw new CustomException(ErrorCode.INVALID_CONTENT_LENGTH);
        }
    }

    public String getUploadUrl(String uploadFileName) {
        return s3Client.utilities().getUrl(b -> b.bucket(awsProperties.getBucketName())
                .key(uploadFileName)).toString();
    }
}
