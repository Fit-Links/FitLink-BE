package spring.fitlinkbe.infra.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.reservation.Session;
import spring.fitlinkbe.domain.reservation.SessionRepository;


@Repository
@RequiredArgsConstructor
public class SessionRepositoryImpl implements SessionRepository {

    private final SessionJpaRepository sessionJpaRepository;

    @Override
    public Page<Session> getSessions(Long memberId, Long trainerId, Session.Status status, Pageable pageRequest) {
        Page<SessionEntity> result = sessionJpaRepository.findSessions(memberId, trainerId, status, pageRequest);

        return result.map(SessionEntity::toDomain);
    }
}
