package spring.fitlinkbe.domain.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import spring.fitlinkbe.domain.common.model.PhoneNumber;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    private Long id;

    private Long trainerId;

    private String name;

    private LocalDate birthDate;

    private PhoneNumber phoneNumber;

    private Boolean isRequest;

    private Boolean isConnected;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public String getPhoneNumber() {
        if (phoneNumber == null) {
            return null;
        }
        return phoneNumber.getPhoneNumber();
    }
}
