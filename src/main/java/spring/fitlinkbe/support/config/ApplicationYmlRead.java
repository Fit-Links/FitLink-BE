package spring.fitlinkbe.support.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@Setter
@ConfigurationProperties(prefix = "app")
public class ApplicationYmlRead {
    private Map<String, String> front;

    public String getFrontUrl() {
        return front.get("url");
    }

}
