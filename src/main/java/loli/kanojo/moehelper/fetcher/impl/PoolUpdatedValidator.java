package loli.kanojo.moehelper.fetcher.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.alibaba.fastjson.JSON;

import loli.kanojo.moehelper.bean.Page;
import loli.kanojo.moehelper.bean.Pool_post;
import loli.kanojo.moehelper.bean.Post;
import loli.kanojo.moehelper.config.Constants;
import loli.kanojo.moehelper.utils.Logger;

/**
 * 用于比较判断上次和本次Pool更新情况的辅助类
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 * @version moehelper - v1.0.7
 * @url https://github.com/icgeass/moehelper
 */
public class PoolUpdatedValidator {

    private static int lastTimePageFrom = 99999;
    private static int lastTimePageTo = -1;
    private static boolean isInited = false;

    private static Pattern pattern_filename_json = Pattern.compile("^yande.re_-_pool_[0-9]{12}_[0-9]{1,}_[0-9]{1,}[.]json$");

    private static Pattern pattern_filename_packages_all_url = Pattern.compile("^yande.re_-_pool_[0-9]{12}_[0-9]{1,}_[0-9]{1,}_packages_url[.]lst$");

    private static Pattern pattern_md5 = Pattern.compile("^[0-9a-z]{32}$");

    // 来自json文件, 读取时忽略all deleted和empty两种情况
    private static Map<Integer, Set<String>> mapLastTimePageId2PostMd5Li = Collections.synchronizedMap(new HashMap<Integer, Set<String>>(100));

    // 来自lst（所有zip链接），lst文件中原本不含all deleted和empty两种情况
    private static Map<Integer, Integer> mapLastTimePageId2ZipLinkNumInfo = Collections.synchronizedMap(new HashMap<Integer, Integer>(100));

    // 私有
    private PoolUpdatedValidator() {
    }

    public static synchronized void init() {
        if (isInited) {
            Logger.fatal("the method init can be called only once");
        }
        // 两者顺序不能变, initMapLastTimePageId2ZipLinkNumInfo需要设置上一次更新的PoolId范围
        initMapLastTimePageId2ZipLinkNumInfo();
        initMapLastTimePageId2PostMd5Li();
        if (!mapLastTimePageId2ZipLinkNumInfo.keySet().equals(mapLastTimePageId2PostMd5Li.keySet())) {
            Logger.fatal("cool");
        }
        isInited = true;
    }

    /**
     * 读取存储上次所有Pool的Url的.lst文件，记录PoolId到该Pool链接数量状态的映射
     * 
     * @return void
     */
    private static void initMapLastTimePageId2ZipLinkNumInfo() {
        File fileToRead = null;
        try {
            File dir = new File(Constants.W_WRITE_DIR);
            List<String> liLastTimeAllPackageUrl = new ArrayList<String>(100);
            fileToRead = new File(Constants.W_WRITE_DIR + "/yande.re_-_a");
            for (File file : dir.listFiles()) {
                if (pattern_filename_packages_all_url.matcher(file.getName()).matches()) {
                    if (file.getName().compareTo(fileToRead.getName()) > 0) {
                        fileToRead = file;
                    }
                }
            }
            // 是否查找到文件
            if (fileToRead.getName().endsWith(".lst")) {
                Logger.info("Read from " + fileToRead.getName() + "\r\n");
                liLastTimeAllPackageUrl = FileUtils.readLines(fileToRead, "utf-8");
            }
            // 是否读取成功
            if (liLastTimeAllPackageUrl.isEmpty()) {
                Logger.info("Read .lst file failed\r\n\r\n");
                return;
            }
            // https://yande.re/pool/zip/3387/Dengeki%20Moeoh%202014-04%20(JPG).zip?jpeg=1
            // 20170204, now the url like https://yande.re/pool/zip/6?jpeg=1 ,  https://yande.re/pool/zip/6
            int lastTimeZipJpegNum = 0;
            int lastTimeZipOriginalNum = 0;
            int lastTimeZipAllNum = 0;
            for (String url : liLastTimeAllPackageUrl) {
                String[] splitStrArr = url.split("/", 6);
                if(null == splitStrArr || splitStrArr.length != 6){
                    throw new RuntimeException("Error Pool url format: " + url);
                }
                String pageIdString = splitStrArr[splitStrArr.length - 1];
                Integer pageId = Integer.valueOf(pageIdString.substring(0, pageIdString.indexOf("?") == -1 ? pageIdString.length() : pageIdString.indexOf("?")));
                if (pageId > lastTimePageTo) {
                    lastTimePageTo = pageId;
                }
                if (pageId < lastTimePageFrom) {
                    lastTimePageFrom = pageId;
                }
                if (!mapLastTimePageId2ZipLinkNumInfo.keySet().contains(pageId)) {
                    mapLastTimePageId2ZipLinkNumInfo.put(pageId, new Integer(0));
                }
                if (url.contains(Constants.LINK_POOL_ZIP_SUFFIX_JPG)) {
                    lastTimeZipJpegNum++;
                    lastTimeZipAllNum++;
                    // 01
                    mapLastTimePageId2ZipLinkNumInfo.put(pageId, mapLastTimePageId2ZipLinkNumInfo.get(pageId) + 1);
                } else{
                    lastTimeZipOriginalNum++;
                    lastTimeZipAllNum++;
                    // 10
                    mapLastTimePageId2ZipLinkNumInfo.put(pageId, mapLastTimePageId2ZipLinkNumInfo.get(pageId) + 2);
                }
                // 最终只会有0b10或0b11，判断0b01, 0b10是因为第一次处理链接时原图和jpeg图链接顺序不确定，而0b11是第二次有原图情况下的计数
                if (mapLastTimePageId2ZipLinkNumInfo.get(pageId) != 0b01 && mapLastTimePageId2ZipLinkNumInfo.get(pageId) != 0b10 && mapLastTimePageId2ZipLinkNumInfo.get(pageId) != 0b11) {
                    Logger.fatal("Find a error zip pack number info, the page id is " + pageId);
                }
            }
            Logger.info("Read file " + fileToRead.getName() + " success, " + mapLastTimePageId2ZipLinkNumInfo.size() + " Pool, " + liLastTimeAllPackageUrl.size() + " Pool zip package urls (jpeg: " + lastTimeZipJpegNum + ", original: " + lastTimeZipOriginalNum + ", all: " + lastTimeZipAllNum + ") in all.\r\n\r\n");
        } catch (Exception e) {
            Logger.fatal("Read file " + fileToRead.getName() + " failed", e);
        }
    }

    /**
     * 读取存储上次所有Pool的Json字符串的.json文件, 记录PoolId到该Pool下所有图片MD5的List集合映射
     * 
     * @return void
     */
    private static void initMapLastTimePageId2PostMd5Li() {
        File fileToRead = null;
        try {
            // 读取本地json文件
            File f = new File(Constants.W_WRITE_DIR);
            List<String> liLastTimeJsonString = new ArrayList<String>(100);
            fileToRead = new File(Constants.W_WRITE_DIR + "/yande.re_-_a");
            for (File file : f.listFiles()) {
                if (pattern_filename_json.matcher(file.getName()).matches()) {
                    if (file.getName().compareTo(fileToRead.getName()) > 0) {
                        fileToRead = file;
                    }
                }
            }
            if (fileToRead.getName().endsWith(".json")) {
                Logger.info("Read from " + fileToRead.getName() + "\r\n");
                liLastTimeJsonString = FileUtils.readLines(fileToRead, "utf-8");
            }
            // 判断文件是否读取成功
            if (liLastTimeJsonString.isEmpty()) {
                Logger.info("Read .json file failed\r\n\r\n");
                return;
            }

            int numEmptyAndAllDeletedPool = 0;
            for (String jsonString : liLastTimeJsonString) {
                Page page = JSON.parseObject(jsonString, Page.class);
                String pageStatus = checkPageStatus(page, null);
                if (Constants.POOL_STATUS_EMPTY.equals(pageStatus)) {
                    numEmptyAndAllDeletedPool++;
                    Logger.info("Pool # NaN  Empty pool found\r\n");
                    continue;
                }
                if (Constants.POOL_STATUS_ALL_DELETED.equals(pageStatus)) {
                    numEmptyAndAllDeletedPool++;
                    Logger.info("Pool # " + page.getPools().get(0).getId() + " " + page.getPools().get(0).getName().replace("_", "") + " with all posts deleted\r\n");
                    continue;
                }
                Map<Integer, String> mapPostId2Md5 = new HashMap<Integer, String>();
                for (Post post : page.getPosts()) {
                    mapPostId2Md5.put(post.getId(), post.getMd5());
                }
                for (Pool_post pool2post : page.getPool_posts()) {
                    // 上次更新的id范围依lst文件为准
                    if (pool2post.getPool_id() > lastTimePageTo || pool2post.getPool_id() < lastTimePageFrom) {
                        continue;
                    }
                    if (!mapLastTimePageId2PostMd5Li.keySet().contains(pool2post.getPool_id())) {
                        mapLastTimePageId2PostMd5Li.put(pool2post.getPool_id(), new HashSet<String>());
                    }
                    String md5 = mapPostId2Md5.get(pool2post.getPost_id());
                    if (null == md5 || !pattern_md5.matcher(md5).matches()) {
                        Logger.fatal("Error md5 format");
                    }
                    mapLastTimePageId2PostMd5Li.get(pool2post.getPool_id()).add(md5);

                }
            }
            Logger.info("Read file " + fileToRead.getName() + " succeed, " + liLastTimeJsonString.size() + " JSON string(Pool) in all, " + numEmptyAndAllDeletedPool + " empty(or all post deleted) pool found\r\n\r\n");
        } catch (Exception e) {
            Logger.fatal("Read file " + fileToRead.getName() + " failed", e);
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
            Logger.fatal("wrong json format in pool page");
        }
        if (null == page) {
            return Constants.POOL_STATUS_NULL;
        }
        if (page.getPosts().isEmpty() || page.getPools().isEmpty()) {
            return Constants.POOL_STATUS_EMPTY;
        }
        {
            boolean isAllPostsDeleted = true;
            for (Post post : page.getPosts()) {
                if (post.getFile_url() != null) {
                    isAllPostsDeleted = false;
                    break;
                }
            }
            if (isAllPostsDeleted) {
                return Constants.POOL_STATUS_ALL_DELETED;
            }
        }
        // pageId = null 始终返回POOL_STATUS_NEW用于初始mapLastTimePageId2PostMd5Li时跳过后面判断
        if (null == pageId || !mapLastTimePageId2ZipLinkNumInfo.keySet().contains(pageId)) {
            return Constants.POOL_STATUS_NEW; // 若上一次的empty和all deleted添加了Post也将任务是new
        }
        for (Post post : page.getPosts()) {
            // 只要有新md5则认为被修改, 仅含有被移除md5不会被认为修改
            if (!mapLastTimePageId2PostMd5Li.get(pageId).contains(post.getMd5())) {
                return Constants.POOL_STATUS_MODIFIED;
            }
        }
        return Constants.POOL_STATUS_NO_CHANGE;
    }

    public static Map<Integer, Set<String>> getMapLastTimePageId2PostMd5Li() {
        return mapLastTimePageId2PostMd5Li;
    }

    public static Map<Integer, Integer> getMapLastTimePageId2ZipLinkNumInfo() {
        return mapLastTimePageId2ZipLinkNumInfo;
    }

}
