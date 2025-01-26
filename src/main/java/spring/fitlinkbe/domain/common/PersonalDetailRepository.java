package spring.fitlinkbe.domain.common;

import spring.fitlinkbe.domain.common.model.PersonalDetail;

public interface PersonalDetailRepository {
    PersonalDetail saveIfNotExist(PersonalDetail personalDetail);
}
