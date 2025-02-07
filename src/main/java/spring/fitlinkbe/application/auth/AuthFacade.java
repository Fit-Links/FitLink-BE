package spring.fitlinkbe.application.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.auth.AuthService;
import spring.fitlinkbe.domain.auth.so.AuthSo;
import spring.fitlinkbe.domain.common.PersonalDetailService;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.Token;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.MemberService;
import spring.fitlinkbe.support.security.AuthTokenProvider;

@Component
@RequiredArgsConstructor
@Transactional
public class AuthFacade {

    private final MemberService memberService;
    private final PersonalDetailService personalDetailService;
    private final AuthService authService;
    private final AuthTokenProvider authTokenProvider;

    public AuthSo.Response registerMember(Long personalDetailId, AuthSo.MemberRegisterRequest so) {
        Member savedMember = memberService.saveMember(so.toMember());

        // personalDetail 업데이트
        PersonalDetail personalDetail = personalDetailService.registerMember(personalDetailId, so, savedMember);

        // 토큰 생성 또는 업데이트
        String accessToken = authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetailId);
        String refreshToken = authTokenProvider.createRefreshToken(personalDetailId);

        // workoutSchedule 업데이트
        memberService.saveWorkoutSchedules(so.toWorkoutSchedules(savedMember));

        Token token = Token.builder()
                .personalDetailId(personalDetailId)
                .refreshToken(refreshToken)
                .build();
        authService.saveOrUpdateToken(token);

        return AuthSo.Response.of(accessToken, refreshToken);
    }


}
