package spring.fitlinkbe.interfaces.controller.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.fitlinkbe.domain.auth.AuthService;
import spring.fitlinkbe.domain.auth.so.AuthSo;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.interfaces.controller.auth.dto.AuthDto;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;
import spring.fitlinkbe.support.argumentresolver.Login;
import spring.fitlinkbe.support.security.SecurityUser;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Member 등록 API </br>
     * 소셜 로그인 진행 후 REQUIRED_SMS 상태의 Member 를 휴대폰 인증 및 회원가입 진행
     */
    @PostMapping("/members/register")
    public ApiResultResponse<Object> registerMember(@Login SecurityUser user,
                                                    @RequestBody AuthDto.MemberRegisterRequest requestBody) {
        checkUserStatusOrThrow(user);
        AuthSo.Response result = authService.registerMember(user.getPersonalDetailId(), requestBody.toSo());

        return ApiResultResponse.ok(AuthDto.Response.from(result));
    }

    private void checkUserStatusOrThrow(SecurityUser user) {
        if (user.getStatus() != PersonalDetail.Status.REQUIRED_SMS) {
            throw new CustomException(ErrorCode.NEED_REQUIRED_SMS_STATUS);
        }
    }

}
