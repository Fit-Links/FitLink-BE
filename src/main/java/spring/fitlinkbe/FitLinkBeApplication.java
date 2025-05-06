package spring.fitlinkbe;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import spring.fitlinkbe.support.config.AwsProperties;

import java.util.TimeZone;

@SpringBootApplication
@EnableConfigurationProperties(value = {AwsProperties.class})
public class FitLinkBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitLinkBeApplication.class, args);
    }

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
