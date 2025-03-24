package spring.fitlinkbe.domain.member;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * @param memberId 연결 확인할 멤버 ID
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
     */
    public ConnectingInfo getConnectingInfo(Long memberId) {
        return connectingInfoRepository.getConnectedInfo(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_CONNECTED_TRAINER));
    }

    /**
     * 해당 회원의 트레이너 연결 정보 조회 </br>
     * 요청 상태거나 연결된 상태만 조회
     *
     * @return 연결된 트레이너가 없을 경우 null 반환
     */
    public ConnectingInfo findConnectingInfo(Long memberId) {
        return connectingInfoRepository.getConnectedInfo(memberId).orElse(null);
    }

    public void saveConnectingInfo(ConnectingInfo connectingInfo) {
        connectingInfoRepository.save(connectingInfo);
    }

    /**
     * @return 연결된 트레이너가 없을 경우 null 반환
     */
    public SessionInfo findSessionInfo(Long trainerId, Long memberId) {
        return sessionInfoRepository.getSessionInfo(trainerId, memberId).orElse(null);
    }

    public SessionInfo getSessionInfo(Long trainerId, Long memberId) {
        return sessionInfoRepository.getSessionInfo(trainerId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));
    }

    public SessionInfo getSessionInfo(Long sessionInfoId) {
        return sessionInfoRepository.getSessionInfo(sessionInfoId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));
    }

    public void savePersonalDetail(PersonalDetail personalDetail) {
        personalDetailRepository.savePersonalDetail(personalDetail);
    }

    public void deleteAllWorkoutSchedules(List<WorkoutSchedule> deletedWorkoutSchedules) {
        workoutScheduleRepository.deleteAllByIds(deletedWorkoutSchedules.stream()
                .map(WorkoutSchedule::getWorkoutScheduleId).toList());
    }

    /**
     * 멤버와 트레이너가 연결되어 있는지 확인
     *
     * @throws CustomException 연결되어 있지 않을 경우
     */
    public void checkConnected(Long trainerId, Long memberId) {
        Optional<ConnectingInfo> connectingInfo = connectingInfoRepository.findConnectingInfo(trainerId, memberId);
        if (connectingInfo.isEmpty() || !connectingInfo.get().isConnected()) {
            throw new CustomException(ErrorCode.MEMBER_NOT_CONNECTED_TRAINER, "트레이너가 멤버와 연결되어 있지 않습니다.");
        }
    }

    /**
     * 연결된 정보 조회
     *
     * @return 해당 회원과 연동 완료된 (Connected) 상태의 연결 정보
     */
    public ConnectingInfo findConnectedInfo(Long memberId) {
        return connectingInfoRepository.getConnectedInfo(memberId).orElse(null);
    }

    public void saveSessionInfo(SessionInfo sessionInfo) {
        sessionInfoRepository.saveSessionInfo(sessionInfo);
    }

    /**
     * 해당 트레이너의 회원 목록 조회
     */
    public Page<Member> getMembers(Long trainerId, Pageable pageRequest, String keyword) {
        return memberRepository.getMembers(trainerId, pageRequest, keyword);
    }

    public List<SessionInfo> findAllSessionInfo(List<Long> memberIds, Long trainerId) {
        return sessionInfoRepository.findAllSessionInfo(memberIds, trainerId);
    }

    /**
     * 세션 차감
     */
    public void deductSession(Long trainerId, Long memberId) {
        SessionInfo getSessionInfo = this.getSessionInfo(trainerId, memberId);
        getSessionInfo.deductSession();
        this.saveSessionInfo(getSessionInfo);
    }

    /**
     * 세션 복구
     */
    public void restoreSession(Long trainerId, Long memberId) {
        SessionInfo getSessionInfo = this.getSessionInfo(trainerId, memberId);
        getSessionInfo.restoreSession();
        this.saveSessionInfo(getSessionInfo);
    }

}
