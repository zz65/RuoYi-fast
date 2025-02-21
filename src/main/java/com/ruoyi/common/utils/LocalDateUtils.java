package com.ruoyi.common.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author zhuzhen
 * @date 2023/3/31 11:36
 * @description: 详细说明
 */
public final class LocalDateUtils {
    public static final String PATTERN_MMDD_CHINESE = "MM月dd日";
    public static final String PATTERN_YYYY = "yyyy";
    public static final String PATTERN_YYYYMM = "yyyy-MM";
    public static final String PATTERN_YYYYMMDD = "yyyy-MM-dd";
    public static final String PATTERN_YYYY1MM1DD = "yyyy/MM/dd";
    public static final String PATTERN_YYYYMMDD_HHmmss = "yyyy-MM-dd HH:mm:ss";

    public static final String START_TIME = " 00:00:00";
    public static final String END_TIME = " 23:59:59";

    private LocalDateUtils(){}

    /**
     * 计算相差天数
     * @param beginDate
     * @param endDate
     * @return
     */
    public static int diffDays(LocalDateTime beginDate, LocalDateTime endDate) {
        int daysNum=(int)(endDate.toLocalDate().toEpochDay() - beginDate.toLocalDate().toEpochDay());
        return daysNum;
    }
    public static int diffDays(LocalDate beginDate, LocalDate endDate) {
        int daysNum=(int)(endDate.toEpochDay() - beginDate.toEpochDay());
        return daysNum;
    }

    public static long dateTimeToMillis(LocalDateTime localDateTime) {
        // 指定中国的时区
        ZoneId chinaZoneId = ZoneId.of("Asia/Shanghai");

        // 将 LocalDateTime 转换为 ZonedDateTime
        ZonedDateTime chinaZonedDateTime = localDateTime.atZone(chinaZoneId);

        // 将 ZonedDateTime 转换为 Instant
        Instant instant = chinaZonedDateTime.toInstant();

        // 获取自1970年1月1日以来的毫秒数
        long epochMilli = instant.toEpochMilli();
        return epochMilli;
    }

    public static long dateTimeToSeconds(LocalDateTime localDateTime) {
        long millis = dateTimeToMillis(localDateTime);
        long seconds = millis / 1000;
        return seconds;
    }

    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ 日期时间 start ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    public static String dateTimeToString(LocalDateTime localDateTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        String string = localDateTime.format(formatter);
        return string;
    }

    public static String dateTimeToString(LocalDateTime localDateTime) {
        String s = dateTimeToString(localDateTime, PATTERN_YYYYMMDD_HHmmss);
        return s;
    }

    public static String dateTimeToStringDate(LocalDateTime localDateTime) {
        String s = dateTimeToString(localDateTime, PATTERN_YYYYMMDD);
        return s;
    }

    public static LocalDateTime stringToDateTime(String str, String pattern) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime localDateTime = LocalDateTime.parse(str, dateTimeFormatter);
        return localDateTime;
    }

    public static LocalDateTime stringToDateTime(String str) {
        LocalDateTime localDateTime = stringToDateTime(str, PATTERN_YYYYMMDD_HHmmss);
        return localDateTime;
    }


    public static LocalDateTime longToDateTime(Long timemillis) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timemillis), ZoneId.systemDefault());
        return localDateTime;
    }

    public static String longToStringDate(Long timemillis) {
        LocalDateTime localDateTime = longToDateTime(timemillis);
        String s = dateTimeToString(localDateTime, PATTERN_YYYYMMDD);
        return s;
    }

    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ 日期时间 end ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ 日期 start ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
    public static String dateToString(LocalDate localDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN_YYYYMMDD);
        String string = localDate.format(formatter);
        return string;
    }

    public static String dateToString(LocalDate localDate, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        String string = localDate.format(formatter);
        return string;
    }

    public static LocalDate stringToDate(String str, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDate localDate = LocalDate.parse(str, formatter);
        return localDate;
    }

    public static LocalDate stringToDate(String str) {
        LocalDate localDate = stringToDate(str, PATTERN_YYYYMMDD);
        return localDate;
    }


    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ 日期 end ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
}
