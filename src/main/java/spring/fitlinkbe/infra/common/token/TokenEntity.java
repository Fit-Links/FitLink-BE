package spring.fitlinkbe.infra.common.token;

import jakarta.persistence.*;
import lombok.*;
import spring.fitlinkbe.domain.common.model.Token;
import spring.fitlinkbe.infra.common.model.BaseTimeEntity;
import spring.fitlinkbe.infra.common.personaldetail.PersonalDetailEntity;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "token")
public class TokenEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_detail_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private PersonalDetailEntity personalDetail;

    private String refreshToken;


    public Token toDomain() {
        return Token.builder()
                .tokenId(tokenId)
                .personalDetailId(personalDetail.getPersonalDetailId())
                .refreshToken(refreshToken)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
