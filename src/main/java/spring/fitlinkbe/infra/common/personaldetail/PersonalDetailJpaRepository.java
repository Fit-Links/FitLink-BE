package spring.fitlinkbe.infra.common.personaldetail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import spring.fitlinkbe.domain.common.model.PersonalDetail.OauthProvider;

import java.util.Optional;

public interface PersonalDetailJpaRepository extends JpaRepository<PersonalDetailEntity, Long> {


    Optional<PersonalDetailEntity> findByOauthProviderAndProviderId(OauthProvider oauthProvider, String providerId);

    @Query("SELECT pd FROM PersonalDetailEntity pd " +
            "LEFT JOIN FETCH pd.trainer " +
            "LEFT JOIN FETCH pd.member " +
            "WHERE pd.personalDetailId = :personalDetailId")
    Optional<PersonalDetailEntity> findByIdJoinFetch(Long personalDetailId);

    @Query("SELECT pd FROM PersonalDetailEntity pd JOIN FETCH pd.trainer t WHERE t.trainerId = :trainerId")
    Optional<PersonalDetailEntity> findByTrainerId(Long trainerId);

    @Query("SELECT pd FROM PersonalDetailEntity pd JOIN FETCH pd.member t WHERE t.memberId = :memberId")
    Optional<PersonalDetailEntity> findByMemberId(Long memberId);
}
