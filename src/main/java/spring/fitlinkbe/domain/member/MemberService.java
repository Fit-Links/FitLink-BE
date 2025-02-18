package spring.fitlinkbe.domain.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.auth.command.AuthCommand;
import spring.fitlinkbe.domain.common.ConnectingInfoRepository;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.common.model.ConnectingInfo;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.trainer.Trainer;

import java.util.List;
import java.util.Optional;

import static spring.fitlinkbe.domain.common.exception.ErrorCode.MEMBER_DETAIL_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final WorkoutScheduleRepository workoutScheduleRepository;
    private final PersonalDetailRepository personalDetailRepository;
    private final ConnectingInfoRepository connectingInfoRepository;

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

    /**
     * 멤버가 이미 연결되어 있는지 확인 </br>
     * 해당 회원의 이미 존재하는, REJECTED 되지 않은 ConnectingInfo 가 있는지 확인
     *
     * @param memberId
     * @throws CustomException 이미 연결되어 있을 경우
     */
    public void checkMemberAlreadyConnected(Long memberId) {
        Optional<ConnectingInfo> existsConnectingInfo = connectingInfoRepository.getConnectedInfo(memberId);
        if (existsConnectingInfo.isPresent()) {
            throw new CustomException(ErrorCode.CONNECT_AVAILABLE_AFTER_DISCONNECTED);
        }
    }

    public Member getMember(Long memberId) {
        return memberRepository.getMember(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public ConnectingInfo requestConnectTrainer(Trainer trainer, Member member) {
        ConnectingInfo connectingInfo = ConnectingInfo.builder()
                .member(member)
                .trainer(trainer)
                .status(ConnectingInfo.ConnectingStatus.REQUESTED)
                .build();

        return connectingInfoRepository.save(connectingInfo);
    }

    /**
     * 해당 회원의 트레이너 연결 정보 조회 </br>
     * 요청 상태거나 연결된 상태만 조회
     * @param memberId
     * @return
     */
    public ConnectingInfo getConnectedInfo(Long memberId) {
        return connectingInfoRepository.getConnectedInfo(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_CONNECTED_TRAINER));
    }

    public void saveConnectingInfo(ConnectingInfo connectingInfo) {
        connectingInfoRepository.save(connectingInfo);
    }
}
