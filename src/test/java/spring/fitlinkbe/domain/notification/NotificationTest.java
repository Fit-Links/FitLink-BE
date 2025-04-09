package spring.fitlinkbe.domain.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import spring.fitlinkbe.domain.common.model.PersonalDetail;

import static org.assertj.core.api.Assertions.assertThat;

public class NotificationTest {

    @Test
    @DisplayName("연결 알림 생성 - 성공")
    void connectRequest() {
        //given
        PersonalDetail personalDetail = PersonalDetail.builder()
                .personalDetailId(1L)
                .build();

        //when
        Notification result = Notification.connectRequest(personalDetail, 1L,
                "멤버1", 1L);

        //then
        assertThat(result).isInstanceOf(Notification.class);
        assertThat(result.getContent()).isEqualTo("멤버1 님에게 연동 요청이 왔습니다.");
    }

}
