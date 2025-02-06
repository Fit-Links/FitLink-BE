package spring.fitlinkbe.domain.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.auth.so.AuthSo;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.TokenRepository;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.Token;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.MemberRepository;
import spring.fitlinkbe.domain.member.WorkoutScheduleRepository;
import spring.fitlinkbe.support.security.AuthTokenProvider;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PersonalDetailRepository personalDetailRepository;
    private final WorkoutScheduleRepository workoutScheduleRepository;
    private final AuthTokenProvider authTokenProvider;
    private final TokenRepository tokenRepository;

    public AuthSo.Response registerMember(Long personalDetailId, AuthSo.MemberRegisterRequest so) {
        Member savedMember = memberRepository.saveMember(so.toMember()).orElseThrow();

        // personalDetail 업데이트
        PersonalDetail personalDetail = personalDetailRepository.getById(personalDetailId);
        personalDetail.registerMember(so.name(), so.birthDate(), so.phoneNumber(), so.profileUrl(), so.gender(), savedMember);
        personalDetailRepository.savePersonalDetail(personalDetail);

        // workoutSchedule 업데이트
        workoutScheduleRepository.saveAll(so.toWorkoutSchedules(savedMember));

        // 토큰 생성 또는 업데이트
        String accessToken = authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetailId);
        String refreshToken = authTokenProvider.createRefreshToken(personalDetailId);

        Token token = Token.builder()
                .personalDetailId(personalDetailId)
                .refreshToken(refreshToken)
                .build();
        tokenRepository.saveOrUpdate(token);

        return AuthSo.Response.of(accessToken, refreshToken);
    }
}
