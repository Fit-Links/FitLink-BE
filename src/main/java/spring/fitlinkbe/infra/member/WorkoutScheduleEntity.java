package spring.fitlinkbe.infra.member;

import jakarta.persistence.*;
import lombok.*;
import spring.fitlinkbe.domain.member.WorkoutSchedule;
import spring.fitlinkbe.support.converter.LocalTimeListConverter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "workout_schedule")
public class WorkoutScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workoutScheduleId;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private MemberEntity member;

    @Convert(converter = LocalTimeListConverter.class)
    @Column(columnDefinition = "varchar(255)")
    private List<LocalTime> preferenceTimes;

    public static WorkoutScheduleEntity from(WorkoutSchedule workoutSchedule) {

        return WorkoutScheduleEntity.builder()
                .workoutScheduleId(workoutSchedule.getWorkoutScheduleId())
                .dayOfWeek(workoutSchedule.getDayOfWeek())
                .member(MemberEntity.from(workoutSchedule.getMember()))
                .preferenceTimes(workoutSchedule.getPreferenceTimes())
                .build();
    }

    public WorkoutSchedule toDomain() {
        return WorkoutSchedule.builder()
                .workoutScheduleId(workoutScheduleId)
                .dayOfWeek(dayOfWeek)
                .member(member.toDomain())
                .preferenceTimes(preferenceTimes)
                .build();
    }
}
