package cn.edu.fudan.codetracker.constants;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * description: 公共、常用 常量
 *
 * @author fancying
 * create: 2020-06-04 15:23
 **/
public interface PublicConstants {

    boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * string 转 Date
     * @param date 2020-01-01 00:00:00
     * @return Date
     */
    default Date getDateByString(String date) {
        LocalDateTime localDateTime = LocalDateTime.parse(date, FORMATTER);
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }
}
