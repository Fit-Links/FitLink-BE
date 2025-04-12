package spring.fitlinkbe.application.member;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.application.member.criteria.*;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.common.model.ConnectingInfo;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.MemberService;
import spring.fitlinkbe.domain.member.WorkoutSchedule;
import spring.fitlinkbe.domain.notification.NotificationService;
import spring.fitlinkbe.domain.notification.command.NotificationCommand;
import spring.fitlinkbe.domain.reservation.ReservationService;
import spring.fitlinkbe.domain.reservation.Session;
import spring.fitlinkbe.domain.reservation.command.ReservationCommand;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MemberFacade {
    private final MemberService memberService;
    private final TrainerService trainerService;
    private final NotificationService notificationService;
    private final ReservationService reservationService;

    @Transactional
    public void connectTrainer(Long memberId, String trainerCode) {
        memberService.checkMemberAlreadyConnected(memberId);

        Trainer trainer = trainerService.getTrainerByCode(trainerCode);
        Member member = memberService.getMember(memberId);

        ConnectingInfo connectingInfo = memberService.requestConnectTrainer(trainer, member);
        PersonalDetail trainerDetail = trainerService.getTrainerDetail(trainer.getTrainerId());

        notificationService.sendNotification(NotificationCommand.Connect.of(trainerDetail, member.getMemberId(),
                member.getName(), connectingInfo.getConnectingInfoId()));
    }

    @Transactional
    public void disconnectTrainer(Long memberId) {
        ConnectingInfo connectingInfo = memberService.getConnectingInfo(memberId);

        Member member = memberService.getMember(memberId);
        PersonalDetail trainerDetail = trainerService.getTrainerDetail(connectingInfo.getTrainer().getTrainerId());
        // -> 트레이너에게 알림 보내기
        notificationService.sendNotification(NotificationCommand.Disconnect.of(trainerDetail, member.getMemberId(),
                member.getName(), UserRole.TRAINER));

        connectingInfo.disconnect();
        memberService.saveConnectingInfo(connectingInfo);
    }

    @Transactional(readOnly = true)
    public MemberInfoResult.Response getMyInfo(Long memberId) {
        ConnectingInfo connectingInfo = memberService.findConnectingInfo(memberId);
        SessionInfo sessionInfo = connectingInfo != null ? memberService
                .findSessionInfo(connectingInfo.getTrainerId(), memberId) : null;

        Member me = memberService.getMember(memberId);

        List<WorkoutSchedule> workoutSchedules = memberService.getWorkoutSchedules(memberId);

        return MemberInfoResult.Response.of(me, connectingInfo, sessionInfo, workoutSchedules);
    }

    @Transactional
    public MemberInfoResult.MemberUpdateResponse updateMyInfo(Long memberId, String name, String phoneNumber) {
        Member me = memberService.getMember(memberId);
        me.update(name, phoneNumber);

        PersonalDetail personalDetail = memberService.getMemberDetail(memberId);
        personalDetail.update(name, phoneNumber);

        memberService.saveMember(me);
        memberService.savePersonalDetail(personalDetail);

        return MemberInfoResult.MemberUpdateResponse.from(me);
    }

    public MemberInfoResult.DetailResponse getMyDetail(Long memberId) {
        Member me = memberService.getMember(memberId);
        return MemberInfoResult.DetailResponse.from(me);
    }

    @Transactional
    public List<WorkoutScheduleResult.Response> updateWorkoutSchedule(
            Long memberId,
            List<WorkoutScheduleCriteria.Request> request
    ) {
        Member member = memberService.getMember(memberId);
        List<WorkoutSchedule> workoutSchedules = new ArrayList<>(memberService.getWorkoutSchedules(memberId));
        List<Long> requestWorkoutScheduleIds = request.stream()
                .map(WorkoutScheduleCriteria.Request::workoutScheduleId).toList();

        deleteNotContainedSchedules(workoutSchedules, requestWorkoutScheduleIds);

        updateExistingWorkoutSchedules(request, workoutSchedules);

        addNewWorkOutSchedules(request, member, workoutSchedules);

        List<WorkoutSchedule> result = memberService.saveWorkoutSchedules(workoutSchedules);
        return result.stream().map(WorkoutScheduleResult.Response::from)
                .sorted(Comparator.comparing(WorkoutScheduleResult.Response::dayOfWeek)).toList();
    }

    private void addNewWorkOutSchedules(List<WorkoutScheduleCriteria.Request> request, Member member, List<WorkoutSchedule> workoutSchedules) {
        List<WorkoutSchedule> newWorkoutSchedules = request.stream()
                .filter(criteria -> criteria.workoutScheduleId() == null)
                .map(criteria -> criteria.toDomain(member))
                .toList();
        workoutSchedules.addAll(newWorkoutSchedules);
    }

    private void updateExistingWorkoutSchedules(List<WorkoutScheduleCriteria.Request> request, List<WorkoutSchedule> workoutSchedules) {
        request.stream()
                .filter(criteria -> criteria.workoutScheduleId() != null)
                .forEach(criteria -> {
                    WorkoutSchedule workoutSchedule = workoutSchedules.stream()
                            .filter(ws -> ws.getWorkoutScheduleId().equals(criteria.workoutScheduleId()))
                            .findFirst()
                            .orElseThrow(() -> new CustomException(ErrorCode.WORKOUT_SCHEDULE_NOT_FOUND));
                    workoutSchedule.update(criteria.dayOfWeek(), criteria.preferenceTimes());
                });
    }

    private void deleteNotContainedSchedules(List<WorkoutSchedule> workoutSchedules, List<Long> requestWorkoutScheduleIds) {
        List<WorkoutSchedule> deletedWorkoutSchedules = workoutSchedules.stream()
                .filter(workoutSchedule -> !requestWorkoutScheduleIds.contains(workoutSchedule.getWorkoutScheduleId()))
                .toList();
        memberService.deleteAllWorkoutSchedules(deletedWorkoutSchedules);
        workoutSchedules.removeAll(deletedWorkoutSchedules);
    }

    @Transactional(readOnly = true)
    public Page<MemberSessionResult.SessionResponse> getMySessions(Long memberId, Session.Status status, Pageable pageRequest) {
        ReservationCommand.GetSessions command = ReservationCommand.GetSessions.builder()
                .memberId(memberId)
                .status(status)
                .pageRequest(pageRequest)
                .build();
        Page<Session> sessions = reservationService.getSessions(command);

        return sessions.map(MemberSessionResult.SessionResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<MemberSessionResult.SessionResponse> getSessions(Long trainerId, Long memberId, Session.Status status, Pageable pageRequest) {
        memberService.checkConnected(trainerId, memberId);

        ReservationCommand.GetSessions command = ReservationCommand.GetSessions.builder()
                .memberId(memberId)
                .trainerId(trainerId)
                .status(status)
                .pageRequest(pageRequest)
                .build();
        Page<Session> sessions = reservationService.getSessions(command);
        return sessions.map(MemberSessionResult.SessionResponse::from);
    }

    @Transactional
    public SessionInfoCriteria.Response updateSessionInfo(Long trainerId, Long memberId,
                                                          Long sessionInfoId, SessionInfoCriteria.UpdateRequest request) {
        memberService.checkConnected(trainerId, memberId);
        SessionInfo sessionInfo = memberService.getSessionInfo(sessionInfoId);

        int beforeTotalCnt = sessionInfo.getTotalCount();
        int beforeRemainingCnt = sessionInfo.getRemainingCount();
        int afterTotalCnt = request.totalCount() == null ? sessionInfo.getTotalCount() : request.totalCount();
        int afterRemainingCnt = request.remainingCount() == null ? sessionInfo.getRemainingCount() : request.remainingCount();

        request.patch(sessionInfo);
        memberService.saveSessionInfo(sessionInfo);

        PersonalDetail memberDetail = memberService.getMemberDetail(memberId);
        // 트레이너 -> 멤버에게 세션 직접 수정했다는 알림 전송
        notificationService.sendNotification(NotificationCommand.EditSession.of(memberDetail, sessionInfoId, trainerId,
                beforeTotalCnt, afterTotalCnt, beforeRemainingCnt, afterRemainingCnt));

        return SessionInfoCriteria.Response.from(sessionInfo);
    }

    @Transactional(readOnly = true)
    public Page<MemberInfoResult.SimpleResponse> getMembers(Long trainerId, Pageable pageRequest, String keyword) {
        Page<Member> memberPage = memberService.getMembers(trainerId, pageRequest, keyword);

        List<Long> memberIds = memberPage.getContent().stream().map(Member::getMemberId).toList();
        List<SessionInfo> sessions = memberService.findAllSessionInfo(memberIds, trainerId);
        Map<Long, SessionInfo> grouped = sessions.stream()
                .collect(Collectors.toMap(SessionInfo::getMemberId, Function.identity()));

        return memberPage.map(member -> MemberInfoResult.SimpleResponse.of(member, grouped.get(member.getMemberId())));
    }

    @Transactional(readOnly = true)
    public MemberInfoResult.Response getMemberInfo(Long trainerId, Long memberId) {
        memberService.checkConnected(trainerId, memberId);

        ConnectingInfo connectingInfo = memberService.findConnectedInfo(memberId);
        SessionInfo sessionInfo = memberService.findSessionInfo(trainerId, memberId);
        Member me = memberService.getMember(memberId);

        List<WorkoutSchedule> workoutSchedules = memberService.getWorkoutSchedules(memberId);

        return MemberInfoResult.Response.of(me, connectingInfo, sessionInfo, workoutSchedules);
    }
}
