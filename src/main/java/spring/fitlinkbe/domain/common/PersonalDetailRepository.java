package spring.fitlinkbe.domain.common;

import spring.fitlinkbe.domain.common.model.PersonalDetail;

import java.util.Optional;

public interface PersonalDetailRepository {

    Optional<PersonalDetail> savePersonalDetail(PersonalDetail personalDetail);

    PersonalDetail saveIfNotExist(PersonalDetail personalDetail);

    PersonalDetail getById(Long personalDetailId);

    Optional<PersonalDetail> getTrainerDetail(Long trainerId);

    Optional<PersonalDetail> getMemberDetail(Long memberId);
}
