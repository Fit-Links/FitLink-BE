package spring.fitlinkbe.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import spring.fitlinkbe.domain.attachment.AttachmentRepository;
import spring.fitlinkbe.domain.attachment.model.Attachment;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.MemberRepository;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.integration.common.BaseIntegrationTest;
import spring.fitlinkbe.integration.common.TestDataHandler;
import spring.fitlinkbe.interfaces.controller.attachment.dto.AttachmentDto;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;

public class AttachmentIntegrationTest extends BaseIntegrationTest {

    @Autowired
    TestDataHandler testDataHandler;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    AttachmentRepository attachmentRepository;

    @Autowired
    PersonalDetailRepository personalDetailRepository;

    @Nested
    @DisplayName("유저 프로필 업데이트 api 테스트")
    class UserProfileUpdateTest {

        private static final String url = "/v1/attachments/user-profile";

        @DisplayName("유저 프로필 업데이트 성공 - 회원일 때")
        @Test
        void userProfileUpdateTestSuccessMember() {
            // given
            // 멤버와 attachment 정보가 있을 때
            Member member = testDataHandler.createMember();
            PersonalDetail memberDetail = testDataHandler.getMemberPersonalDetail(member.getMemberId());
            String token = testDataHandler.createTokenFromMember(member);
            Attachment attachment = testDataHandler.createAttachment();

            // when
            // 회원 프로필 업데이트 api 호출시
            AttachmentDto.UserAttachmentAddDto reqDto = new AttachmentDto.UserAttachmentAddDto(attachment.getAttachmentId());
            String requestBody = writeValueAsString(reqDto);
            ExtractableResponse<Response> result = post(url, requestBody, token);

            // then
            // 프로필 업데이트 성공
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response.status()).isEqualTo(201);
                softly.assertThat(response.success()).isTrue();

                Member updatedMember = memberRepository.getMember(member.getMemberId()).orElseThrow();
                softly.assertThat(updatedMember.getProfilePictureUrl()).isEqualTo(attachment.getUploadFilePath());

                PersonalDetail updatedDetail = personalDetailRepository.getById(memberDetail.getPersonalDetailId());
                softly.assertThat(updatedDetail.getProfilePictureUrl()).isEqualTo(attachment.getUploadFilePath());

                Attachment updatedAttachment = attachmentRepository.findById(attachment.getAttachmentId()).orElseThrow();
                softly.assertThat(updatedAttachment.getPersonalDetailId()).isEqualTo(memberDetail.getPersonalDetailId());
            });
        }

        @Test
        @DisplayName("유저 프로필 업데이트 성공 - 트레이너일 때")
        void userProfileUpdateTestSuccessTrainer() {
            // given
            // 트레이너와 attachment 정보가 있을 때
            String trainerCode = "123sDSD";
            Trainer trainer = testDataHandler.createTrainer(trainerCode);
            PersonalDetail trainerDetail = testDataHandler.getTrainerPersonalDetail(trainer.getTrainerId());
            String token = testDataHandler.createTokenFromTrainer(trainer);
            Attachment attachment = testDataHandler.createAttachment();

            // when
            // 트레이너 프로필 업데이트 api 호출시
            AttachmentDto.UserAttachmentAddDto reqDto = new AttachmentDto.UserAttachmentAddDto(attachment.getAttachmentId());
            String requestBody = writeValueAsString(reqDto);
            ExtractableResponse<Response> result = post(url, requestBody, token);

            // then
            // 프로필 업데이트 성공
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response.status()).isEqualTo(201);
                softly.assertThat(response.success()).isTrue();

                PersonalDetail updatedDetail = personalDetailRepository.getById(trainerDetail.getPersonalDetailId());
                softly.assertThat(updatedDetail.getProfilePictureUrl()).isEqualTo(attachment.getUploadFilePath());

                Attachment updatedAttachment = attachmentRepository.findById(attachment.getAttachmentId()).orElseThrow();
                softly.assertThat(updatedAttachment.getPersonalDetailId()).isEqualTo(trainerDetail.getPersonalDetailId());
            });
        }
    }
}
