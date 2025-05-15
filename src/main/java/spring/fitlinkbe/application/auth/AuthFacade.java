package spring.fitlinkbe.application.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.attachment.AttachmentService;
import spring.fitlinkbe.domain.attachment.model.Attachment;
import spring.fitlinkbe.domain.auth.AuthService;
import spring.fitlinkbe.domain.auth.command.AuthCommand;
import spring.fitlinkbe.domain.common.enums.UserRole;
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
    private final AttachmentService attachmentService;

    @Transactional
    public AuthCommand.Response registerTrainer(Long personalDetailId, AuthCommand.TrainerRegisterRequest command) {
        PersonalDetail personalDetail = authService.getPersonalDetailById(personalDetailId);
        Attachment attachment = attachmentService.findAttachment(command.attachmentId(), personalDetailId);

        String profileUrl = attachment != null ? attachment.getUploadFilePath() : null;
        Trainer newTrainer = Trainer.builder()
                .trainerCode(generateRandomString(Trainer.CODE_SIZE))
                .name(command.name())
                .phoneNumber(new PhoneNumber(personalDetail.getPhoneNumber()))
                .profilePictureUrl(profileUrl)
                .build();
        Trainer savedTrainer = trainerService.saveTrainer(newTrainer);
        personalDetail.registerTrainer(command.name(), command.birthDate(), profileUrl, command.gender(), savedTrainer);

        authService.savePersonalDetail(personalDetail);
        trainerService.saveAvailableTimes(command.toAvailableTimes(savedTrainer));

        return createAndReturnToken(personalDetail);
    }

    private AuthCommand.Response createAndReturnToken(PersonalDetail personalDetail) {
        String accessToken = authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetail.getPersonalDetailId(),
                personalDetail.getUserRole());
        String refreshToken = authTokenProvider.createRefreshToken(personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

        Token token = Token.builder()
                .personalDetailId(personalDetail.getPersonalDetailId())
                .refreshToken(refreshToken)
                .build();
        authService.saveOrUpdateToken(token);

        return AuthCommand.Response.of(accessToken, refreshToken);
    }

    @Transactional
    public AuthCommand.Response registerMember(Long personalDetailId, AuthCommand.MemberRegisterRequest command) {
        PersonalDetail personalDetail = authService.getPersonalDetailById(personalDetailId);

        Attachment attachment = attachmentService.findAttachment(command.attachmentId(), personalDetailId);
        String profileUrl = attachment != null ? attachment.getUploadFilePath() : null;

        Member member = memberService.saveMember(command.toMember(personalDetail.getPhoneNumber(), profileUrl));
        personalDetail.registerMember(command.name(), command.birthDate(), profileUrl, command.gender(), member);
        memberService.savePersonalDetail(personalDetail);

        // workoutSchedule 업데이트
        memberService.saveWorkoutSchedules(command.toWorkoutSchedules(member));

        return createAndReturnToken(personalDetail);
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

    public String createAccessToken(Long personalDetailId, UserRole userRole, PersonalDetail.Status status) {
        return authTokenProvider.createAccessToken(status, personalDetailId, userRole);
    }

    public String renewAccessToken(String refreshToken) {
        Long personalDetailId = authTokenProvider.getPersonalDetailIdFromRefreshToken(refreshToken);
        PersonalDetail personalDetail = authService.getPersonalDetailById(personalDetailId);

        return authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetailId, personalDetail.getUserRole());
    }
}
