package com.zeroq6.moehelper.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 工具类
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public class Kit {

    private static String[] byteUtils = { " Bytes", " KiB", " MiB", " GiB", " TiB", " PiB", " EiB", " ZiB", " YiB" };

    private static String illegalCharaterInFilename[] = { "/", "\\", "*", "?", ":", "|", "<", ">", "\"" };

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public static String getFormatedCurrentTime() {
        return sdf.format(Calendar.getInstance().getTime());
    }

    /**
     * 传人byte的long值, 返回可读文件大小
     * 
     * @param fileSizeInBytes
     * @return String
     */
    public static String getReadableFileSize(long fileSizeInBytes) {
        int i = 0;
        // 用double得到除后的小数位
        double fileSize = fileSizeInBytes;
        while (fileSize > 1024) {
            fileSize = fileSize / 1024;
            i++;
        }
        return String.format("%.2f", fileSize) + byteUtils[i];
    }

    /**
     * 替换URL连接中包含的非法字符为_
     * 
     * @param url
     * @return String
     */
    public static String formatUrlLink(String url) {
        String re = null;
        String t = url.substring(0, url.lastIndexOf("/") + 1);
        try {
            re = URLDecoder.decode(url.substring(url.lastIndexOf("/") + 1), "utf-8");
            for (int i = 0; i < illegalCharaterInFilename.length; i++) {
                re = re.replace(illegalCharaterInFilename[i], "_");
            }
            re = URLEncoder.encode(re, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        re = t + re.replace("+", "%20");// 由于此时的re已经是URL编码,所以原有的"+"不会被替换掉
        return re;
    }

    /**
     * 替换文件名字符串中的非法字符为_
     * 
     * @param fileName
     * @return String
     */
    public static String formatFileName(String fileName) {
        String re = null;
        try {
            re = URLDecoder.decode(fileName, "utf-8");
            for (String string : illegalCharaterInFilename) {
                re = re.replace(string, "_");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return re;
    }

    public static String parseDigitWithSymbol(Integer number, String charInsertWhenZero) {
        if (!"+".equals(charInsertWhenZero) && !"-".equals(charInsertWhenZero)) {
            Logger.fatal("For input " + charInsertWhenZero);
        }
        if (0 == number) {
            return charInsertWhenZero + number;
        }
        return number > 0 ? "+" + number : String.valueOf(number);
    }

    /**
     * 返回字符串target中第times次出现的str的位置
     * 
     * @param target
     * @param str
     * @param times
     * @return int
     */
    public static int getInnerStrIndex(String target, String str, int times) {
        int re = -1;
        int count = 0;
        if (target == null || str == null || "".equals(target.trim()) || "".equals(str.trim()) || times <= 0) {
            return re;
        }
        while (count != times) {
            re = target.indexOf(str, re + 1);
            if (re == -1) {
                break;
            }
            count++;
        }
        return re;
    }

}
