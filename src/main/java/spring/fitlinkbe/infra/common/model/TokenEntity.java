package spring.fitlinkbe.infra.common.model;

import jakarta.persistence.*;
import lombok.*;
import spring.fitlinkbe.domain.common.model.Token;

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
    private PersonalDetailEntity personalDetail;

    private String refreshToken;


    public Token toDomain() {
        return Token.builder()
                .tokenId(tokenId)
                .personalDetailId(personalDetail.getPersonalDetailId())
                .refreshToken(refreshToken)
                .build();
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
