package spring.fitlinkbe.support.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    /**
     * LocalDateTime 형식을 String 형식으로 변환한다.
     *
     * @param localDateTime localDateTime 정보
     * @return String String 타입
     */
    public static String getLocalDateTimeToString(LocalDateTime localDateTime) {

        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * 받은 날짜로부터 2주뒤의 날짜 정보를 받는다.
     *
     * @return LocalDateTime
     */
    public static LocalDateTime getTwoWeekAfterDate(LocalDateTime receivedDate) {

        return receivedDate.plusWeeks(2).minusSeconds(1);
    }

    /**
     * 받은 날짜로부터 한달 뒤의 날짜 정보를 받는다.
     *
     * @return LocalDateTime
     */
    public static LocalDateTime getOneMonthAfterDate(LocalDateTime receivedDate) {

        return receivedDate.plusMonths(1).minusSeconds(1);
    }
}
