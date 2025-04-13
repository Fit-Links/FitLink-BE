package spring.fitlinkbe.domain.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile({"test", "local"}) // 테스트나 로컬에서만 사용
public class FakeFcmService implements FcmService {
    @Override
    public void sendNotification(String fcmToken, String title, String body) {
        log.info("테스트 중이라 FCM 전송 생략: title:{}, body:{}", title, body);
    }
}