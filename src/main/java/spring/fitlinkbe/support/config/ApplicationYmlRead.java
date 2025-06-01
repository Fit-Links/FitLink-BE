package spring.fitlinkbe.support.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Setter
@ConfigurationProperties(prefix = "app")
public class ApplicationYmlRead {

    @Getter
    private CorsProperties cors;


    @Getter
    @Setter
    public static class CorsProperties {
        private String allowedOrigins;
        private String allowedMethods;
        private String allowedHeaders;
        private Long maxAge;
    }
}
