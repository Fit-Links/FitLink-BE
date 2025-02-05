package spring.fitlinkbe.domain.auth.so;

import lombok.Builder;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.PhoneNumber;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AuthSo {

    @Builder
    public record Response(String accessToken, String refreshToken) {

        public static Response of(String accessToken, String refreshToken) {
            return Response.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        }
    }

    @Builder
    public record MemberRegisterRequest(String name, LocalDate birthDate, PhoneNumber phoneNumber,
                                        PersonalDetail.Gender gender,
                                        String profileUrl, List<WorkoutScheduleRequest> workoutSchedule) {
    }

    @Builder
    public record WorkoutScheduleRequest(DayOfWeek dayOfWeek, List<LocalTime> preferenceTimes) {
    }
}
