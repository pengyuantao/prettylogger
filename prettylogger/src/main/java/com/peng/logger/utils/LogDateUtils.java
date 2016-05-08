package com.peng.logger.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by administrator on 16/5/1.
 */
public class LogDateUtils {

    public static String getDateFormat() {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date1 = format1.format(new Date(System.currentTimeMillis()));
        return date1;// 2012-10-03 23:41:31
    }
}
