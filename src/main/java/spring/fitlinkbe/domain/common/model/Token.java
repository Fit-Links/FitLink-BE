package spring.fitlinkbe.domain.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Token {

    private Long tokenId;

    private Long personalDetailId;

    private String refreshToken;

    private LocalTime createdAt;

    private LocalTime updatedAt;
}
