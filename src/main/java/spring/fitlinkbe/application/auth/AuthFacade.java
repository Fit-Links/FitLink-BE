package spring.fitlinkbe.application.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.auth.AuthService;
import spring.fitlinkbe.domain.auth.command.AuthCommand;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.PhoneNumber;
import spring.fitlinkbe.domain.common.model.Token;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.MemberService;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerService;
import spring.fitlinkbe.interfaces.controller.auth.dto.SnsEmailNotificationDto;
import spring.fitlinkbe.support.parser.EmailParser;
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
        PersonalDetail personalDetail = memberService.getPersonalDetail(personalDetailId);
        Member member = memberService.saveMember(command.toMember(personalDetail.getPhoneNumber()));
        personalDetail.registerMember(command.name(), command.birthDate(), command.profileUrl(), command.gender(), member);

        // 토큰 생성 또는 업데이트
        String accessToken = authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetailId,
                personalDetail.getUserRole());
        String refreshToken = authTokenProvider.createRefreshToken(personalDetailId, personalDetail.getUserRole());

        // workoutSchedule 업데이트
        memberService.saveWorkoutSchedules(command.toWorkoutSchedules(member));

        Token token = Token.builder()
                .personalDetailId(personalDetailId)
                .refreshToken(refreshToken)
                .build();
        authService.saveOrUpdateToken(token);

        return AuthCommand.Response.of(accessToken, refreshToken);
    }

    public String getEmailVerificationToken(Long personalDetailId) {
        return authService.createEmailVerificationToken(personalDetailId);
    }

    public void verifySnsEmail(SnsEmailNotificationDto dto) {
        String token = EmailParser.parseEmailContent(dto.content());
        PhoneNumber phoneNumber = new PhoneNumber(EmailParser.extractPhoneNumber(dto.mail().source()));

        PersonalDetail personalDetail = authService.getPersonalDetailByToken(token);
        personalDetail.verifySnsEmail(phoneNumber);

        authService.savePersonalDetail(personalDetail);
    }
}
