package spring.fitlinkbe.infra.member;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.MemberRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Optional<Member> saveMember(Member member) {
        MemberEntity memberEntity = memberJpaRepository.save(MemberEntity.from(member));

        return Optional.of(memberEntity.toDomain());
    }

    @Override
    public Optional<Member> getMember(Long memberId) {
        Optional<MemberEntity> findEntity = memberJpaRepository.findByIdJoinFetch(memberId);

        if (findEntity.isPresent()) {
            return findEntity.map(MemberEntity::toDomain);
        }

        return Optional.empty();
    }

    @Override
    public List<Member> getMembers() {
        return memberJpaRepository.findAll().stream().map(MemberEntity::toDomain).toList();
    }

    @Override
    public Page<Member> getMembers(Long trainerId, Pageable pageRequest, String keyword) {
        return memberJpaRepository.findAllMembers(trainerId, pageRequest, keyword)
                .map(MemberEntity::toDomain);
    }

    @Override
    public boolean exists(Long memberId) {
        return memberJpaRepository.existsById(memberId);
    }
}
