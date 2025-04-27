package spring.fitlinkbe.domain.trainer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.common.ConnectingInfoRepository;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.common.model.ConnectingInfo;
import spring.fitlinkbe.domain.common.model.PersonalDetail;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static spring.fitlinkbe.domain.common.exception.ErrorCode.TRAINER_IS_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final PersonalDetailRepository personalDetailRepository;
    private final AvailableTimeRepository availableTimeRepository;
    private final ConnectingInfoRepository connectingInfoRepository;

    @Transactional(readOnly = true)
    public Trainer getTrainerInfo(Long trainerId) {

        return trainerRepository.getTrainerInfo(trainerId)
                .orElseThrow(() -> new CustomException(TRAINER_IS_NOT_FOUND,
                        "트레이너 정보가 존재하지 않습니다. [trainerID: %d]".formatted(trainerId)));
    }

    public Trainer saveTrainer(Trainer trainer) {
        return trainerRepository.saveTrainer(trainer).orElseThrow();
    }

    public void saveAvailableTimes(List<AvailableTime> availableTimes) {
        trainerRepository.saveAvailableTimes(availableTimes);
    }

    public Trainer getTrainerByCode(String trainerCode) {
        return trainerRepository.getTrainerByCode(trainerCode);
    }

    public PersonalDetail getTrainerDetail(Long trainerId) {
        return personalDetailRepository.getTrainerDetail(trainerId)
                .orElseThrow(() -> new CustomException(TRAINER_IS_NOT_FOUND,
                        "트레이너 상세 정보를 찾을 수 없습니다. [trainerId: %d]".formatted(trainerId)));
    }

    public void savePersonalDetail(PersonalDetail personalDetail) {
        personalDetailRepository.savePersonalDetail(personalDetail);
    }

    public List<AvailableTime> getCurrentAvailableTimes(Long trainerId) {
        LocalDate currentAppliedDate = availableTimeRepository.getCurrentAppliedDate(trainerId);
        if (currentAppliedDate == null) {
            return Collections.emptyList();
        }

        return availableTimeRepository.getAvailableTimes(trainerId, currentAppliedDate);
    }

    public List<AvailableTime> getScheduledAvailableTimes(Long trainerId) {
        LocalDate scheduledAppliedDate = availableTimeRepository.getScheduledAppliedDate(trainerId);
        if (scheduledAppliedDate == null) {
            return Collections.emptyList();
        }

        return availableTimeRepository.getAvailableTimes(trainerId, scheduledAppliedDate);
    }


    public List<AvailableTime> getAvailableTimes(Long trainerId, LocalDate applyAt) {
        return availableTimeRepository.getAvailableTimes(trainerId, applyAt);
    }

    public void deleteAvailableTimes(List<AvailableTime> availableTimes) {
        availableTimeRepository.deleteAll(availableTimes);
    }

    public void checkDayOffDuplicatedOrThrow(Long trainerId, List<LocalDate> dayOffDates) {
        if (trainerRepository.isDayOffExists(trainerId, dayOffDates)) {
            throw new CustomException(ErrorCode.DAY_OFF_DUPLICATED);
        }
    }

    public List<DayOff> saveAllDayOffs(List<DayOff> dayOffs) {
        return trainerRepository.saveAllDayOffs(dayOffs);
    }

    public DayOff getDayOff(Long trainerId, Long dayOffId) {
        return trainerRepository.findDayOff(trainerId, dayOffId)
                .orElseThrow(() -> new CustomException(ErrorCode.DAY_OFF_NOT_FOUND));
    }

    @Transactional
    public void deleteDayOff(DayOff dayOff) {
        trainerRepository.deleteDayOff(dayOff);
    }

    /**
     * 트레이너의 휴무일을 조회한다.
     *
     * @return 휴무일 목록 (현재 날짜 이후의 휴무일만)
     */
    public List<DayOff> findAllDayOff(Long trainerId) {
        return trainerRepository.findScheduledDayOff(trainerId);
    }

    public ConnectingInfo getConnectingInfo(Long trainerId, Long memberId) {
        return connectingInfoRepository.getConnectingInfo(memberId, trainerId);
    }

    public void saveConnectingInfo(ConnectingInfo connectingInfo) {
        connectingInfoRepository.save(connectingInfo);
    }
}
