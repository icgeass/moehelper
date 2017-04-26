package com.zeroq6.moehelper.fetcher.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.zeroq6.moehelper.bean.Page;
import com.zeroq6.moehelper.bean.Post;
import com.zeroq6.moehelper.fetcher.Fetcher;
import com.zeroq6.moehelper.resources.ResourcesHolder;
import com.zeroq6.moehelper.utils.MyLogUtils;
import com.zeroq6.moehelper.utils.MyPoolUtils;
import com.zeroq6.moehelper.utils.MyStringUtils;
import com.zeroq6.moehelper.bean.Pool;
import com.zeroq6.moehelper.log.impl.PoolLog;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;

/**
 * Pool页面的解析和资源存储
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public class PoolFetcher implements Fetcher {


    /**
     * https://yande.re/pool/show/6
     *
        <li><a href="/pool/zip/6?jpeg=1" onclick="if(!User.run_login_onclick(event)) return false; return true;">Download JPGs (49.7 MB)</a></li>
    <li class="advanced-editing"><a href="/pool/zip/6" onclick="if(!User.run_login_onclick(event)) return false; return true;">Download PNGs (59.8 MB)</a></li>
     */
    public final static String LINK_POOL_ZIP_SUFFIX_JPG = "?jpeg=1";

    public static Pattern pattern_zip_link = Pattern.compile("^(.*)(/pool/zip/[1-9][0-9]{0,})([?]jpeg=1)?(.*)$");
    private int pageId = -1;

    private Document doc = null;

    public PoolFetcher(int page_id, Document doc) {
        this.pageId = page_id;
        this.doc = doc;
    }

    static {
        PrevPoolCheckUtils.init();
    }

    @Override
    public void run() {
        try {
            // HTTP Status=404
            if (this.doc == null) {
                ResourcesHolder.readPageFailed();
                ResourcesHolder.getMapIdLog().put(this.pageId, new PoolLog(this.pageId));
                PoolLog.logPageCountByPageStatus(PoolLog.POOL_STATUS_NULL);
                MyLogUtils.error("Post #" + this.pageId + " read page failed, 404, page not found");
                return;
            }
            // HTTP Status=200
            PoolLog log = new PoolLog(this.pageId);
            ResourcesHolder.getMapIdLog().put(this.pageId, log);

            Page page = null;

            // 获得并解析json数据
            String[] arr = this.doc.html().split("\n");
            // id=2427 jsoup bug? httpclient
            for (int i = 0; i < arr.length; i++) {
                if (arr[i].contains("Post.register_resp")) {
                    String json = arr[i].substring(arr[i].indexOf("(") + 1, arr[i].lastIndexOf(")")).trim();
                    ResourcesHolder.getMapIdJson().put(this.pageId, json);
                    page = JSON.parseObject(json, Page.class);
                    break;
                }
            }
            List<String> liUrls = setPoolLogInfo(page, log, doc);
            String pageStatus = log.getStatus();
            PoolLog.logPageCountByPageStatus(pageStatus);
            if (page != null) {
                if (pageStatus.equals(PoolLog.POOL_STATUS_NEW) || pageStatus.equals(PoolLog.POOL_STATUS_MODIFIED) || pageStatus.equals(PoolLog.POOL_STATUS_NO_CHANGE)) {
                    PoolLog.getMapPageId2ZipLinkPoolAll().put(this.pageId, liUrls);
                }
                if (pageStatus.equals(PoolLog.POOL_STATUS_NEW) || pageStatus.equals(PoolLog.POOL_STATUS_MODIFIED)) {
                    PoolLog.getMapPageId2ZipLinkPoolUpdated().put(this.pageId, liUrls);
                }
                PoolLog.getMapPageId2PoolDescription().put(this.pageId, getPoolInfo(pageId, page, this.doc));
                ResourcesHolder.getMapIdPage().put(this.pageId, page);
            } else {
                ResourcesHolder.readPageFailed();
            }
        } catch (Exception e) {
            MyLogUtils.fatal("Pool #" + this.pageId + " read page failed, exception.", e);
        }

    }

    /**
     * 设置当前也面的日志, 并返回页面zip链接的List集合
     * 
     * @param page
     * @param log
     * @param doc
     * @return List<String>
     */
    private List<String> setPoolLogInfo(Page page, PoolLog log, Document doc) {
        List<String> re = new ArrayList<String>();
        // 设置Pool状态, 注意顺序, Null和No change不错误输出
        String pageStatus = PrevPoolCheckUtils.checkPageStatus(page, this.pageId);
        String poolName = null;
        if (page != null) {
            poolName = this.doc.getElementById("pool-show").getElementsByTag("h4").text().replace("Pool:", "").trim();
        }
        if (PoolLog.POOL_STATUS_NULL.equals(pageStatus)) {
            // MyLogUtils.warn("Pool #" + pageId + "  " + poolName + " ---->Null");
        } else if (PoolLog.POOL_STATUS_EMPTY.equals(pageStatus)) {
            MyLogUtils.warn("Pool #" + pageId + "  " + poolName + " ---->No Posts");
        } else if (PoolLog.POOL_STATUS_ALL_DELETED.equals(pageStatus)) {
            MyLogUtils.warn("Pool #" + pageId + "  " + poolName + " ---->All Posts were Deleted");
        } else if (PoolLog.POOL_STATUS_NEW.equals(pageStatus)) {
            MyLogUtils.warn("Pool #" + pageId + "  " + poolName + " ---->New");
        } else if (PoolLog.POOL_STATUS_MODIFIED.equals(pageStatus)) {
            MyLogUtils.warn("Pool #" + pageId + "  " + poolName + " ---->Modified");
        } else if (PoolLog.POOL_STATUS_NO_CHANGE.equals(pageStatus)) {
            // MyLogUtils.warn("Pool #" + pageId + "  " + poolName + " ---->No change");
        } else {
            MyLogUtils.fatal("Unreachable Code");
        }
        log.setStatus(pageStatus);
        // Pool是否存在
        if (page != null) {
            log.setIsExist("true ");
        } else {
            log.setIsExist("false");
        }
        if (!pageStatus.equals(PoolLog.POOL_STATUS_NULL) && !pageStatus.equals(PoolLog.POOL_STATUS_EMPTY) && !pageStatus.equals(PoolLog.POOL_STATUS_ALL_DELETED)) {
            Elements elements = this.doc.getElementsByTag("a");
            for (Element element : elements) {
                if (pattern_zip_link.matcher(element.absUrl("href")).matches()) {
                    String zipUrl = element.absUrl("href").trim();
                    re.add(zipUrl);
                    log.setAllPackages(log.getAllPackages() + 1);
                    if (zipUrl.contains(LINK_POOL_ZIP_SUFFIX_JPG)) {
                        log.setJpegPackages(log.getJpegPackages() + 1);
                    } else{
                        log.setOriginalPackages(log.getOriginalPackages() + 1);
                    }
                }
            }
            // 校验数目是否正确
            if (hasPng(page)) {
                if (log.getJpegPackages() != 1 || log.getOriginalPackages() != 1 || log.getAllPackages() != 2) {
                    MyLogUtils.fatal("Pool #" + this.pageId + " error zip pack count info, find png pic in json data, the jpegPackages, originalPackages, allPackages count should be 1, 1, 2");
                }
            } else {
                if (log.getJpegPackages() != 0 || log.getOriginalPackages() != 1 || log.getAllPackages() != 1) {
                    MyLogUtils.fatal("Pool #" + this.pageId + " error zip pack count info, not find png pic in json data, the jpegPackages, originalPackages, allPackages count should be 0, 1, 1");
                }
            }
        }
        if (PoolLog.POOL_STATUS_NO_CHANGE.equals(log.getStatus())) {
            checkIfPoolOnlyPostsRemove(page, log);
        }
        return re;
    }

    /**
     * 返回当前Pool页面的简述字符串
     * 
     * @param pageId
     * @param page
     * @param doc
     * @return String
     */
    private String getPoolInfo(int pageId, Page page, Document doc) {
        StringBuffer re = new StringBuffer();
        String pageStatus = PrevPoolCheckUtils.checkPageStatus(page, this.pageId);
        // 先处理没有图片或所有图片被删除的情况
        if (PoolLog.POOL_STATUS_EMPTY.equals(pageStatus) || PoolLog.POOL_STATUS_ALL_DELETED.equals(pageStatus)) {
            re.append("Id = " + pageId + "\r\n");
            re.append("Name = " + doc.getElementById("pool-show").getElementsByTag("h4").text().replace("Pool:", "").trim() + "\r\n");
            if (PoolLog.POOL_STATUS_EMPTY.equals(pageStatus)) {
                re.append("Status = No Posts\r\n");
            } else {
                re.append("Status = All Posts were Deleted\r\n");
            }
            re.append("Description = " + doc.getElementById("pool-show").getElementsByTag("h4").get(0).nextElementSibling().html().replace("<br />", "\r\n\t      ") + "\r\n");
            re.append("\r\n");
            return re.toString();
        }
        // 处理其他Pool
        Pool currPool = getPoolByPageId(page, pageId);
        long[] zipPackageSize = getPoolSizeInByte(page);
        re.append("Id = " + currPool.getId() + "\r\n");
        re.append("Name = " + currPool.getName().replace("_", " ") + "\r\n");
        re.append("Post count = " + currPool.getPost_count() + "\r\n");
        if (hasPng(page)) {
            re.append("JPGs zip package size = " + MyStringUtils.getReadableFileSize(zipPackageSize[0]) + " (" + zipPackageSize[0] + " Bytes)" + "\r\n");
            re.append("PNGs zip package size = " + MyStringUtils.getReadableFileSize(zipPackageSize[1]) + " (" + zipPackageSize[1] + " Bytes)" + "\r\n");
        } else {
            re.append("zip package size = " + MyStringUtils.getReadableFileSize(zipPackageSize[1]) + " (" + zipPackageSize[1] + " Bytes)" + "\r\n");
        }
        re.append("User Id = " + currPool.getUser_id() + "\r\n");
        re.append("Created at = " + currPool.getCreated_at() + "\r\n");
        re.append("Updated at = " + currPool.getUpdated_at() + "\r\n");
        re.append("Description = " + currPool.getDescription().replace("\r\n", "\r\n\t      ") + "\r\n");
        re.append("\r\n");
        return re.toString();
    }

    /**
     * 判断Pool中是否含有PNG文件
     * 
     * @param page
     * @return boolean
     */
    private boolean hasPng(Page page) {
        for (Post post : page.getPosts()) {
            // null对应遍历到图片被删除, file_url为空
            if (post.getFile_url() != null && post.getFile_url().trim().endsWith(".png")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通过page, pageid得到Pool集合中对应的pool对象
     * 
     * @param page
     * @param pageId
     * @return Pool
     */
    private Pool getPoolByPageId(Page page, int pageId) {
        for (Pool pool : page.getPools()) {
            if (pool.getId() == pageId) {
                return pool;
            }
        }
        MyLogUtils.fatal("can not find pool by pageId " + pageId);
        return null;
    }

    /**
     * 通过page对象获得Pool的JPG和PNG的zip包大小
     * 
     * @param page
     * @return long[]
     */
    private long[] getPoolSizeInByte(Page page) {
        long bytesJpg = 0;
        long bytesPng = 0;
        for (Post post : page.getPosts()) {
            bytesJpg += post.getJpeg_file_size() == 0 ? post.getFile_size() : post.getJpeg_file_size();
            bytesPng += post.getFile_size();
        }
        return new long[] { bytesJpg, bytesPng };
    }

    private void checkIfPoolOnlyPostsRemove(Page page, PoolLog log) {
        if (!PoolLog.POOL_STATUS_NO_CHANGE.equals(log.getStatus())) {
            MyLogUtils.fatal("Unreachable Code");
        }
        int postsNumNow = page.getPosts().size();
        int postsNumPre = PrevPoolCheckUtils.getMapLastTimePageId2PostMd5List().get(pageId).size();
        if (postsNumPre != postsNumNow) {
            if(postsNumPre > postsNumNow){ // 因为是在no_change下
                MyLogUtils.debug("Pool #" + pageId + " only has posts removed, will be classified as no change pool, posts number affected " + MyStringUtils.insertBeforePlusOrMinus(postsNumNow - postsNumPre));
            }else{
                MyLogUtils.fatal("Unreachable Code");
            }
        }
        Integer zipNumStatusNow = log.getJpegPackages() + (log.getOriginalPackages() << 1);
        Integer zipNumStatusPre = PrevPoolCheckUtils.getMapLastTimePageId2ZipLinkCountInfo().get(pageId);
        if (!zipNumStatusPre.equals(zipNumStatusNow)) {
            int affectedZipNumJpg = MyPoolUtils.toPoolJpegCount(zipNumStatusNow) - MyPoolUtils.toPoolJpegCount(zipNumStatusPre);
            int affectedZipNumPng = MyPoolUtils.toPoolOriginalCount(zipNumStatusNow) - MyPoolUtils.toPoolOriginalCount(zipNumStatusPre);
            MyLogUtils.debug("Pool #" + pageId + " jpeg packages affected " + MyStringUtils.insertBeforePlusOrMinus(affectedZipNumJpg)
                    + ", original packages affected " + MyStringUtils.insertBeforePlusOrMinus(affectedZipNumPng)
                    + ", all packages affected " + MyStringUtils.insertBeforePlusOrMinus(affectedZipNumJpg + affectedZipNumPng));
        }
    }

}
