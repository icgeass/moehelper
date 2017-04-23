package com.zeroq6.moehelper.resources;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zeroq6.moehelper.bean.Page;
import com.zeroq6.moehelper.log.Log;

/**
 * Post或Pool解析线程运行时产生的结果数据
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public class ResourcesHolder {

    private final static Map<Integer, String> mapIdJson = new ConcurrentHashMap<Integer, String>();
    private final static Map<Integer, Log> mapIdLog = new ConcurrentHashMap<Integer, Log>();
    private final static Map<Integer, Page> mapIdPage = new ConcurrentHashMap<Integer, Page>();

    private static int readFailedPageCount = 0;

    private ResourcesHolder() {
    }

    public synchronized static void readPageFailed() {
        readFailedPageCount++;
    }

    public static int getReadFailedPageCount() {
        return readFailedPageCount;
    }

    public static Map<Integer, String> getMapIdJson() {
        return mapIdJson;
    }

    public static Map<Integer, Log> getMapIdLog() {
        return mapIdLog;
    }

    public static Map<Integer, Page> getMapIdPage() {
        return mapIdPage;
    }

}
