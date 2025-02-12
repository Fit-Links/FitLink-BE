package spring.fitlinkbe.infra.trainer;

import jakarta.persistence.*;
import lombok.*;
import spring.fitlinkbe.domain.trainer.AvailableTime;
import spring.fitlinkbe.infra.common.model.BaseTimeEntity;

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

    private Boolean isHoliday;

    private Boolean unavailable;

    private LocalTime startTime;

    private LocalTime endTime;

    public static AvailableTimeEntity from(AvailableTime availableTime) {
        return AvailableTimeEntity.builder()
                .availableTimeId(availableTime.getAvailableTimeId())
                .trainer(TrainerEntity.from(availableTime.getTrainer()))
                .isHoliday(availableTime.getIsHoliday())
                .unavailable(availableTime.getUnavailable())
                .startTime(availableTime.getStartTime())
                .endTime(availableTime.getEndTime())
                .build();
    }

    public AvailableTime toDomain() {
        return AvailableTime.builder()
                .availableTimeId(availableTimeId)
                .trainer(trainer.toDomain())
                .isHoliday(isHoliday)
                .unavailable(unavailable)
                .startTime(startTime)
                .endTime(endTime)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }
}
