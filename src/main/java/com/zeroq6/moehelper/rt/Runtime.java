package com.zeroq6.moehelper.rt;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.zeroq6.moehelper.bean.Page;
import com.zeroq6.moehelper.log.Log;

/**
 * Post或Pool解析线程运行时产生的结果数据
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 * @version moehelper - v1.0.7
 * @url https://github.com/icgeass/moehelper
 */
public class Runtime {

    private final static Map<Integer, String> mapId2JsonData = Collections.synchronizedMap(new HashMap<Integer, String>(100));
    private final static Map<Integer, Log> mapId2Log = Collections.synchronizedMap(new HashMap<Integer, Log>(100));
    private final static Map<Integer, Page> mapId2Page = Collections.synchronizedMap(new HashMap<Integer, Page>(100));

    private static int failedPageNum = 0;

    private Runtime() {
    }

    public synchronized static void readPageFailed() {
        failedPageNum++;
    }

    public static int getFailedPageNum() {
        return failedPageNum;
    }

    public static Map<Integer, String> getMapid2jsondata() {
        return mapId2JsonData;
    }

    public static Map<Integer, Log> getMapid2log() {
        return mapId2Log;
    }

    public static Map<Integer, Page> getMapid2page() {
        return mapId2Page;
    }

}
