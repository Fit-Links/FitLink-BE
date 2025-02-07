package spring.fitlinkbe.domain.member;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.PersonalDetailService;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.model.PersonalDetail;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static spring.fitlinkbe.domain.common.exception.ErrorCode.MEMBER_DETAIL_NOT_FOUND;

class MemberServiceTest {

    @Mock
    private PersonalDetailRepository personalDetailRepository;

    @InjectMocks
    private PersonalDetailService personalDetailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("멤버의 상세 정보를 반환한다.")
    void getMemberDetail() {
        //given
        PersonalDetail memberDetail = PersonalDetail.builder()
                .memberId(1L)
                .personalDetailId(1L)
                .name("홍길동")
                .build();

        when(personalDetailRepository.getMemberDetail(memberDetail.getMemberId()))
                .thenReturn(Optional.of(memberDetail));

        //when
        PersonalDetail result = personalDetailService.getMemberDetail(memberDetail.getMemberId());

        //then
        Assertions.assertThat(result.getMemberId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("멤버의 상세 정보가 없으면 PERSONAL_DETAIL_NOT_FOUND 예외를 반환한다..")
    void getMemberDetailWithNotFound() {
        //given
        Long memberId = 1L;

        when(personalDetailRepository.getMemberDetail(any(Long.class))).thenThrow(
                new CustomException(MEMBER_DETAIL_NOT_FOUND,
                        MEMBER_DETAIL_NOT_FOUND.getMsg()));

        //when & then
        assertThatThrownBy(() -> personalDetailService.getMemberDetail(memberId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MEMBER_DETAIL_NOT_FOUND);
    }

}