package spring.fitlinkbe.infra.member;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import spring.fitlinkbe.domain.common.model.ConnectingInfo;
import spring.fitlinkbe.infra.common.connectingInfo.QConnectingInfoEntity;

import java.util.List;

import static spring.fitlinkbe.infra.member.QMemberEntity.memberEntity;

@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MemberEntity> findAllMembers(Long trainerId, Pageable pageRequest, String keyword) {
        List<MemberEntity> members = queryFactory.selectFrom(memberEntity)
                .leftJoin(QConnectingInfoEntity.connectingInfoEntity)
                .on(memberEntity.memberId.eq(QConnectingInfoEntity.connectingInfoEntity.member.memberId))
                .where(
                        eqTrainerId(trainerId),
                        likeKeyword(keyword),
                        isConnected()
                )
                .orderBy(memberEntity.createdAt.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        Long totalCount = queryFactory.select(memberEntity.count())
                .from(memberEntity)
                .leftJoin(QConnectingInfoEntity.connectingInfoEntity)
                .on(
                        memberEntity.memberId
                                .eq(QConnectingInfoEntity.connectingInfoEntity.member.memberId)
                )
                .where(
                        eqTrainerId(trainerId),
                        likeKeyword(keyword),
                        isConnected()
                )
                .fetchOne();
        if (totalCount == null) {
            totalCount = 0L;
        }

        return new PageImpl<>(members, pageRequest, totalCount);
    }

    private BooleanExpression isConnected() {
        return QConnectingInfoEntity.connectingInfoEntity.status.eq(ConnectingInfo.ConnectingStatus.CONNECTED);
    }

    private BooleanExpression eqTrainerId(Long trainerId) {
        return trainerId != null ? QConnectingInfoEntity.connectingInfoEntity.trainer.trainerId.eq(trainerId) : null;
    }

    private BooleanExpression likeKeyword(String keyword) {
        return keyword != null ? memberEntity.name.contains(keyword) : null;
    }
}
