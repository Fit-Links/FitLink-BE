package spring.fitlinkbe.domain.trainer;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import spring.fitlinkbe.domain.common.model.SessionInfo;

import java.time.LocalDateTime;

// 최대한 순수 POJO 를 위해서는 custom import를 자제해야 하지만, lombok 정도는 허용해줘야 편하다.
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Trainer {

    private Long trainerId;

    private String name;

    private Long sessionInfoId;

    private SessionInfo sessionInfo;

    private String trainerCode;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Trainer(String trainerCode, String name) {
        this.trainerCode = trainerCode;
        this.name = name;
    }

    public void updateName(String name) {
        this.name = name;
    }
}
