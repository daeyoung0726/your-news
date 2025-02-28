package project.yourNews.common.utils.datetime;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateTimeFormatterUtil {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static LocalDate parseToLocalDateTime(String date) {
        return LocalDate.parse(date, formatter);
    }
}