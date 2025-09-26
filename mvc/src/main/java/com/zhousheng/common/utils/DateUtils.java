package com.zhousheng.common.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    // 避免每次创建一个格式器
    private static final DateTimeFormatter YYYY_MM_DD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 私有构造函数，防止被实例化
    private DateUtils() {}

    public static String getCurrentDateString() {
        return LocalDate.now().format(YYYY_MM_DD_FORMATTER);
    }
}
