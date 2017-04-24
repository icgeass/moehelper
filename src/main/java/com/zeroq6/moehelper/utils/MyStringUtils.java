package com.zeroq6.moehelper.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

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
public class MyStringUtils {

    /**
     * 传人byte的long值, 返回可读文件大小
     * 
     * @param fileSizeInBytes
     * @return String
     */
    public static String getReadableFileSize(long fileSizeInBytes) {
        String[] byteUtils = { " Bytes", " KiB", " MiB", " GiB", " TiB", " PiB", " EiB", " ZiB", " YiB" };
        int i = 0;
        float fileSize = fileSizeInBytes;
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
        String suffix = null;
        String prefix = url.substring(0, url.lastIndexOf("/") + 1);
        try {
            suffix = URLDecoder.decode(url.substring(url.lastIndexOf("/") + 1), "utf-8");
            suffix = replaceIllegalCharInFilename(suffix);
            suffix = URLEncoder.encode(suffix, "utf-8");
        } catch (Exception e) {
            MyLogUtils.fatal(e.getMessage(), e);
        }
        return prefix + suffix.replace("+", "%20");// 由于此时的re已经是URL编码,所以原有的"+"不会被替换掉
    }

    public static String replaceIllegalCharInFilename(String filename){
        if(null == filename){
            return null;
        }
        String[] illegalCharArray = { "/", "\\", "*", "?", ":", "|", "<", ">", "\"" };
        for(String illegalChar : illegalCharArray){
            filename = filename.replace(illegalChar, "_");
        }
        return filename;
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
            re = replaceIllegalCharInFilename(re);
        } catch (Exception e) {
            MyLogUtils.fatal(e.getMessage(), e);
        }
        return re;
    }

    public static String insertBeforePlusOrMinus(Integer number, String charInsertWhenZero) {
        if (!"+".equals(charInsertWhenZero) && !"-".equals(charInsertWhenZero)) {
            MyLogUtils.fatal("只能输入\"+\"或者\"-\"");
        }
        if (0 == number) {
            return charInsertWhenZero + number;
        }
        return number > 0 ? "+" + number : String.valueOf(number);
    }


    /**
     * 返回字符串sourceStr中第times次出现的targetSubStr的位置
     * 没有则返回-1
     * 
     * @param sourceStr
     * @param targetSubStr
     * @param times
     * @return int
     */
    public static int getInnerStrIndex(String sourceStr, String targetSubStr, int times) {
        int re = -1;
        int count = 0;
        int pos = 0;
        if (sourceStr == null || targetSubStr == null) {
            MyLogUtils.fatal("sourceStr，targetSubStr不能为null");
        }
        if(times < 0){
            MyLogUtils.fatal("times不能小于0");
        }
        while (count != times) {
            re = sourceStr.indexOf(targetSubStr, pos);
            if (re == -1) {
                break;
            }
            pos += targetSubStr.length();
            count++;
        }
        return re;
    }

}
