package com.zeroq6.moehelper.fetcher.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.zeroq6.moehelper.bean.Page;
import com.zeroq6.moehelper.bean.Pool;
import com.zeroq6.moehelper.bean.Pool_post;
import com.zeroq6.moehelper.bean.Post;
import com.zeroq6.moehelper.log.impl.PoolLog;
import com.zeroq6.moehelper.utils.MyLogUtils;
import com.zeroq6.moehelper.writer.Writer;
import org.apache.commons.io.FileUtils;

import com.alibaba.fastjson.JSON;

/**
 * 用于比较判断上次和本次Pool更新情况的辅助类
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public class PrevPoolCheckUtils {

    private static int lastTimePageFrom = 99999;
    private static int lastTimePageTo = -1;
    private static boolean init = false;

    private static Pattern pattern_filename_json = Pattern.compile("^yande.re_-_pool_[0-9]{12}_[0-9]{1,}_[0-9]{1,}[.]json$");

    private static Pattern pattern_filename_packages_all_url = Pattern.compile("^yande.re_-_pool_[0-9]{12}_[0-9]{1,}_[0-9]{1,}_packages_url[.]lst$");

    private static Pattern pattern_md5 = Pattern.compile("^[0-9a-z]{32}$");

    // 来自json文件, 读取时忽略all deleted和empty两种情况
    private static Map<Integer, Set<String>> mapLastTimePageId2PostMd5List = Collections.synchronizedMap(new HashMap<Integer, Set<String>>(100));

    // 来自lst（所有zip链接），lst文件中原本不含all deleted和empty两种情况
    private static Map<Integer, Integer> mapLastTimePageId2ZipLinkCountInfo = Collections.synchronizedMap(new HashMap<Integer, Integer>(100));

    // 私有
    private PrevPoolCheckUtils() {
    }

    public static synchronized void init() {
        if (init) {
           MyLogUtils.fatal("只能初始化一次");
        }
        // 两者顺序不能变, initMapLastTimePageId2ZipLinkCountInfo需要设置上一次更新的PoolId范围
        initMapLastTimePageId2ZipLinkCountInfo();
        initMapLastTimePageId2PostMd5List();
        if (!mapLastTimePageId2ZipLinkCountInfo.keySet().equals(mapLastTimePageId2PostMd5List.keySet())) {
            MyLogUtils.fatal("链接信息和MD5信息不一致");
        }
        init = true;
    }

    /**
     * 读取存储上次所有Pool的Url的.lst文件，记录PoolId到该Pool链接数量状态的映射
     * 
     * @return void
     */
    private static void initMapLastTimePageId2ZipLinkCountInfo() {
        File fileToRead = null;
        try {
            File dir = new File(Writer.W_WRITE_DIR);
            List<String> liLastTimeAllPackageUrl = new ArrayList<String>();
            fileToRead = new File(Writer.W_WRITE_DIR + "/yande.re_-_a");
            for (File file : dir.listFiles()) {
                if (pattern_filename_packages_all_url.matcher(file.getName()).matches()) {
                    if (file.getName().compareTo(fileToRead.getName()) > 0) {
                        fileToRead = file;
                    }
                }
            }
            // 是否查找到文件
            if (fileToRead.getName().endsWith(".lst")) {
                MyLogUtils.info("read from " + fileToRead.getName() + ".");
                liLastTimeAllPackageUrl = FileUtils.readLines(fileToRead, "utf-8");
            }
            // 是否读取成功
            if (liLastTimeAllPackageUrl.isEmpty()) {
                MyLogUtils.info("read .lst file failed.");
                return;
            }
            // https://yande.re/pool/zip/3387/Dengeki%20Moeoh%202014-04%20(JPG).zip?jpeg=1
            // 20170204, now the url like https://yande.re/pool/zip/6?jpeg=1 ,  https://yande.re/pool/zip/6
            int lastTimeZipJpegNum = 0;
            int lastTimeZipOriginalNum = 0;
            int lastTimeZipAllNum = 0;
            for (String url : liLastTimeAllPackageUrl) {
                String[] splitStrArr = url.split("/");
                if(null == splitStrArr || splitStrArr.length != 6){
                    MyLogUtils.fatal("pool包链接格式不正确, " + url);
                }
                String pageIdString = splitStrArr[splitStrArr.length - 1];
                Integer pageId = Integer.valueOf(pageIdString.substring(0, pageIdString.indexOf("?") == -1 ? pageIdString.length() : pageIdString.indexOf("?")));
                if (pageId > lastTimePageTo) {
                    lastTimePageTo = pageId;
                }
                if (pageId < lastTimePageFrom) {
                    lastTimePageFrom = pageId;
                }
                if (!mapLastTimePageId2ZipLinkCountInfo.keySet().contains(pageId)) {
                    mapLastTimePageId2ZipLinkCountInfo.put(pageId, new Integer(0));
                }
                if (url.contains(PoolFetcher.LINK_POOL_ZIP_SUFFIX_JPG)) {
                    lastTimeZipJpegNum++;
                    lastTimeZipAllNum++;
                    // 01
                    mapLastTimePageId2ZipLinkCountInfo.put(pageId, mapLastTimePageId2ZipLinkCountInfo.get(pageId) + 1);
                } else{
                    lastTimeZipOriginalNum++;
                    lastTimeZipAllNum++;
                    // 10
                    mapLastTimePageId2ZipLinkCountInfo.put(pageId, mapLastTimePageId2ZipLinkCountInfo.get(pageId) + 2);
                }
                // 最终只会有0b10或0b11，判断0b01, 0b10是因为第一次处理链接时原图和jpeg图链接顺序不确定，而0b11是第二次有原图情况下的计数
                Integer pageId2CountCurrent = mapLastTimePageId2ZipLinkCountInfo.get(pageId);
                if (pageId2CountCurrent != 0b01 && pageId2CountCurrent != 0b10 && pageId2CountCurrent != 0b11) {
                    MyLogUtils.fatal("pool id对应的pool链接数量不正确, " + pageId);
                }
            }
            MyLogUtils.info("read file " + fileToRead.getName() + " success, " + mapLastTimePageId2ZipLinkCountInfo.size() + " Pool, " + liLastTimeAllPackageUrl.size() + " Pool zip package urls (jpeg: " + lastTimeZipJpegNum + ", original: " + lastTimeZipOriginalNum + ", all: " + lastTimeZipAllNum + ") in all.");
        } catch (Exception e) {
            MyLogUtils.fatal(e.getMessage(), e);
        }
    }

    /**
     * 读取存储上次所有Pool的Json字符串的.json文件, 记录PoolId到该Pool下所有图片MD5的List集合映射
     * 
     * @return void
     */
    private static void initMapLastTimePageId2PostMd5List() {
        File fileToRead = null;
        try {
            // 读取本地json文件
            File f = new File(Writer.W_WRITE_DIR);
            List<String> liLastTimeJsonString = new ArrayList<String>();
            fileToRead = new File(Writer.W_WRITE_DIR + "/yande.re_-_a");
            for (File file : f.listFiles()) {
                if (pattern_filename_json.matcher(file.getName()).matches()) {
                    if (file.getName().compareTo(fileToRead.getName()) > 0) {
                        fileToRead = file;
                    }
                }
            }
            if (fileToRead.getName().endsWith(".json")) {
                MyLogUtils.info("read from " + fileToRead.getName() + "");
                liLastTimeJsonString = FileUtils.readLines(fileToRead, "utf-8");
            }
            // 判断文件是否读取成功
            if (liLastTimeJsonString.isEmpty()) {
                MyLogUtils.info("read .json file failed");
                return;
            }

            int numEmptyAndAllDeletedPool = 0;
            for (String jsonString : liLastTimeJsonString) {
                Page page = JSON.parseObject(jsonString, Page.class);
                // 除了EMPTY，ALL_DELETED，其余都是NEW
                String pageStatus = checkPageStatus(page, null);
                if (PoolLog.POOL_STATUS_EMPTY.equals(pageStatus)) {
                    numEmptyAndAllDeletedPool++;
                    MyLogUtils.info("Pool # NaN empty pool found");
                    continue;
                }
                if (PoolLog.POOL_STATUS_ALL_DELETED.equals(pageStatus)) {
                    numEmptyAndAllDeletedPool++;
                    Pool pool = page.getPools().get(0); // json数据中有多个pool时，不准确，没必要处理
                    MyLogUtils.info("Pool # " + pool.getId() + " " + pool.getName().replace("_", " ") + " with all posts deleted.");
                    continue;
                }
                Map<Integer, String> mapPostId2Md5 = new HashMap<Integer, String>();
                for (Post post : page.getPosts()) {
                    mapPostId2Md5.put(post.getId(), post.getMd5());
                }
                for (Pool_post pool2post : page.getPool_posts()) {
                    // 如果json数据中包括了多个pool信息，而输入（上一次）指定的pool id范围没有包括其中一个或多个pool信息对应的id，则url list和md5 list大小不一致，后续校验出错
                    // 上次更新的id范围依lst文件为准，其他过滤掉
                    if (pool2post.getPool_id() > lastTimePageTo || pool2post.getPool_id() < lastTimePageFrom) {
                        continue;
                    }
                    if (!mapLastTimePageId2PostMd5List.keySet().contains(pool2post.getPool_id())) {
                        mapLastTimePageId2PostMd5List.put(pool2post.getPool_id(), new HashSet<String>());
                    }
                    String md5 = mapPostId2Md5.get(pool2post.getPost_id());
                    if (null == md5 || !pattern_md5.matcher(md5).matches()) {
                        MyLogUtils.fatal("错误的MD5格式, " + md5);
                    }
                    mapLastTimePageId2PostMd5List.get(pool2post.getPool_id()).add(md5);

                }
            }
            MyLogUtils.info("read file " + fileToRead.getName() + " success, " + liLastTimeJsonString.size() + " JSON string(Pool) in all, " + numEmptyAndAllDeletedPool + " empty(or all post deleted) pool found");
        } catch (Exception e) {
            MyLogUtils.fatal(e.getMessage(), e);
        }
    }

    /**
     * 返回当前Pool状态的枚举描述
     * 
     * @param page
     * @param pageId
     * @return String
     */
    public static String checkPageStatus(Page page, Integer pageId) {
        // 校验, 对于非null的Pool来说, posts和 pools均为空或非空
        if (page != null && page.getPosts().isEmpty() != page.getPools().isEmpty()) {
            MyLogUtils.fatal("非法的json数据格式, " + pageId);
        }
        if (null == page) {
            return PoolLog.POOL_STATUS_NULL;
        }
        if (page.getPosts().isEmpty() || page.getPools().isEmpty()) {
            return PoolLog.POOL_STATUS_EMPTY;
        }
        {
            boolean allPostsDeleted = true;
            for (Post post : page.getPosts()) {
                if (post.getFile_url() != null) {
                    allPostsDeleted = false;
                    break;
                }
            }
            if (allPostsDeleted) {
                return PoolLog.POOL_STATUS_ALL_DELETED;
            }
        }
        // pageId = null 始终返回POOL_STATUS_NEW用于初始mapLastTimePageId2PostMd5List时跳过后面判断
        if (null == pageId || !mapLastTimePageId2ZipLinkCountInfo.keySet().contains(pageId)) {
            return PoolLog.POOL_STATUS_NEW; // 若上一次的empty和all deleted添加了Post也将任务是new
        }
        for (Post post : page.getPosts()) {
            // 只要有新md5则认为被修改, 仅含有被移除md5不会被认为修改
            if (!mapLastTimePageId2PostMd5List.get(pageId).contains(post.getMd5())) {
                return PoolLog.POOL_STATUS_MODIFIED;
            }
        }
        return PoolLog.POOL_STATUS_NO_CHANGE;
    }

    public static Map<Integer, Set<String>> getMapLastTimePageId2PostMd5List() {
        return mapLastTimePageId2PostMd5List;
    }

    public static Map<Integer, Integer> getMapLastTimePageId2ZipLinkCountInfo() {
        return mapLastTimePageId2ZipLinkCountInfo;
    }

}
