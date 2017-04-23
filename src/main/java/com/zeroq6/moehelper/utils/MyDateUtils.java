package com.zeroq6.moehelper.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yuuki asuna on 2017/4/23.
 */
public class MyDateUtils {

    private final static SimpleDateFormat defaultDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String formatCurrentTime(){
        return defaultDateFormat.format(new Date());
    }

    public static String format(Date date, String pattern){
        return new SimpleDateFormat(pattern).format(date);
    }


}
