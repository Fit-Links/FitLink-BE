package spring.fitlinkbe.infra.common.personaldetail;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.common.model.PersonalDetail;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PersonalDetailRepositoryImpl implements PersonalDetailRepository {

    private final PersonalDetailJpaRepository personalDetailJpaRepository;

    @Override
    public Optional<PersonalDetail> savePersonalDetail(PersonalDetail personalDetail) {
        PersonalDetailEntity entity = personalDetailJpaRepository.save(PersonalDetailEntity.from(personalDetail));

        return Optional.of(entity.toDomain());

    }

    @Override
    public PersonalDetail saveIfNotExist(PersonalDetail personalDetail) {
        Optional<PersonalDetailEntity> optionalEntity = personalDetailJpaRepository
                .findByOauthProviderAndProviderId(personalDetail.getOauthProvider(), personalDetail.getProviderId());

        if (optionalEntity.isPresent()) {
            return optionalEntity.get().toDomain();
        } else {
            PersonalDetailEntity entity = personalDetailJpaRepository.save(PersonalDetailEntity.from(personalDetail));
            return entity.toDomain();
        }
    }

    @Override
    public PersonalDetail getById(Long personalDetailId) {
        return personalDetailJpaRepository.findByIdJoinFetch(personalDetailId)
                .orElseThrow(() -> new CustomException(ErrorCode.PERSONAL_DETAIL_NOT_FOUND))
                .toDomain();
    }

    @Override
    public Optional<PersonalDetail> getTrainerDetail(Long trainerId) {
        Optional<PersonalDetailEntity> findEntity = personalDetailJpaRepository.findByTrainerId(trainerId);
        if (findEntity.isPresent()) {
            return findEntity.map(PersonalDetailEntity::toDomain);
        }

        return Optional.empty();
    }


}
