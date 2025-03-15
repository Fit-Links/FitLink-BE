package spring.fitlinkbe.infra.reservation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import spring.fitlinkbe.domain.reservation.Session;

public interface SessionRepositoryCustom {
    Page<SessionEntity> findSessions(Long memberId, Long trainerId, Session.Status status, Pageable pageRequest);
}
