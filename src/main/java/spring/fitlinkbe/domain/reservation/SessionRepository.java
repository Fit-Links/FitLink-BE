package spring.fitlinkbe.domain.reservation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SessionRepository {
    Page<Session> getSessions(Long memberId, Session.Status status, Pageable pageRequest);
}
