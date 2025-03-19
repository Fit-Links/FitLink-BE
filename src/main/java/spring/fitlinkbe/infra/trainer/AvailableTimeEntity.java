package spring.fitlinkbe.infra.trainer;

import jakarta.persistence.*;
import lombok.*;
import spring.fitlinkbe.domain.trainer.AvailableTime;
import spring.fitlinkbe.infra.common.model.BaseTimeEntity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "available_time")
public class AvailableTimeEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long availableTimeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private TrainerEntity trainer;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private LocalDate applyAt;

    private Boolean isHoliday;

    private Boolean unavailable;

    private LocalTime startTime;

    private LocalTime endTime;

    public static AvailableTimeEntity from(AvailableTime availableTime) {
        return AvailableTimeEntity.builder()
                .availableTimeId(availableTime.getAvailableTimeId())
                .trainer(TrainerEntity.from(availableTime.getTrainer()))
                .dayOfWeek(availableTime.getDayOfWeek())
                .isHoliday(availableTime.getIsHoliday())
                .applyAt(availableTime.getApplyAt())
                .unavailable(availableTime.getUnavailable())
                .startTime(availableTime.getStartTime())
                .endTime(availableTime.getEndTime())
                .build();
    }

    public AvailableTime toDomain() {
        return AvailableTime.builder()
                .availableTimeId(availableTimeId)
                .trainer(trainer.toDomain())
                .dayOfWeek(dayOfWeek)
                .isHoliday(isHoliday)
                .unavailable(unavailable)
                .startTime(startTime)
                .endTime(endTime)
                .applyAt(applyAt)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }
}
