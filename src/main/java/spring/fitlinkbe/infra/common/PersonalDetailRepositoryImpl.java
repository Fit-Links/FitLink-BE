package spring.fitlinkbe.infra.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.infra.common.model.PersonalDetailEntity;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PersonalDetailRepositoryImpl implements PersonalDetailRepository {

    private final PersonalDetailJpaRepository personalDetailJpaRepository;

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
}
