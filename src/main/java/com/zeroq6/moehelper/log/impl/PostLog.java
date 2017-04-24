package com.zeroq6.moehelper.log.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zeroq6.moehelper.log.Log;
import com.zeroq6.moehelper.utils.MyLogUtils;

/**
 * Post日志类, 存放解析处理的结果
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public class PostLog implements Log, Comparable<PostLog> {

    /**
     * post页面状态
     */
    // 页面404
    public final static String POST_STATUS_404 = "404";
    // 程序异常
    public final static String POST_STATUS_EXCEPTION = "exception";
    // 从Json中获取链接
    public final static String POST_STATUS_READ_BY_JSON = "read_by_json";
    // 从文档中中获取链接
    public final static String POST_STATUS_READ_BY_DOCUMENT = "read_by_document";
    // 未找到链接
    public final static String POST_STATUS_NO_LINK_FOUND = "no_link_found";



    private static Map<String, Integer> mapPageStatus2Count = new ConcurrentHashMap<String, Integer>();
    private int id = 0;
    private int postStatus = 0b1000000;
    private String isInPool = "null";
    private String isReadOk = "no ";// 留一个空格方便输出

    static {
        mapPageStatus2Count.put(POST_STATUS_READ_BY_JSON, new Integer(0));
        mapPageStatus2Count.put(POST_STATUS_READ_BY_DOCUMENT, new Integer(0));
        mapPageStatus2Count.put(POST_STATUS_NO_LINK_FOUND, new Integer(0));
        mapPageStatus2Count.put(POST_STATUS_EXCEPTION, new Integer(0));
        mapPageStatus2Count.put(POST_STATUS_404, new Integer(0));
    }

    public PostLog(int id) {
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

    public int getId() {
        return id;
    }


    public int getPostStatus() {
        return postStatus;
    }

    public PostLog setPostStatus(int postStatus) {
        this.postStatus = postStatus;
        return this;
    }

    public String getIsInPool() {
        return isInPool;
    }

    public PostLog setIsInPool(String isInPool) {
        this.isInPool = isInPool;
        return this;
    }

    public String getIsReadOk() {
        return isReadOk;
    }

    public PostLog setIsReadOk(String isReadOk) {
        this.isReadOk = isReadOk;
        return this;
    }

    @Override
    public int compareTo(PostLog o) {
        return this.getId() - o.getId();
    }
}
