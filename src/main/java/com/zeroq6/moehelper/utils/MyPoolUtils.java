package com.zeroq6.moehelper.utils;

/**
 * Created by zengshengxin on 2017/4/26.
 */
public class MyPoolUtils
{


    public static int toPoolJpegCount(Integer status){
        return status & 0b01;
    }
    public static int toPoolOriginalCount(Integer status){
        return (status & 0b10) >>> 1;
    }
}
