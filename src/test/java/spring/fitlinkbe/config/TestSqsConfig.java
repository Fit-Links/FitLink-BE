package spring.fitlinkbe.config;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestSqsConfig {

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        SqsAsyncClient mockClient = mock(SqsAsyncClient.class);

        // getQueueUrl 호출에 대한 mock 설정
        GetQueueUrlResponse response = GetQueueUrlResponse.builder()
                .queueUrl("https://sqs/test-queue")
                .build();

        when(mockClient.getQueueUrl(any(GetQueueUrlRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        return mockClient;
    }

    @Bean
    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
        return SqsTemplate.newTemplate(sqsAsyncClient);
    }
}
