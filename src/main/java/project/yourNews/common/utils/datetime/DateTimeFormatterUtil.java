package project.yourNews.common.utils.datetime;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateTimeFormatterUtil {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    /**
     * 주어진 시간을 LocalDateTime으로 변환하는 메서드
     *
     * @param dateTimeStr : 변환할 날짜 문자열 (예: "Mon, 30 Dec 2024 20:14:00 +0900")
     * @return LocalDateTime 객체
     */
    public static LocalDateTime parseToLocalDateTime(String dateTimeStr) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        return zonedDateTime.toLocalDateTime();
    }
}