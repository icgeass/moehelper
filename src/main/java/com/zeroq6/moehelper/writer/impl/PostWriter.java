package com.zeroq6.moehelper.writer.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.zeroq6.moehelper.bean.Page;
import com.zeroq6.moehelper.log.Log;
import com.zeroq6.moehelper.log.impl.PostLog;
import com.zeroq6.moehelper.utils.MyLogUtils;
import com.zeroq6.moehelper.utils.MyDateUtils;
import com.zeroq6.moehelper.utils.MyStringUtils;
import com.zeroq6.moehelper.bean.Post;
import com.zeroq6.moehelper.config.Configuration;
import com.zeroq6.moehelper.resources.ResourcesHolder;
import com.zeroq6.moehelper.writer.Writer;

import org.apache.commons.io.FileUtils;

import com.alibaba.fastjson.JSON;

/**
 * Post数据写入, 先从Runtime和PostLog中获取数据存在实例字段中后进行写入
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public class PostWriter implements Writer {

    // 图片未被标记为删除和标记为删除但是从html标签中找到链接的page
    private List<Page> liPages = new ArrayList<Page>();
    // 同上
    private List<String> liMd5All = new ArrayList<String>();
    // 图片未被标记未删除并且在Pool中的链接
    private List<String> liMd5InPool = new ArrayList<String>();
    // 图片未被标记为删除并且不在Pool中的链接和图片标记为删除的在和不在Pool的链接
    private List<String> liMd5NoPool = new ArrayList<String>();
    private List<String> liLinkAll = new ArrayList<String>();
    private List<String> liLinkInPool = new ArrayList<String>();
    private List<String> liLinkNoPool = new ArrayList<String>();
    // 图片未被标记为删除的JSON
    private List<String> liJsonOkPost = new ArrayList<String>();
    // 图片标记为删除但是从html中找到链接的JSON
    private List<String> liJsonDeletedPost = new ArrayList<String>();
    private List<Log> liLog = new ArrayList<Log>();

    public PostWriter() {
    }

    @Override
    public void writeToFile() {
        // 当没有任何数据时也可以处理
        try {
            MyLogUtils.stdOut("开始写入文件");
            // 开始写入到文件
            init();
            validate();
            writeOkJson();
            writeDeletedPostJson();
            writeLinkNoPool();
            writeMD5NoPool();
            writeLinkAll();
            writeMD5All();
            writeLinkInPool();
            writeMD5InPool();
            writeLog();
            MyLogUtils.stdOut("文件写入完毕");
        } catch (Exception e) {
            MyLogUtils.fatal("写入文件错误", e);
        }
    }

    /**
     * 初始化集合数据存在实例中, 注意避免修改原始数据
     */
    private void init() {
        liPages.addAll(ResourcesHolder.getMapIdPage().values());
        Collections.sort(liPages, new Comparator<Page>() {
            @Override
            public int compare(Page o1, Page o2) {
                if (o1.getPosts().size() != 1 || o2.getPosts().size() != 1) {
                    MyLogUtils.fatal("posts大小必须为1");
                }
                return o1.getPosts().get(0).getId() - o2.getPosts().get(0).getId();
            }
        });
        for (int i = Configuration.getFromPage(); i <= Configuration.getToPage(); i++) {
            if (ResourcesHolder.getMapIdLog().keySet().contains(i)) {
                liLog.add(ResourcesHolder.getMapIdLog().get(i));
            } else {
                MyLogUtils.fatal("日志记录中不包含页面id, " + i);
            }
        }
        for (Page page : liPages) {
            Post post = page.getPosts().get(0);
            // 防止文件名中含有/导致文件名错误
            // https://yuno.yande.re/image/dc831cdb5ec31232bea18062d2847af8/yande.re%20281641%20karory.jpg
            // https://yande.re/jpeg/eced93c44306c8f95bda9e47cfc0d5ef/yande.re%20268050%20bikini%20cleavage%20fukahire_sanba%20ruinon%20swimsuits.jpg
            String url = post.getFile_url();
            int index = MyStringUtils.getInnerStrIndex(url, "/", 5);// 第5个"/"后为文件名
            String prefix = url.substring(0, index + 1);
            String suffix = url.substring(index + 1);
            if (post.getCreated_at() == Configuration.DELETED_POST_CREATED_AT) {
                liJsonDeletedPost.add(JSON.toJSONString(page));
            } else {
                liJsonOkPost.add(ResourcesHolder.getMapIdJson().get(post.getId()));
            }
            liMd5All.add(post.getMd5() + " *" + MyStringUtils.formatFileName(suffix));
            liLinkAll.add(MyStringUtils.formatUrlLink(prefix + suffix.replace("/", "_")));
            if (page.getPools().size() == 0 || post.getCreated_at() == Configuration.DELETED_POST_CREATED_AT) {
                // 被删除的Post, 不管是否在Pool内, 均归类在NoPool内
                liMd5NoPool.add(post.getMd5() + " *" + MyStringUtils.formatFileName(suffix));
                liLinkNoPool.add(MyStringUtils.formatUrlLink(prefix + suffix.replace("/", "_")));
            } else {
                liMd5InPool.add(post.getMd5() + " *" + MyStringUtils.formatFileName(suffix));
                liLinkInPool.add(MyStringUtils.formatUrlLink(prefix + suffix.replace("/", "_")));
            }
        }
    }

    private void validate() {
        int ok_json = PostLog.getPageCountByPageStatus(PostLog.POST_STATUS_READ_BY_JSON);
        int ok_doc = PostLog.getPageCountByPageStatus(PostLog.POST_STATUS_READ_BY_DOCUMENT);
        int post_404 = PostLog.getPageCountByPageStatus(PostLog.POST_STATUS_404);
        int post_exception = PostLog.getPageCountByPageStatus(PostLog.POST_STATUS_EXCEPTION);
        int post_no_url = PostLog.getPageCountByPageStatus(PostLog.POST_STATUS_NO_LINK_FOUND);
        int allPosts = Configuration.getToPage() - Configuration.getFromPage() + 1;
        int successPosts = liPages.size();// ok_doc 和 ok_json都含有
        int failedPosts = ResourcesHolder.getReadFailedPageCount();
        int[] post_pool_info_count = new int[4];
        fillingPostPoolInfoCount(post_pool_info_count);
        int json_post_no_pool = post_pool_info_count[0];
        int json_post_in_pool = post_pool_info_count[1];
        int doc_post_no_pool = post_pool_info_count[2];
        int doc_post_in_pool = post_pool_info_count[3];
        boolean isValidateOk = true;
        isValidateOk = isValidateOk && (allPosts == (successPosts + failedPosts));
        isValidateOk = isValidateOk && (allPosts == liLog.size());
        isValidateOk = isValidateOk && (successPosts == ok_json + ok_doc);
        isValidateOk = isValidateOk && (failedPosts == post_404 + post_exception + post_no_url);
        // post/show
        isValidateOk = isValidateOk && (ok_json == liJsonOkPost.size());
        isValidateOk = isValidateOk && (ok_doc == liJsonDeletedPost.size());
        isValidateOk = isValidateOk && (allPosts == ok_json + ok_doc + post_404 + post_exception + post_no_url);
        isValidateOk = isValidateOk && (ok_json == json_post_in_pool + json_post_no_pool);
        isValidateOk = isValidateOk && (ok_doc == doc_post_in_pool + doc_post_no_pool);
        isValidateOk = isValidateOk && (liLinkAll.size() == liMd5All.size());
        isValidateOk = isValidateOk && (liLinkInPool.size() == liMd5InPool.size());
        isValidateOk = isValidateOk && (liLinkNoPool.size() == liMd5NoPool.size());
        isValidateOk = isValidateOk && (liLinkAll.size() == successPosts);
        isValidateOk = isValidateOk && (liLinkInPool.size() == json_post_in_pool);
        isValidateOk = isValidateOk && (liLinkNoPool.size() == doc_post_in_pool + doc_post_no_pool + json_post_no_pool);
        if (!isValidateOk) {
            MyLogUtils.fatal("数据验证失败");
        }
    }

    private void writeDeletedPostJson() throws IOException {
        FileUtils.writeLines(new File(Writer.W_FULL_PATH_PREFIX + ".deleted_post.json"), "utf-8", liJsonDeletedPost, true);

    }

    private void writeOkJson() throws IOException {
        FileUtils.writeLines(new File(Writer.W_FULL_PATH_PREFIX + ".json"), "utf-8", liJsonOkPost, true);
    }

    private void writeMD5InPool() throws IOException {
        FileUtils.writeLines(new File(Writer.W_FULL_PATH_PREFIX + "_post_in_pool.md5"), "utf-8", liMd5InPool, false);
    }

    private void writeLinkInPool() throws IOException {
        FileUtils.writeLines(new File(Writer.W_FULL_PATH_PREFIX + "_post_in_pool.lst"), "utf-8", liLinkInPool, false);
    }

    private void writeLinkNoPool() throws IOException {
        FileUtils.writeLines(new File(Writer.W_FULL_PATH_PREFIX + "_post_no_pool.lst"), "utf-8", liLinkNoPool, false);
    }

    private void writeMD5NoPool() throws IOException {
        FileUtils.writeLines(new File(Writer.W_FULL_PATH_PREFIX + "_post_no_pool.md5"), "utf-8", liMd5NoPool, false);
    }

    private void writeMD5All() throws IOException {
        FileUtils.writeLines(new File(Writer.W_FULL_PATH_PREFIX + "_post_all.md5"), "utf-8", liMd5All, false);
    }

    private void writeLinkAll() throws IOException {
        FileUtils.writeLines(new File(Writer.W_FULL_PATH_PREFIX + "_post_all.lst"), "utf-8", liLinkAll, false);

    }

    private void writeLog() throws IOException {
        List<String> li = new ArrayList<String>();
        li.add(MyDateUtils.formatCurrentTime());
        li.add("统计: ");
        String userOption = "";
        for (String string : Configuration.getInputParams()) {
            userOption += string + " ";
        }
        userOption = userOption.trim();
        li.add("");
        li.add("用户参数: " + userOption);
        li.add("页面总数: " + (Configuration.getToPage() - Configuration.getFromPage() + 1));
        li.add("读取成功: " + liPages.size());
        li.add("读取失败: " + ResourcesHolder.getReadFailedPageCount());
        li.add("JSON数据条数: " + liJsonOkPost.size());
        li.add("详细计数: ok-json=" + PostLog.getPageCountByPageStatus(PostLog.POST_STATUS_READ_BY_JSON) + ", ok-doc-post-deleted=" + PostLog.getPageCountByPageStatus(PostLog.POST_STATUS_READ_BY_DOCUMENT) + ", 404=" + PostLog.getPageCountByPageStatus(PostLog.POST_STATUS_404) + ", exception=" + PostLog.getPageCountByPageStatus(PostLog.POST_STATUS_EXCEPTION) + ", no url=" + PostLog.getPageCountByPageStatus(PostLog.POST_STATUS_NO_LINK_FOUND));
        li.add("Pool信息: " + fillingPostPoolInfoCount(new int[4]));
        li.add("文件写入情况: JSON条数=" + liJsonOkPost.size() + ", 写入URL条数=" + liLinkNoPool.size() + ", 写入MD5条数=" + liMd5NoPool.size() + ", 记录Log条数=" + liLog.size());
        li.add("");
        li.add("Post id   Status   Read OK In Pool");
        li.add("------------------------------------");
        for (Log log : liLog) {
            PostLog postlog = (PostLog) log;
            String details = "# %6d  |" + Integer.toBinaryString(postlog.getPostStatus()).substring(1) + "  |" + postlog.getIsReadOk() + "    |" + postlog.getIsInPool();
            li.add(String.format(details, postlog.getId()));
        }
        FileUtils.writeLines(new File(Writer.W_FULL_PATH_PREFIX + "_post.log"), "utf-8", li, false);

    }

    private String fillingPostPoolInfoCount(int[] arr) {
        for (Page page : liPages) {
            Post post = page.getPosts().get(0);
            if (post.getCreated_at() != Configuration.DELETED_POST_CREATED_AT) {
                if (page.getPools().size() == 0) {
                    arr[0]++;// 未被删除中不含Pool
                } else {
                    arr[1]++;// 未被删除中含Pool
                }
            } else {
                if (page.getPools().size() == 0) {
                    arr[2]++;// 被删除中不含pool
                } else {
                    arr[3]++;// 被删除中含pool
                }
            }
        }
        return "ok-json=" + arr[0] + "+" + arr[1] + ", ok-doc-post-deleted=" + arr[2] + "+" + arr[3];
    }
}
