package spring.fitlinkbe.domain.common.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.trainer.Trainer;

import java.time.LocalDateTime;


@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConnectingInfo {
    private Long connectingInfoId;
    private Trainer trainer;
    private Member member;
    private ConnectingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ConnectingStatus {
        REQUESTED, CONNECTED, REJECTED, DISCONNECTED
    }
}
