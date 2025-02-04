package spring.fitlinkbe.domain.common.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.trainer.Trainer;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfo {

    private Long SessionInfoId;
    private Member member;
    private Trainer trainer;
    private int totalCount;
    private int remainCount;
}
