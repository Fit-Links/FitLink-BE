package spring.fitlinkbe.interfaces.controller.member;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import spring.fitlinkbe.application.member.MemberFacade;
import spring.fitlinkbe.application.member.criteria.MemberInfoResult;
import spring.fitlinkbe.application.member.criteria.WorkoutScheduleResult;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;
import spring.fitlinkbe.interfaces.controller.member.dto.MemberDto;
import spring.fitlinkbe.interfaces.controller.member.dto.MemberInfoDto;
import spring.fitlinkbe.interfaces.controller.member.dto.WorkoutScheduleDto;
import spring.fitlinkbe.support.argumentresolver.Login;
import spring.fitlinkbe.support.security.SecurityUser;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/members")
public class MemberController {

    private final MemberFacade memberFacade;

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

    @GetMapping("/workout-schedule")
    public ApiResultResponse<List<WorkoutScheduleDto.Response>> getWorkoutSchedule(@Login SecurityUser user) {
        List<WorkoutScheduleResult.Response> result = memberFacade.getWorkoutSchedule(user.getMemberId());

        return ApiResultResponse.ok(result.stream()
                .map(WorkoutScheduleDto.Response::from)
                .toList());
    }
}
