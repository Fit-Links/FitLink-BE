package spring.fitlinkbe.support.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Getter
@ConfigurationProperties(prefix = "spring.cloud.aws")
@RequiredArgsConstructor(onConstructor_ = @ConstructorBinding)
public class AwsProperties {

    private final Credentials credentials;
    private final Region region;
    private final S3 s3;
    private final Sqs sqs;

    public String getAccessKey() {
        return credentials != null ? credentials.getAccessKey() : null;
    }

    public String getSecretKey() {
        return credentials != null ? credentials.getSecretKey() : null;
    }

    public String getRegion() {
        return region != null ? region.getName() : null;
    }

    public String getBucketName() {
        return s3 != null ? s3.getBucketName() : null;
    }

    @Getter
    @RequiredArgsConstructor(onConstructor_ = @ConstructorBinding)
    public static class Credentials {
        private final String accessKey;
        private final String secretKey;
    }

    @Getter
    @RequiredArgsConstructor(onConstructor_ = @ConstructorBinding)
    public static class Region {
        private final String name;
    }

    @Getter
    @RequiredArgsConstructor(onConstructor_ = @ConstructorBinding)
    public static class S3 {
        private final String bucketName;
    }

    @Getter
    @RequiredArgsConstructor(onConstructor_ = @ConstructorBinding)
    public static class Sqs {
        private final String queueName;
    }
}
