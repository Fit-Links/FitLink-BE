package spring.fitlinkbe.domain.common.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import spring.fitlinkbe.domain.common.exception.CustomException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PhoneNumberUnitTest {

    @Nested
    @DisplayName("Phone 객체 생성 성공 테스트")
    class CreatePhone {
        @Test
        @DisplayName("Phone 객체 생성 성공 테스트 - 10자리")
        void createPhoneWith10() {
            // given
            String phoneNumber = "0101234567";

            // when
            PhoneNumber phone = new PhoneNumber(phoneNumber);

            // then
            assertThat(phone.getPhoneNumber()).isEqualTo(phoneNumber);
        }

        @Test
        @DisplayName("Phone 객체 생성 성공 테스트 - 11자리")
        void createPhoneWith11() {
            // given
            String phoneNumber = "01012345678";

            // when
            PhoneNumber phone = new PhoneNumber(phoneNumber);

            // then
            assertThat(phone.getPhoneNumber()).isEqualTo(phoneNumber);
        }

        @Test
        @DisplayName("Phone 객체 생성 성공 테스트 - null")
        void createPhoneWithNull() {
            // given
            String phoneNumber = null;

            // when
            PhoneNumber phone = new PhoneNumber(phoneNumber);

            // then
            assertThat(phone.getPhoneNumber()).isEqualTo(phoneNumber);
        }
    }

    @Nested
    @DisplayName("Phone 객체 생성 실패 테스트")
    class CreatePhoneFail {
        @Test
        @DisplayName("Phone 객체 생성 실패 테스트 - 빈 문자열")
        void createPhoneWithEmpty() {
            // given
            String phoneNumber = "";

            // when & then
            assertThatThrownBy(() -> new PhoneNumber(phoneNumber))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        @DisplayName("Phone 객체 생성 실패 테스트 - 10자리 미만")
        void createPhoneWithLessThan10() {
            // given
            String phoneNumber = "010123456";

            // when & then
            assertThatThrownBy(() -> new PhoneNumber(phoneNumber))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        @DisplayName("Phone 객체 생성 실패 테스트 - 11자리 초과")
        void createPhoneWithMoreThan11() {
            // given
            String phoneNumber = "010123456789";

            // when & then
            assertThatThrownBy(() -> new PhoneNumber(phoneNumber))
                    .isInstanceOf(CustomException.class);
        }

        @Test
        @DisplayName("Phone 객체 생성 실패 테스트 - 숫자가 아닌 문자 포함")
        void createPhoneWithNonNumeric() {
            // given
            String phoneNumber = "0101234a678";

            // when & then
            assertThatThrownBy(() -> new PhoneNumber(phoneNumber))
                    .isInstanceOf(CustomException.class);
        }
    }
}
