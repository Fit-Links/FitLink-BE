package spring.fitlinkbe.application.member;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.application.member.criteria.MemberInfoResult;
import spring.fitlinkbe.application.member.criteria.MemberSessionResult;
import spring.fitlinkbe.application.member.criteria.WorkoutScheduleCriteria;
import spring.fitlinkbe.application.member.criteria.WorkoutScheduleResult;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.common.model.ConnectingInfo;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.MemberService;
import spring.fitlinkbe.domain.member.WorkoutSchedule;
import spring.fitlinkbe.domain.notification.NotificationService;
import spring.fitlinkbe.domain.reservation.ReservationService;
import spring.fitlinkbe.domain.reservation.Session;
import spring.fitlinkbe.domain.reservation.command.ReservationCommand;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
        notificationService.sendConnectRequestNotification(trainerDetail, member.getName(), connectingInfo.getConnectingInfoId());
    }

    @Transactional
    public void disconnectTrainer(Long memberId) {
        ConnectingInfo connectingInfo = memberService.getConnectedInfo(memberId);
        if (connectingInfo.isPending()) {
            throw new CustomException(ErrorCode.DISCONNECT_AVAILABLE_AFTER_ACCEPTED);
        }

        Member member = memberService.getMember(memberId);
        PersonalDetail trainerDetail = trainerService.getTrainerDetail(connectingInfo.getTrainer().getTrainerId());
        notificationService.sendDisconnectNotification(member.getName(), trainerDetail);

        connectingInfo.disconnect();
        memberService.saveConnectingInfo(connectingInfo);
    }

    @Transactional(readOnly = true)
    public MemberInfoResult.Response getMyInfo(Long memberId) {
        Optional<ConnectingInfo> connectingInfo = memberService.findConnectedInfo(memberId);
        Optional<SessionInfo> sessionInfo = memberService.findSessionInfo(memberId);

        Member me = memberService.getMember(memberId);
        Trainer trainer = connectingInfo.map(ConnectingInfo::getTrainer).orElse(null);

        List<WorkoutSchedule> workoutSchedules = memberService.getWorkoutSchedules(memberId);

        return MemberInfoResult.Response.of(me, trainer, sessionInfo.orElse(null), workoutSchedules);
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
    public Page<MemberSessionResult.SessionResponse> getSessions(Long memberId, Session.Status status, Pageable pageRequest) {
        Page<Session> sessions = reservationService.getSessions(ReservationCommand.GetSessions.of(memberId, status, pageRequest));

        return sessions.map(MemberSessionResult.SessionResponse::from);
    }
}
