package spring.fitlinkbe.domain.member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    Optional<Member> saveMember(Member member);

    Optional<Member> getMember(Long memberId);

    List<Member> getMembers();

    boolean exists(Long memberId);
}
