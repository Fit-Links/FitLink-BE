package spring.fitlinkbe.support.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.PersonalDetail.Status;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static spring.fitlinkbe.domain.common.enums.UserRole.MEMBER;
import static spring.fitlinkbe.domain.common.enums.UserRole.TRAINER;

@Getter
public class SecurityUser implements OAuth2User {
    private final String name;
    private final Long trainerId;
    private final Long memberId;
    private final Long personalDetailId;
    private final Status status;

    public SecurityUser(PersonalDetail personalDetail) {
        this.name = personalDetail.getName();
        this.trainerId = personalDetail.getTrainerId();
        this.memberId = personalDetail.getMemberId();
        this.personalDetailId = personalDetail.getPersonalDetailId();
        this.status = personalDetail.getStatus();
    }

    @Override
    public <A> A getAttribute(String name) {
        return OAuth2User.super.getAttribute(name);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getName() {
        if (name == null || name.isEmpty()) {
            return "unknown";
        }
        return name;
    }

    public UserRole getUserRole() {
        return trainerId == null ? MEMBER : TRAINER;
    }

    public Long getUserId() {
        return trainerId == null ? memberId : trainerId;
    }

    public void checkUserStatusOrThrow(Status status) {
        if (this.status != status) {
            throw new CustomException(ErrorCode.USER_STATUS_NOT_ALLOWED,
                    "사용자의 상태가 %s 가 아닙니다. [userId: %d, status: %s]".formatted(status, personalDetailId, this.status));
        }
    }
}
