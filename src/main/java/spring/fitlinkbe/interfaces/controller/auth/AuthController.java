package spring.fitlinkbe.interfaces.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import spring.fitlinkbe.application.auth.AuthFacade;
import spring.fitlinkbe.domain.auth.command.AuthCommand;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.interfaces.controller.auth.dto.AuthDto;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;
import spring.fitlinkbe.support.aop.RoleCheck;
import spring.fitlinkbe.support.argumentresolver.Login;
import spring.fitlinkbe.support.security.SecurityUser;

@RestController
@RequiredArgsConstructor
@RoleCheck(allowedRoles = {UserRole.TRAINER, UserRole.MEMBER})
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthFacade authFacade;

    /**
     * Trainer 등록 API </br>
     * sns 인증 진행 후 REQUIRED_REGISTER 상태의 Trainer 를 회원가입 진행
     */
    @PostMapping("/trainers/register")
    public ApiResultResponse<Object> registerTrainer(@Login SecurityUser user,
                                                     @RequestBody @Valid AuthDto.TrainerRegisterRequest requestBody) {
        user.checkUserStatusOrThrow(PersonalDetail.Status.REQUIRED_REGISTER);
        AuthCommand.Response result = authFacade.registerTrainer(user.getPersonalDetailId(), requestBody.toCommand());

        return ApiResultResponse.ok(AuthDto.Response.from(result));
    }

    /**
     * Member 등록 API </br>
     * sns 인증 진행 후 REQUIRED_REGISTER 상태의 Member 를 회원가입 진행
     */
    @PostMapping("/members/register")
    public ApiResultResponse<Object> registerMember(@Login SecurityUser user,
                                                    @RequestBody @Valid AuthDto.MemberRegisterRequest requestBody) {
        user.checkUserStatusOrThrow(PersonalDetail.Status.REQUIRED_REGISTER);
        AuthCommand.Response result = authFacade.registerMember(user.getPersonalDetailId(), requestBody.toCommand());

        return ApiResultResponse.ok(AuthDto.Response.from(result));
    }

    /**
     * sns 인증을 위한 Token 발급 API
     */
    @GetMapping("/email-verification-token")
    public ApiResultResponse<AuthDto.EmailAuthTokenResponse> getEmailVerificationToken(
            @Login SecurityUser user
    ) {
        String verificationToken = authFacade.getEmailVerificationToken(user.getPersonalDetailId());

        return ApiResultResponse.ok(new AuthDto.EmailAuthTokenResponse(verificationToken));
    }

    /**
     * 유저의 상태 polling api
     */
    @GetMapping("/status")
    public ApiResultResponse<AuthDto.UserStatusResponse> getUserStatus(@Login SecurityUser user) {
        PersonalDetail.Status status = user.getStatus();
        String accessToken = authFacade.createAccessToken(user.getPersonalDetailId(), user.getUserRole(), status);

        return ApiResultResponse.ok(new AuthDto.UserStatusResponse(status, accessToken));
    }

    @PostMapping("/access-token")
    public ApiResultResponse<AuthDto.AccessTokenResponse> getAccessToken(
            @RequestBody @Valid AuthDto.AccessTokenRequest requestBody
    ) {
        String accessToken = authFacade.renewAccessToken(requestBody.refreshToken());

        return ApiResultResponse.ok(new AuthDto.AccessTokenResponse(accessToken));
    }

}
