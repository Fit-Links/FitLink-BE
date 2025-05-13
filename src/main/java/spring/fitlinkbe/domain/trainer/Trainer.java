package spring.fitlinkbe.domain.trainer;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import spring.fitlinkbe.domain.common.model.PhoneNumber;

import java.time.LocalDateTime;

// 최대한 순수 POJO 를 위해서는 custom import를 자제해야 하지만, lombok 정도는 허용해줘야 편하다.
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Trainer {

    private Long trainerId;

    private String name;

    private PhoneNumber phoneNumber;

    private String profilePictureUrl;

    private String trainerCode;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static int CODE_SIZE = 6;

    public Trainer(String trainerCode, String name) {
        this.trainerCode = trainerCode;
        this.name = name;
    }

    public String getPhoneNumber() {
        if (phoneNumber == null) {
            return null;
        }
        return phoneNumber.getPhoneNumber();
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateProfile(String uploadFilePath) {
        this.profilePictureUrl = uploadFilePath;
    }
}
