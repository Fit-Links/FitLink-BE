package spring.fitlinkbe.infra.trainer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.trainer.AvailableTime;
import spring.fitlinkbe.domain.trainer.AvailableTimeRepository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AvailableTimeRepositoryImpl implements AvailableTimeRepository {

    private final AvailableTimeJpaRepository availableTimeJpaRepository;

    @Override
    public LocalDate getCurrentAppliedDate(Long trainerId) {
        return availableTimeJpaRepository.getCurrentAppliedDate(trainerId);
    }

    @Override
    public LocalDate getScheduledAppliedDate(Long trainerId) {
        return availableTimeJpaRepository.getScheduledAppliedDate(trainerId);
    }

    @Override
    public List<AvailableTime> getAvailableTimes(Long trainerId, LocalDate appliedDate) {
        return availableTimeJpaRepository.findAllAvailableTimes(trainerId, appliedDate)
                .stream().map(AvailableTimeEntity::toDomain).toList();
    }
}
