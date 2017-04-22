package com.zeroq6.moehelper.log.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zeroq6.moehelper.config.Constants;
import com.zeroq6.moehelper.log.Log;

/**
 * Pool日志类, 存放解析处理的结果
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 * @version moehelper - v1.0.7
 * @url https://github.com/icgeass/moehelper
 */
public class PoolLog implements Log {

    // 页面Id对页面类型
    private static Map<String, Integer> mapPageStatus2Count = new HashMap<String, Integer>(6);
    // 页面Id对Pool详细描述
    private static Map<Integer, String> mapPageId2PoolDescription = Collections.synchronizedMap(new HashMap<Integer, String>(200));
    // Id对zip链接的List集合（对照上次处理结果，需要更新的）
    private static Map<Integer, List<String>> mapPageId2ZipLinkPoolUpdated = Collections.synchronizedMap(new HashMap<Integer, List<String>>(200));
    // Id对zip链接的List集合（本次所有，页面状态为no change, modified, new共三种）
    private static Map<Integer, List<String>> mapPageId2ZipLinkPoolAll = Collections.synchronizedMap(new HashMap<Integer, List<String>>(200));

    static {
        mapPageStatus2Count.put(Constants.POOL_STATUS_NULL, new Integer(0));
        mapPageStatus2Count.put(Constants.POOL_STATUS_EMPTY, new Integer(0));
        mapPageStatus2Count.put(Constants.POOL_STATUS_ALL_DELETED, new Integer(0));
        mapPageStatus2Count.put(Constants.POOL_STATUS_NO_CHANGE, new Integer(0));
        mapPageStatus2Count.put(Constants.POOL_STATUS_MODIFIED, new Integer(0));
        mapPageStatus2Count.put(Constants.POOL_STATUS_NEW, new Integer(0));
    }

    private int id = 0;

    private int jpegPackages = 0;

    private int originalPackages = 0;

    private int allPackages = 0;

    private String isExist = "false";

    private String status = "null";

    public PoolLog(int id) {
        this.id = id;
    }

    /**
     * pageStatus下页面读取数加1
     * 
     * @param pageStatus
     * @return void
     */
    public static synchronized void logPageNumByType(String pageStatus) {
        if (mapPageStatus2Count.containsKey(pageStatus)) {
            mapPageStatus2Count.put(pageStatus, mapPageStatus2Count.get(pageStatus) + 1);
        } else {
            throw new UnsupportedOperationException("For input " + pageStatus);
        }
    }

    /**
     * 得到指定pageStatus状态下页面读取数目
     * 
     * @param pageStatus
     * @return int
     */
    public static synchronized int getPageNumStatus(String pageStatus) {
        if (mapPageStatus2Count.containsKey(pageStatus)) {
            return mapPageStatus2Count.get(pageStatus);
        } else {
            throw new UnsupportedOperationException("For input " + pageStatus);
        }
    }

    public static Map<Integer, String> getMapPageId2PoolDescription() {
        return mapPageId2PoolDescription;
    }

    public static Map<Integer, List<String>> getMapPageId2ZipLinkPoolUpdated() {
        return mapPageId2ZipLinkPoolUpdated;
    }

    public static Map<Integer, List<String>> getMapPageId2ZipLinkPoolAll() {
        return mapPageId2ZipLinkPoolAll;
    }

    public int getId() {
        return id;
    }

    public int getJpegPackages() {
        return jpegPackages;
    }

    public void setJpegPackages(int jpegPackages) {
        this.jpegPackages = jpegPackages;
    }

    public int getOriginalPackages() {
        return originalPackages;
    }

    public void setOriginalPackages(int originalPackages) {
        this.originalPackages = originalPackages;
    }

    public int getAllPackages() {
        return allPackages;
    }

    public void setAllPackages(int allPackages) {
        this.allPackages = allPackages;
    }

    public String getIsExist() {
        return isExist;
    }

    public void setIsExist(String isExist) {
        this.isExist = isExist;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int compareTo(Log o) {
        return this.id - ((PoolLog) o).id;
    }

}
