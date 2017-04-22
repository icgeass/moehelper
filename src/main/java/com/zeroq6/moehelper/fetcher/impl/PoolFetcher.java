package com.zeroq6.moehelper.fetcher.impl;

import java.util.ArrayList;
import java.util.List;

import com.zeroq6.moehelper.bean.Page;
import com.zeroq6.moehelper.bean.Post;
import com.zeroq6.moehelper.config.Constants;
import com.zeroq6.moehelper.fetcher.Fetcher;
import com.zeroq6.moehelper.rt.Runtime;
import com.zeroq6.moehelper.utils.Kit;
import com.zeroq6.moehelper.utils.Logger;
import com.zeroq6.moehelper.bean.Pool;
import com.zeroq6.moehelper.log.impl.PoolLog;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;

/**
 * Pool页面的解析和资源存储
 * 将解析后的结果放入Rumtime和PoolLog中
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public class PoolFetcher implements Fetcher {

    private int pageId = -1;

    private Document doc = null;

    public PoolFetcher(int page_id, Document doc) {
        this.pageId = page_id;
        this.doc = doc;
    }

    static {
        PoolUpdatedValidator.init();
    }

    @Override
    public void run() {
        try {
            // HTTP Status=404
            if (this.doc == null) {
                Runtime.readPageFailed();
                Runtime.getMapid2log().put(this.pageId, new PoolLog(this.pageId));
                PoolLog.logPageNumByType(Constants.POOL_STATUS_NULL);
                Logger.error("Post #" + this.pageId + " read page failed, 404, page not found");
                return;
            }
            // HTTP Status=200
            PoolLog log = new PoolLog(this.pageId);
            Runtime.getMapid2log().put(this.pageId, log);

            Page page = null;

            // 获得并解析json数据
            String[] arr = this.doc.html().split("\n");
            // id=2427 jsoup bug? httpclient
            for (int i = 0; i < arr.length; i++) {
                if (arr[i].contains("Post.register_resp")) {
                    String json = arr[i].substring(arr[i].indexOf("(") + 1, arr[i].lastIndexOf(")")).trim();
                    Runtime.getMapid2jsondata().put(this.pageId, json);
                    page = JSON.parseObject(new String(json), Page.class);
                    break;
                }
            }
            List<String> liUrls = setPoolLogInfo(page, log, doc);
            if (page != null) {
                if (log.getStatus().equals(Constants.POOL_STATUS_NEW) || log.getStatus().equals(Constants.POOL_STATUS_MODIFIED) || log.getStatus().equals(Constants.POOL_STATUS_NO_CHANGE)) {
                    PoolLog.getMapPageId2ZipLinkPoolAll().put(this.pageId, liUrls);
                }
                if (log.getStatus().equals(Constants.POOL_STATUS_NEW) || log.getStatus().equals(Constants.POOL_STATUS_MODIFIED)) {
                    PoolLog.getMapPageId2ZipLinkPoolUpdated().put(this.pageId, liUrls);
                }
                PoolLog.getMapPageId2PoolDescription().put(this.pageId, getPoolInfo(pageId, page, this.doc));
                Runtime.getMapid2page().put(this.pageId, page);
            } else {
                Runtime.readPageFailed();
            }
        } catch (Exception e) {
            Logger.fatal("Pool #" + this.pageId + " read page failed, exception", e);
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
        String pageStatus = PoolUpdatedValidator.checkPageStatus(page, this.pageId);
        String poolName = null;
        if (page != null) {
            poolName = this.doc.getElementById("pool-show").getElementsByTag("h4").text().replace("Pool:", "").trim();
        }
        if (Constants.POOL_STATUS_NULL.equals(pageStatus)) {
            // Logger.warn("Pool #" + pageId + "  " + poolName + " ---->Null");
        } else if (Constants.POOL_STATUS_EMPTY.equals(pageStatus)) {
            Logger.warn("Pool #" + pageId + "  " + poolName + " ---->No Posts");
        } else if (Constants.POOL_STATUS_ALL_DELETED.equals(pageStatus)) {
            Logger.warn("Pool #" + pageId + "  " + poolName + " ---->All Posts were Deleted");
        } else if (Constants.POOL_STATUS_NEW.equals(pageStatus)) {
            Logger.warn("Pool #" + pageId + "  " + poolName + " ---->New");
        } else if (Constants.POOL_STATUS_MODIFIED.equals(pageStatus)) {
            Logger.warn("Pool #" + pageId + "  " + poolName + " ---->Modified");
        } else if (Constants.POOL_STATUS_NO_CHANGE.equals(pageStatus)) {
            // Logger.warn("Pool #" + pageId + "  " + poolName + " ---->No change");
        } else {
            Logger.fatal("Unreachable code");
        }
        log.setStatus(pageStatus);
        // Pool是否存在
        if (page != null) {
            log.setIsExist("true ");
        } else {
            log.setIsExist("false");
        }
        if (!pageStatus.equals(Constants.POOL_STATUS_NULL) && !pageStatus.equals(Constants.POOL_STATUS_EMPTY) && !pageStatus.equals(Constants.POOL_STATUS_ALL_DELETED)) {
            Elements eles = this.doc.getElementsByTag("a");
            for (Element element : eles) {
                if (Constants.pattern_zip_link.matcher(element.absUrl("href")).matches()) {
                    String zipUrl = element.absUrl("href").trim();
                    re.add(zipUrl);
                    log.setAllPackages(log.getAllPackages() + 1);
                    if (zipUrl.contains(Constants.LINK_POOL_ZIP_SUFFIX_JPG)) {
                        log.setJpegPackages(log.getJpegPackages() + 1);
                    } else{
                        log.setOriginalPackages(log.getOriginalPackages() + 1);
                    }
                }
            }
            // 校验数目是否正确
            if (hasPng(page)) {
                if (log.getJpegPackages() != 1 || log.getOriginalPackages() != 1 || log.getAllPackages() != 2) {
                    Logger.fatal("Error zip pack number info, the page id is " + this.pageId);
                }
            } else {
                if (log.getJpegPackages() != 0 || log.getOriginalPackages() != 1 || log.getAllPackages() != 1) {
                    Logger.fatal("Error zip pack number info, the page id is " + this.pageId);
                }
            }
        }
        PoolLog.logPageNumByType(pageStatus);
        if (log.getStatus().equals(Constants.POOL_STATUS_NO_CHANGE)) {
            checkIsPoolOnlyPostsRemove(page, log);
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
        StringBuffer re = new StringBuffer(50);
        String pageStatus = PoolUpdatedValidator.checkPageStatus(page, this.pageId);
        // 先处理没有图片或所有图片被删除的情况
        if (Constants.POOL_STATUS_EMPTY.equals(pageStatus) || Constants.POOL_STATUS_ALL_DELETED.equals(pageStatus)) {
            re.append("Id = " + pageId + "\r\n");
            re.append("Name = " + doc.getElementById("pool-show").getElementsByTag("h4").text().replace("Pool:", "").trim() + "\r\n");
            if (Constants.POOL_STATUS_EMPTY.equals(pageStatus)) {
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
        // Kit.getReadableFileSize(bytes0) + " (" + bytes0 + " Bytes)", Kit.getReadableFileSize(bytes1) + " (" + bytes1 + " Bytes)"
        if (hasPng(page)) {
            re.append("JPGs zip package size = " + Kit.getReadableFileSize(zipPackageSize[0]) + " (" + zipPackageSize[0] + " Bytes)" + "\r\n");
            re.append("PNGs zip package size = " + Kit.getReadableFileSize(zipPackageSize[1]) + " (" + zipPackageSize[1] + " Bytes)" + "\r\n");
        } else {
            re.append("zip package size = " + Kit.getReadableFileSize(zipPackageSize[1]) + " (" + zipPackageSize[1] + " Bytes)" + "\r\n");
        }
        re.append("User Id = " + currPool.getUser_id() + "\r\n");
        re.append("Created at = " + currPool.getCreated_at().replace("T", " ").replace("Z", "") + "\r\n");
        re.append("Updated at = " + currPool.getUpdated_at().replace("T", " ").replace("Z", "") + "\r\n");
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
        boolean re = false;
        for (Post post : page.getPosts()) {
            // null对应遍历到图片被删除, file_url为空
            if (post.getFile_url() != null && post.getFile_url().trim().endsWith(".png")) {
                re = true;
                break;
            }
        }
        return re;
    }

    /**
     * 通过page, pageid得到Pool集合中对应的pool对象
     * 
     * @param page
     * @param pageId
     * @return Pool
     */
    private Pool getPoolByPageId(Page page, int pageId) {
        Pool re = null;
        for (Pool pool : page.getPools()) {
            if (pool.getId() == pageId) {
                re = pool;
                break;
            }
        }
        return re;
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

    private void checkIsPoolOnlyPostsRemove(Page page, PoolLog log) {
        if (!Constants.POOL_STATUS_NO_CHANGE.equals(log.getStatus())) {
            Logger.fatal("call when pool status is no change");
        }
        int postsNumNow = page.getPosts().size();
        int postsNumPre = PoolUpdatedValidator.getMapLastTimePageId2PostMd5Li().get(pageId).size();
        if (postsNumPre != postsNumNow) {
            Logger.debug("Pool #" + pageId + " only has posts removed, will be classified as no change pool, posts number affected " + Kit.parseDigitWithSymbol(postsNumNow - postsNumPre, "-"));
        }
        Integer zipNumStatusNow = log.getJpegPackages() + (log.getOriginalPackages() << 1);
        Integer zipNumStatusPre = PoolUpdatedValidator.getMapLastTimePageId2ZipLinkNumInfo().get(pageId);
        if (!zipNumStatusPre.equals(zipNumStatusNow)) {
            int affectedZipNumJpg = (zipNumStatusNow & 0b01) - (zipNumStatusPre & 0b01);
            int affectedZipNumPng = (zipNumStatusNow >>> 1) - (zipNumStatusPre >>> 1);
            Logger.debug("Pool #" + pageId + " jpeg packages affected " + Kit.parseDigitWithSymbol(affectedZipNumJpg, "-") + ", original packages affected " + Kit.parseDigitWithSymbol(affectedZipNumPng, "-"));
        }
    }

}
