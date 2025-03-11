package spring.fitlinkbe.domain.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    Optional<Member> saveMember(Member member);

    Optional<Member> getMember(Long memberId);

    List<Member> getMembers();

    Page<Member> getMembers(Long trainerId, Pageable pageRequest, String keyword);

    boolean exists(Long memberId);
}
