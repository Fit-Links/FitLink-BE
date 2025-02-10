package spring.fitlinkbe.domain.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.auth.command.AuthCommand;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.member.Member;

import static spring.fitlinkbe.domain.common.exception.ErrorCode.MEMBER_DETAIL_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class PersonalDetailService {

    private final PersonalDetailRepository personalDetailRepository;

    public PersonalDetail registerMember(Long personalDetailId, AuthCommand.MemberRegisterRequest so, Member savedMember) {
        PersonalDetail personalDetail = personalDetailRepository.getById(personalDetailId);
        personalDetail.registerMember(so.name(), so.birthDate(), so.phoneNumber(), so.profileUrl(), so.gender(), savedMember);
        personalDetailRepository.savePersonalDetail(personalDetail);

        return personalDetail;
    }

    public PersonalDetail getMemberDetail(Long memberId) {
        return personalDetailRepository.getMemberDetail(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_DETAIL_NOT_FOUND,
                        "멤버 상세 정보를 찾을 수 없습니다. [memberId: %d]".formatted(memberId)));
    }
}
