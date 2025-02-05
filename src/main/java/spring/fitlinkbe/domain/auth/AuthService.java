package spring.fitlinkbe.domain.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.auth.so.AuthSo;

@Service
@Transactional
public class AuthService {
    public AuthSo.Response registerMember(Long personalDetailId, AuthSo.MemberRegisterRequest so) {
        return null;
    }
}
