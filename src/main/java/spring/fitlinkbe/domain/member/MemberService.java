package spring.fitlinkbe.domain.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.model.PersonalDetail;

import static spring.fitlinkbe.domain.common.exception.ErrorCode.MEMBER_DETAIL_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PersonalDetailRepository personalDetailRepository;


    public PersonalDetail getMemberDetail(Long memberId) {
        return personalDetailRepository.getMemberDetail(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_DETAIL_NOT_FOUND,
                        "멤버 상세 정보를 찾을 수 없습니다. [memberId: %d]".formatted(memberId)));
    }
}
