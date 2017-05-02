package com.zeroq6.moehelper.utils;

import java.net.URLDecoder;
import java.net.URLEncoder;

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


    public static String decodeUTF8(String string) {
        try {
            return URLDecoder.decode(string, "utf-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodeUTF8(String string) {
        try {
            return URLEncoder.encode(string, "utf-8").replace("+", "%20");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String insertBeforePlusOrMinus(Integer number) {
        if (0 == number) {
            return number + "";
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
        if (sourceStr == null || targetSubStr == null) {
            throw new RuntimeException("sourceStr，targetSubStr不能为null");
        }
        if(times < 0){
            throw new RuntimeException("times不能小于0");
        }
        while (count != times) {
            re = sourceStr.indexOf(targetSubStr, re + targetSubStr.length());
            if (re == -1) {
                break;
            }
            count++;
        }
        return re;
    }



}
