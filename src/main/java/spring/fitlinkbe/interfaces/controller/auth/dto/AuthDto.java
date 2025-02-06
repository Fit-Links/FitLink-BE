package spring.fitlinkbe.interfaces.controller.auth.dto;

import lombok.Builder;
import spring.fitlinkbe.domain.auth.so.AuthSo;
import spring.fitlinkbe.domain.common.model.PersonalDetail.Gender;
import spring.fitlinkbe.domain.common.model.PhoneNumber;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AuthDto {

    @Builder
    public record Response(String accessToken, String refreshToken) {

        public static Response from(AuthSo.Response so) {
            return Response.builder()
                    .accessToken(so.accessToken())
                    .refreshToken(so.refreshToken())
                    .build();
        }

    }

    public record MemberRegisterRequest(String name, LocalDate birthDate, String phoneNumber, Gender gender,
                                        String profileUrl, List<WorkoutScheduleRequest> workoutSchedule) {

        public AuthSo.MemberRegisterRequest toSo() {
            return AuthSo.MemberRegisterRequest.builder()
                    .name(name)
                    .birthDate(birthDate)
                    .phoneNumber(new PhoneNumber(phoneNumber))
                    .gender(gender)
                    .profileUrl(profileUrl)
                    .workoutSchedule(workoutSchedule.stream().map(WorkoutScheduleRequest::toSo).toList())
                    .build();
        }
    }

    public record WorkoutScheduleRequest(DayOfWeek dayOfWeek, List<LocalTime> preferenceTimes) {

        public AuthSo.WorkoutScheduleRequest toSo() {
            return AuthSo.WorkoutScheduleRequest.builder()
                    .dayOfWeek(dayOfWeek)
                    .preferenceTimes(new ArrayList<>(preferenceTimes))
                    .build();
        }
    }
}
