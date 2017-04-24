package com.zeroq6.moehelper.log.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zeroq6.moehelper.log.Log;
import com.zeroq6.moehelper.utils.MyLogUtils;

/**
 * Pool日志类, 存放解析处理的结果
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public class PoolLog implements Log, Comparable<PoolLog> {

    /**
     * pool页面状态
     */
    // 该页面404或不存在（被重定向到https://yande.re/pool, 表现为没有找到Json数据）
    public final static String POOL_STATUS_NULL = "null";
    // 页面存在, 没有Post（图片数量为0）, 一般是因为新创建的Pool未添加, Json中无数据, 为{"posts":[],"pool_posts":[],"pools":[],"tags":{},"votes":{}}
    public final static String POOL_STATUS_EMPTY = "empty";
    // 页面存在, 含至少一张Post, 但均显示为被删除（https://assets.yande.re/deleted-preview.png）, Json里面有数据不过没有file_url等字段, 如Pool=2057
    public final static String POOL_STATUS_ALL_DELETED = "all deleted";
    // 正常Pool, 相对于上次抓取时图片没有更新
    public final static String POOL_STATUS_NO_CHANGE = "no change";
    // 正常Pool, 相对于上次抓取时图片有更新
    public final static String POOL_STATUS_MODIFIED = "modified";
    // 正常Pool, 上次抓取时没有该Pool, 新添加的Pool
    public final static String POOL_STATUS_NEW = "new";


    // 页面状态-对应页面数量
    private static Map<String, Integer> mapPageStatus2Count = new ConcurrentHashMap<String, Integer>(6);
    // 页面Id对Pool详细描述
    private static Map<Integer, String> mapPageId2PoolDescription = new ConcurrentHashMap<Integer, String>();
    // Id对zip链接的List集合（对照上次处理结果，需要更新的）
    private static Map<Integer, List<String>> mapPageId2ZipLinkPoolUpdated = new ConcurrentHashMap<Integer, List<String>>();
    // Id对zip链接的List集合（本次所有，页面状态为no change, modified, new共三种）
    private static Map<Integer, List<String>> mapPageId2ZipLinkPoolAll = new ConcurrentHashMap<Integer, List<String>>();

    static {
        mapPageStatus2Count.put(POOL_STATUS_NULL, new Integer(0));
        mapPageStatus2Count.put(POOL_STATUS_EMPTY, new Integer(0));
        mapPageStatus2Count.put(POOL_STATUS_ALL_DELETED, new Integer(0));
        mapPageStatus2Count.put(POOL_STATUS_NO_CHANGE, new Integer(0));
        mapPageStatus2Count.put(POOL_STATUS_MODIFIED, new Integer(0));
        mapPageStatus2Count.put(POOL_STATUS_NEW, new Integer(0));
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
    public static synchronized void logPageCountByPageStatus(String pageStatus) {
        if (mapPageStatus2Count.containsKey(pageStatus)) {
            mapPageStatus2Count.put(pageStatus, mapPageStatus2Count.get(pageStatus) + 1);
        } else {
            MyLogUtils.fatal("传入页面状态错误, " + pageStatus);
        }
    }

    /**
     * 得到指定pageStatus状态下页面读取数目
     * 
     * @param pageStatus
     * @return int
     */
    public static synchronized Integer getPageCountByPageStatus(String pageStatus) {
        if (mapPageStatus2Count.containsKey(pageStatus)) {
            return mapPageStatus2Count.get(pageStatus);
        } else {
            MyLogUtils.fatal("传入页面状态错误, " + pageStatus);
            return null;
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



    ///////


    public int getId() {
        return id;
    }


    public int getJpegPackages() {
        return jpegPackages;
    }

    public PoolLog setJpegPackages(int jpegPackages) {
        this.jpegPackages = jpegPackages;
        return this;
    }

    public int getOriginalPackages() {
        return originalPackages;
    }

    public PoolLog setOriginalPackages(int originalPackages) {
        this.originalPackages = originalPackages;
        return this;
    }

    public int getAllPackages() {
        return allPackages;
    }

    public PoolLog setAllPackages(int allPackages) {
        this.allPackages = allPackages;
        return this;
    }

    public String getIsExist() {
        return isExist;
    }

    public PoolLog setIsExist(String isExist) {
        this.isExist = isExist;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public PoolLog setStatus(String status) {
        this.status = status;
        return this;
    }

    @Override
    public int compareTo(PoolLog o) {
        return this.getId() - o.getId();
    }
}
