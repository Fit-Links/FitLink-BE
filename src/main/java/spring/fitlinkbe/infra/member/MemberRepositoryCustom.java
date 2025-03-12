package spring.fitlinkbe.infra.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {

    /**
     * 해당 트레이너의 회원 목록을 가져온다.
     */
    Page<MemberEntity> findAllMembers(Long trainerId, Pageable pageRequest, String keyword);
}
