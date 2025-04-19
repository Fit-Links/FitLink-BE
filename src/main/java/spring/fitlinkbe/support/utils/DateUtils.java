package spring.fitlinkbe.support.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtils {

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

    /**
     * 알림에서 받는 시간 보기 좋게 포맷팅
     * ex) 1.1 (월) 오후 2시
     *
     * @return LocalDateTime
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        // 월/일과 시간 추출
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM.dd (E) a h시", Locale.KOREAN);
        String formattedDate = dateTime.format(dateFormatter);

        // 오전/오후에 맞춰서 시간을 12시간제로 변경
        int hour = dateTime.getHour();
        String period = (hour >= 12) ? "오후" : "오전";
        int adjustedHour = (hour % 12 == 0) ? 12 : hour % 12; // 12시를 제외한 시간 조정
        formattedDate = formattedDate.replaceFirst("a", period).replaceFirst("h", String.valueOf(adjustedHour));

        return formattedDate;
    }
}
