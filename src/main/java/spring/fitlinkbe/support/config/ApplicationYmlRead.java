package spring.fitlinkbe.support.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@Setter
@ConfigurationProperties(prefix = "app")
public class ApplicationYmlRead {
    private Map<String, String> front;

    @Getter
    private CorsProperties cors;

    public String getFrontUrl() {
        return front.get("url");
    }

    @Getter
    @Setter
    public static class CorsProperties {
        private String allowedOrigins;
        private String allowedMethods;
        private String allowedHeaders;
        private Long maxAge;
    }
}
