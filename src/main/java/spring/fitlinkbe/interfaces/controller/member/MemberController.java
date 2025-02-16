package spring.fitlinkbe.interfaces.controller.member;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.fitlinkbe.application.member.MemberFacade;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;
import spring.fitlinkbe.interfaces.controller.member.dto.MemberDto;
import spring.fitlinkbe.support.argumentresolver.Login;
import spring.fitlinkbe.support.security.SecurityUser;

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

}
