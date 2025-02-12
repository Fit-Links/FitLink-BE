package spring.fitlinkbe.domain.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.auth.command.AuthCommand;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.model.PersonalDetail;

import java.util.List;

import static spring.fitlinkbe.domain.common.exception.ErrorCode.MEMBER_DETAIL_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final WorkoutScheduleRepository workoutScheduleRepository;
    private final PersonalDetailRepository personalDetailRepository;

    public PersonalDetail registerMember(Long personalDetailId, AuthCommand.MemberRegisterRequest command, Member savedMember) {
        PersonalDetail personalDetail = personalDetailRepository.getById(personalDetailId);
        personalDetail.registerMember(command.name(), command.birthDate(), command.phoneNumber(), command.profileUrl(), command.gender(), savedMember);
        personalDetailRepository.savePersonalDetail(personalDetail);

        return personalDetail;
    }

    public PersonalDetail getMemberDetail(Long memberId) {
        return personalDetailRepository.getMemberDetail(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_DETAIL_NOT_FOUND,
                        "멤버 상세 정보를 찾을 수 없습니다. [memberId: %d]".formatted(memberId)));
    }

    public Member saveMember(Member member) {
        return memberRepository.saveMember(member).orElseThrow();
    }

    public void saveWorkoutSchedules(List<WorkoutSchedule> workoutSchedules) {
        workoutScheduleRepository.saveAll(workoutSchedules);
    }
}
