package spring.fitlinkbe.infra.trainer;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import spring.fitlinkbe.domain.trainer.DayOff;
import spring.fitlinkbe.infra.common.model.BaseTimeEntity;

import java.time.LocalDate;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "day_off")
public class DayOffEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dayOffId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private TrainerEntity trainer;

    private LocalDate dayOffDate;

    public static DayOffEntity from(DayOff dayOff) {
        return DayOffEntity.builder()
                .dayOffId(dayOff.getDayOffId() != null ? dayOff.getDayOffId() : null)
                .dayOffDate(dayOff.getDayOffDate())
                .trainer(TrainerEntity.from(dayOff.getTrainer()))
                .build();
    }

    public DayOff toDomain() {
        return DayOff.builder()
                .dayOffId(dayOffId)
                .dayOffDate(dayOffDate)
                .trainer(Hibernate.isInitialized(trainer) ? trainer.toDomain() : null)
                .build();
    }
}
