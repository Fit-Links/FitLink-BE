package spring.fitlinkbe.domain.auth.command;

import lombok.Builder;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.PhoneNumber;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.WorkoutSchedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AuthCommand {

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

        /**
         * member 등록 요청을 Member 로 변환 </br>
         * isRequest, isConnected 는 false 로 초기화
         *
         * @return Member
         */
        public Member toMember() {
            return Member.builder()
                    .name(name)
                    .birthDate(birthDate)
                    .phoneNumber(phoneNumber)
                    .isRequest(false)
                    .isConnected(false)
                    .build();
        }

        public List<WorkoutSchedule> toWorkoutSchedules(Member member) {
            return workoutSchedule.stream()
                    .map(workoutScheduleRequest -> workoutScheduleRequest.toWorkoutSchedule(member))
                    .toList();
        }
    }

    @Builder
    public record WorkoutScheduleRequest(DayOfWeek dayOfWeek, List<LocalTime> preferenceTimes) {
        public WorkoutSchedule toWorkoutSchedule(Member member) {
            return WorkoutSchedule.builder()
                    .member(member)
                    .dayOfWeek(dayOfWeek)
                    .preferenceTimes(preferenceTimes)
                    .build();
        }
    }
}
