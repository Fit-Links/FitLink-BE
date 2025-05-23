package spring.fitlinkbe.support.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
@RequiredArgsConstructor
public class SnsConfig {

    private final AwsProperties awsProperties;

    @Bean
    public SnsClient snsClient() {
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(awsProperties.getAccessKey(), awsProperties.getSecretKey())
        );

        return SnsClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(credentialsProvider)
                .build();
    }
}

