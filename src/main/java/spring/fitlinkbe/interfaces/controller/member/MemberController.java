package spring.fitlinkbe.interfaces.controller.member;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import spring.fitlinkbe.application.member.MemberFacade;
import spring.fitlinkbe.application.member.criteria.MemberInfoResult;
import spring.fitlinkbe.application.member.criteria.MemberSessionResult;
import spring.fitlinkbe.application.member.criteria.WorkoutScheduleResult;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.reservation.Session;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;
import spring.fitlinkbe.interfaces.controller.common.dto.CustomPageResponse;
import spring.fitlinkbe.interfaces.controller.member.dto.MemberDto;
import spring.fitlinkbe.interfaces.controller.member.dto.MemberInfoDto;
import spring.fitlinkbe.interfaces.controller.member.dto.MemberSessionDto;
import spring.fitlinkbe.interfaces.controller.member.dto.WorkoutScheduleDto;
import spring.fitlinkbe.support.argumentresolver.Login;
import spring.fitlinkbe.support.security.SecurityUser;
import spring.fitlinkbe.support.validator.CollectionValidator;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/members")
public class MemberController {

    private final MemberFacade memberFacade;
    private final CollectionValidator validator;

    @PostMapping("/connect")
    public ApiResultResponse<Object> connectTrainer(
            @Login SecurityUser user,
            @RequestBody @Valid MemberDto.MemberConnectRequest requestBody) {
        memberFacade.connectTrainer(user.getMemberId(), requestBody.trainerCode());

        return ApiResultResponse.ok(null);
    }

    @PostMapping("/disconnect")
    public ApiResultResponse<Object> disconnectTrainer(@Login SecurityUser user) {
        memberFacade.disconnectTrainer(user.getMemberId());

        return ApiResultResponse.ok(null);
    }

    @GetMapping("/me")
    public ApiResultResponse<MemberInfoDto.Response> getMyInfo(@Login SecurityUser user) {
        MemberInfoResult.Response result = memberFacade.getMyInfo(user.getMemberId());

        return ApiResultResponse.ok(MemberInfoDto.Response.from(result));
    }

    @PatchMapping("/me")
    public ApiResultResponse<MemberInfoDto.MemberUpdateResponse> updateMyInfo(
            @Login SecurityUser user,
            @RequestBody @Valid MemberInfoDto.MemberUpdateRequest requestBody) {
        MemberInfoResult.MemberUpdateResponse result = memberFacade
                .updateMyInfo(user.getMemberId(), requestBody.name(), requestBody.phoneNumber());

        return ApiResultResponse.ok(MemberInfoDto.MemberUpdateResponse.from(result));
    }

    @GetMapping("/me/detail")
    public ApiResultResponse<MemberInfoDto.DetailResponse> getMyDetail(@Login SecurityUser user) {
        MemberInfoResult.DetailResponse result = memberFacade.getMyDetail(user.getMemberId());

        return ApiResultResponse.ok(MemberInfoDto.DetailResponse.from(result));
    }

    @PutMapping("/me/workout-schedule")
    public ApiResultResponse<Object> updateWorkoutSchedule(
            @Login SecurityUser user,
            @RequestBody @Valid List<WorkoutScheduleDto.Request> requestBody
    ) {
        if (!isWorkoutScheduleDayOfWeekUnique(requestBody)) {
            throw new CustomException(ErrorCode.DUPLICATED_WORKOUT_SCHEDULE);
        }

        List<WorkoutScheduleResult.Response> result = memberFacade.updateWorkoutSchedule(
                user.getMemberId(),
                requestBody.stream().map(WorkoutScheduleDto.Request::toCriteria).toList()
        );

        return ApiResultResponse.ok(result.stream()
                .map(WorkoutScheduleDto.Response::from)
                .toList());
    }

    /**
     * 요일이 중복되는지 확인
     */
    private boolean isWorkoutScheduleDayOfWeekUnique(List<WorkoutScheduleDto.Request> workoutSchedule) {
        if (workoutSchedule == null) {
            return true;
        }
        return workoutSchedule.stream()
                .map(WorkoutScheduleDto.Request::dayOfWeek)
                .distinct()
                .count() == workoutSchedule.size();
    }

    @GetMapping("/me/workout-schedule")
    public ApiResultResponse<List<WorkoutScheduleDto.Response>> getWorkoutSchedule(@Login SecurityUser user) {
        List<WorkoutScheduleResult.Response> result = memberFacade.getWorkoutSchedule(user.getMemberId());

        return ApiResultResponse.ok(result.stream()
                .map(WorkoutScheduleDto.Response::from)
                .toList());
    }

    @GetMapping("/me/sessions")
    public ApiResultResponse<CustomPageResponse<MemberSessionDto.SessionResponse>> getSessions(
            @Login SecurityUser user,
            @PageableDefault(size = 5) Pageable pageRequest,
            @RequestParam(required = false) Session.Status status
    ) {
        Page<MemberSessionResult.SessionResponse> result = memberFacade.getSessions(user.getMemberId(), status, pageRequest);

        return ApiResultResponse.ok(CustomPageResponse.of(result, MemberSessionDto.SessionResponse::from));
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        Object target = dataBinder.getTarget();
        if (target instanceof List) { // List 타입에 대해서만 validator 등록
            dataBinder.addValidators(validator);
        }
    }
}
