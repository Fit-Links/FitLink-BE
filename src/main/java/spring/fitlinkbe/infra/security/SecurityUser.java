package spring.fitlinkbe.infra.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.PersonalDetail.Status;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
}
