package spring.fitlinkbe.application.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.auth.AuthService;
import spring.fitlinkbe.domain.auth.command.AuthCommand;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.Token;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.MemberService;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerService;
import spring.fitlinkbe.support.security.AuthTokenProvider;

import static spring.fitlinkbe.support.utils.RandomStringGenerator.generateRandomString;

@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final MemberService memberService;
    private final TrainerService trainerService;
    private final AuthService authService;
    private final AuthTokenProvider authTokenProvider;

    @Transactional
    public AuthCommand.Response registerTrainer(Long personalDetailId, AuthCommand.TrainerRegisterRequest command) {
        // trainer 저장
        Trainer savedTrainer = trainerService.saveTrainer(new Trainer(generateRandomString(6), command.name()));

        // personalDetail 업데이트
        PersonalDetail personalDetail = trainerService.registerTrainer(personalDetailId, command, savedTrainer);

        // 토큰 생성 또는 업데이트
        String accessToken = authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetailId,
                personalDetail.getUserRole());
        String refreshToken = authTokenProvider.createRefreshToken(personalDetailId, personalDetail.getUserRole());

        trainerService.saveAvailableTimes(command.toAvailableTimes(savedTrainer));

        Token token = Token.builder()
                .personalDetailId(personalDetailId)
                .refreshToken(refreshToken)
                .build();
        authService.saveOrUpdateToken(token);

        return AuthCommand.Response.of(accessToken, refreshToken);
    }

    @Transactional
    public AuthCommand.Response registerMember(Long personalDetailId, AuthCommand.MemberRegisterRequest command) {
        Member savedMember = memberService.saveMember(command.toMember());

        // personalDetail 업데이트
        PersonalDetail personalDetail = memberService.registerMember(personalDetailId, command, savedMember);

        // 토큰 생성 또는 업데이트
        String accessToken = authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetailId,
                personalDetail.getUserRole());
        String refreshToken = authTokenProvider.createRefreshToken(personalDetailId, personalDetail.getUserRole());

        // workoutSchedule 업데이트
        memberService.saveWorkoutSchedules(command.toWorkoutSchedules(savedMember));

        Token token = Token.builder()
                .personalDetailId(personalDetailId)
                .refreshToken(refreshToken)
                .build();
        authService.saveOrUpdateToken(token);

        return AuthCommand.Response.of(accessToken, refreshToken);
    }
}
