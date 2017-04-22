package com.zeroq6.moehelper.log.impl;

import java.util.HashMap;
import java.util.Map;

import com.zeroq6.moehelper.config.Constants;
import com.zeroq6.moehelper.log.Log;

/**
 * Post日志类, 存放解析处理的结果
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 * @version moehelper - v1.0.7
 * @url https://github.com/icgeass/moehelper
 */
public class PostLog implements Log {

    private static Map<String, Integer> mapPageStatus2Count = new HashMap<String, Integer>();
    private int id = 0;
    private int postStatus = 0b1000000;
    private String isInPool = "null";
    private String isReadOk = "no ";// 留一个空格方便输出

    static {
        mapPageStatus2Count.put(Constants.POST_STATUS_READ_BY_JSON, new Integer(0));
        mapPageStatus2Count.put(Constants.POST_STATUS_READ_BY_DOCUMENT, new Integer(0));
        mapPageStatus2Count.put(Constants.POST_STATUS_NO_LINK_FOUND, new Integer(0));
        mapPageStatus2Count.put(Constants.POST_STATUS_EXCEPTION, new Integer(0));
        mapPageStatus2Count.put(Constants.POST_STATUS_404, new Integer(0));
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

    public int getId() {
        return id;
    }

    public int getPostStatus() {
        return postStatus;
    }

    public void setPostStatus(int postStatus) {
        this.postStatus = postStatus;
    }

    public String getIsInPool() {
        return isInPool;
    }

    public void setIsInPool(String isInPool) {
        this.isInPool = isInPool;
    }

    public String getIsReadOk() {
        return isReadOk;
    }

    public void setIsReadOk(String isReadOk) {
        this.isReadOk = isReadOk;
    }

    @Override
    public int compareTo(Log o) {
        return this.id - ((PostLog) o).id;
    }

}
