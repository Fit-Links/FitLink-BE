package spring.fitlinkbe.support.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
@Configuration
@ConfigurationProperties(prefix = "spring.cloud.aws")
@Getter
@Setter
public class AwsProperties {
    private Credentials credentials;
    private Region region;
    private S3 s3;

    public String getAccessKey() {
        return credentials != null ? credentials.getAccessKey() : null;
    }

    public String getSecretKey() {
        return credentials != null ? credentials.getSecretKey() : null;
    }

    public String getRegion() {
        return region != null ? region.getStaticValue() : null;
    }

    public String getBucketName() {
        return s3 != null ? s3.getBucketName() : null;
    }

    @Getter
    @Setter
    public static class Credentials {
        private String accessKey;
        private String secretKey;
    }

    @Getter
    @Setter
    public static class S3 {
        private String bucketName;
    }

    @Getter
    @Setter
    public static class Region {
        private String staticValue;

        public void setStatic(String staticValue) {
            this.staticValue = staticValue;
        }
    }
}
