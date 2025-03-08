package spring.fitlinkbe.domain.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.auth.command.AuthCommand;
import spring.fitlinkbe.domain.common.ConnectingInfoRepository;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.SessionInfoRepository;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.common.model.ConnectingInfo;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.SessionInfo;
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
    private final SessionInfoRepository sessionInfoRepository;

    public PersonalDetail registerMember(Long personalDetailId, AuthCommand.MemberRegisterRequest command, Member savedMember) {
        PersonalDetail personalDetail = personalDetailRepository.getById(personalDetailId);
        personalDetail.registerMember(command.name(), command.birthDate(), command.phoneNumber(), command.profileUrl(), command.gender(), savedMember);
        personalDetailRepository.savePersonalDetail(personalDetail);

        return personalDetail;
    }

    @Transactional(readOnly = true)
    public List<WorkoutSchedule> getWorkoutSchedules(Long memberId) {
        return workoutScheduleRepository.findAllByMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public PersonalDetail getMemberDetail(Long memberId) {
        return personalDetailRepository.getMemberDetail(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_DETAIL_NOT_FOUND,
                        "멤버 상세 정보를 찾을 수 없습니다. [memberId: %d]".formatted(memberId)));
    }

    public Member saveMember(Member member) {
        return memberRepository.saveMember(member).orElseThrow();
    }

    public List<WorkoutSchedule> saveWorkoutSchedules(List<WorkoutSchedule> workoutSchedules) {
        return workoutScheduleRepository.saveAll(workoutSchedules);
    }

    /**
     * 이미 연결된, 연결 시도중인 트레이너가 있는지 확인
     *
     * @param memberId
     * @throws CustomException 이미 연결된, 연결 시도중인 트레이너가 있을 경우
     */
    public void checkMemberAlreadyConnected(Long memberId) {
        Optional<ConnectingInfo> existsConnectingInfo = connectingInfoRepository.getConnectedInfo(memberId);
        if (existsConnectingInfo.isPresent()) {
            throw new CustomException(ErrorCode.CONNECT_AVAILABLE_AFTER_DISCONNECTED);
        }
    }

    @Transactional(readOnly = true)
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
     *
     * @param memberId
     * @return
     */
    public ConnectingInfo getConnectedInfo(Long memberId) {
        return connectingInfoRepository.getConnectedInfo(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_CONNECTED_TRAINER));
    }

    public Optional<ConnectingInfo> findConnectedInfo(Long memberId) {
        return connectingInfoRepository.getConnectedInfo(memberId);
    }

    public void saveConnectingInfo(ConnectingInfo connectingInfo) {
        connectingInfoRepository.save(connectingInfo);
    }

    public Optional<SessionInfo> findSessionInfo(Long memberId) {
        return sessionInfoRepository.getSessionInfo(memberId);
    }

    public SessionInfo getSessionInfo(Long trainerId, Long memberId) {
        return sessionInfoRepository.getSessionInfo(trainerId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));
    }

    public void savePersonalDetail(PersonalDetail personalDetail) {
        personalDetailRepository.savePersonalDetail(personalDetail);
    }

    public void deleteAllWorkoutSchedules(List<WorkoutSchedule> deletedWorkoutSchedules) {
        workoutScheduleRepository.deleteAllByIds(deletedWorkoutSchedules.stream()
                .map(WorkoutSchedule::getWorkoutScheduleId).toList());
    }

    public void checkMemberExists(Long memberId) {
        if (!memberRepository.exists(memberId)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    /**
     * 해당 트레이너와 회원의 연결 정보 조회
     *
     * @param trainerId
     * @param memberId
     * @return
     */
    public Optional<ConnectingInfo> findConnectingInfo(Long trainerId, Long memberId) {
        return connectingInfoRepository.findConnectingInfo(trainerId, memberId);
    }
}
