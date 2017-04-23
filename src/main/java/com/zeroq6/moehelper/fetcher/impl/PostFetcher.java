package com.zeroq6.moehelper.fetcher.impl;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.zeroq6.moehelper.bean.Page;
import com.zeroq6.moehelper.bean.Post;
import com.zeroq6.moehelper.config.Configuration;
import com.zeroq6.moehelper.config.Constants;
import com.zeroq6.moehelper.fetcher.Fetcher;
import com.zeroq6.moehelper.log.impl.PostLog;
import com.zeroq6.moehelper.resources.ResourcesHolder;
import com.zeroq6.moehelper.utils.Logger;
import com.zeroq6.moehelper.bean.Pool;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;

/**
 * Post页面的解析并将解析结果存入Runtime和PostLog
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public class PostFetcher implements Fetcher {

    // 当图片被删除时用于查找文件url的标签属性对
    private static Map<String, String> mapTag2Prop = new HashMap<String, String>();

    // 正则
    private static Pattern pattern_moe_post_deleted = Pattern.compile("^.*?This post was deleted[.].*?$", Pattern.CASE_INSENSITIVE);
    private static Pattern pattern_moe_post_in_pool = Pattern.compile("^.*?This post is.*?in the <a href=\"/pool/show/[1-9][0-9]{0,}\">.*?pool[.].*?$", Pattern.CASE_INSENSITIVE);
    private static Pattern pattern_moe_post_url_exist = Pattern.compile("^.*?https://((.*?[.])?)yande[.]re/image/[0-9a-f]{32}/yande[.]re%20[1-9][0-9]{0,}%20.*?[.][jpngifsw]{3}.*?$", Pattern.CASE_INSENSITIVE);
    private static Pattern pattern_kona_post_deleted0 = Pattern.compile("^.*?<p>This post does not exist[.]</p>.*?$", Pattern.CASE_INSENSITIVE);
    private static Pattern pattern_kona_post_deleted1 = pattern_moe_post_deleted;
    // @see http://zh.wikipedia.org/wiki/%E8%B6%85%E6%96%87%E6%9C%AC%E5%92%96%E5%95%A1%E5%A3%B6%E6%8E%A7%E5%88%B6%E5%8D%8F%E8%AE%AE
    // http://konachan.com/post/show/1853
    private static Pattern pattern_kona_post_deleted2 = Pattern.compile("^.*?<p>HTTP/1.1 418.*?</p>.*?$", Pattern.CASE_INSENSITIVE);
    private static Pattern pattern_kona_post_in_pool = pattern_moe_post_in_pool;
    private static Pattern pattern_kona_post_url_exist = Pattern.compile("^.*?http://((.*?[.])?)konachan[.]com/image/[0-9a-f]{32}/Konachan[.]com.*?[.][pngjgifswe]{3,4}.*?$", Pattern.CASE_INSENSITIVE);

    static {
        mapTag2Prop.put("link", "href");
        mapTag2Prop.put("meta", "content");
    }

    private int pageId = -1;

    private Document doc = null;

    public PostFetcher(int pageId, Document doc) {
        this.pageId = pageId;
        this.doc = doc;
    }

    @Override
    public void run() {
        try {
            if (doc == null) {
                ResourcesHolder.readPageFailed();
                ResourcesHolder.getMapid2log().put(this.pageId, new PostLog(this.pageId));
                PostLog.logPageNumByType(Constants.POST_STATUS_404);
                Logger.error("Post #" + this.pageId + " read page failed. Reason: 404, page not found");
                return;
            }
            PostLog log = new PostLog(this.pageId);
            ResourcesHolder.getMapid2log().put(this.pageId, log);
            // ----------------通过json获得Page对象----------------
            Page page = null;
            String[] line = doc.html().split("\n");
            for (int i = 0; i < line.length; i++) {
                if (line[i].contains("Post.register_resp")) {
                    String json = line[i].substring(line[i].indexOf("(") + 1, line[i].lastIndexOf(")"));
                    ResourcesHolder.getMapid2jsondata().put(this.pageId, json);
                    page = JSON.parseObject(new String(json), Page.class);
                    PostLog.logPageNumByType(Constants.POST_STATUS_READ_BY_JSON);
                    break;
                }
            }
            // ----------------通过docment校验得到的Page信息是否准确----------------
            // 与上面循环分开以便代码结构清晰
            boolean isDeletedByDoc = false;
            boolean isInPoolByDoc = false;
            for (int i = 0; i < line.length; i++) {
                if (isDeletedByDoc && isInPoolByDoc) {
                    break;
                }
                if (pattern_moe_post_deleted.matcher(line[i]).matches() || pattern_kona_post_deleted0.matcher(line[i]).matches() || pattern_kona_post_deleted1.matcher(line[i]).matches() || pattern_kona_post_deleted2.matcher(line[i]).matches()) {
                    isDeletedByDoc = true;
                } else if (pattern_moe_post_in_pool.matcher(line[i]).matches() || pattern_kona_post_in_pool.matcher(line[i]).matches()) {
                    isInPoolByDoc = true;
                }
            }
            // -------------------校验信息---------------------------
            if (page == null) {
                if (!isDeletedByDoc) {
                    // ---Post是否删除校验1
                    Logger.fatal("Post #" + this.pageId + " match error. Reason: post url was not deleted in document");
                }
            } else {
                // moe 305487, 1楼 评论: This post was deleted......., OTL, debug
                if (isDeletedByDoc) {
                    // ---Post是否删除校验2
                    Logger.debug("Post #" + this.pageId + " match error. Reason: post url was deleted in document");
                }
                if ((page.getPools().size() == 0 && isInPoolByDoc)) {
                    // ---Post是否在Pool校验1
                    Logger.fatal("Post #" + this.pageId + " match error. Reason: post was in pool in document");
                }
                if (page.getPools().size() != 0 && !isInPoolByDoc) {
                    // ---Post是否在Pool校验2
                    Logger.fatal("Post #" + this.pageId + " match error. Reason: post was not in pool in document");
                }
                // ---校验json数据中post文件链接是否为原图/image/
                String fileUrl = page.getPosts().get(0).getFile_url();
                if(fileUrl.startsWith("//")){
                    fileUrl = Configuration.getConnParam("protocol") + ":" + fileUrl;
                    page.getPosts().get(0).setFile_url(fileUrl);
                }
                if (!(fileUrl.startsWith("http") && fileUrl.contains("/image/"))) {
                    Logger.fatal("Post #" + this.pageId + " match error. Reason: post url was not correct in json");
                }
                boolean isPostUrlMatched = false;
                for (Element element : doc.getElementsByTag("a")) {
                    // 当图片被标记为删除时通过a标签不会获取到连接, 需要通过link meta标签
                    Pattern p = Constants.HOST_MOE.equals(Configuration.getConnParam("host")) ? pattern_moe_post_url_exist : pattern_kona_post_url_exist;
                    if (p.matcher(element.absUrl("href")).matches()) {
                        // ---校验a标签中链接是否与page对象中的链接一致
                        // moe 294894、294895原本未登录只显示/sample/，评论区有回复原图，所以匹配上了，但是该原图链接与json里的原图链接不完全相同, debug
                        if (!page.getPosts().get(0).getFile_url().equals(element.absUrl("href"))) {
                            Logger.debug("Post #" + this.pageId + " match error. Reason: different url was found. The url from json is " + page.getPosts().get(0).getFile_url() + ", while the url from document in tag a is " + element.absUrl("href"));
                        }
                        isPostUrlMatched = true;
                        // 防止有多个不匹配链接被找到而造成的重复打印
                        break;
                    }
                }
                // ---校验图片被删除, 则能不能找到匹配链接, 否则能找到匹配链接
                if (isDeletedByDoc == isPostUrlMatched) {
                    if (!isDeletedByDoc) {
                        // moe 304104等, 未登陆只显示 /sample/ 不显示 /image/ 导致在a标签中找不到原图链接, debug
                        Logger.debug("Post #" + this.pageId + " match error. Reason: this post was not deleted in pattern but not found the matched url in document in tag a.");
                    } else {
                        // moe 305487, 1楼 评论
                        Logger.debug("Post #" + this.pageId + " match error. Reason: this post was deleted in pattern but found the matched url in document in tag a.");
                    }
                }
            }
            // ----------------如果未获取到Page对象的处理--------------------
            if (page == null) {
                Logger.warn("Post #" + this.pageId + " this post was flagged for deletion, now try to read by html tag. Details: " + getReasonForDeleted(doc));
                lab: for (String key : mapTag2Prop.keySet()) {
                    String file_url = null;
                    String tags = null;
                    String md5 = null;
                    Pattern p = Constants.HOST_MOE.equals(Configuration.getConnParam("host")) ? pattern_moe_post_url_exist : pattern_kona_post_url_exist;
                    for (Element element : doc.getElementsByTag(key)) {
                        if (p.matcher(element.absUrl(mapTag2Prop.get(key))).matches()) {
                            // https://yuno.yande.re/image/dfad8806778d80d9a6843be2278b909b/yande.re%20286457%20ayase_eli%20love_live%21%20seifuku%20sonoda_umi%20uehara.jpg
                            // 得到连接、md5和标签
                            file_url = element.absUrl(mapTag2Prop.get(key));
                            // 分割成的字符串数组最大长度6
                            String[] arr = file_url.split("/", 6);
                            tags = URLDecoder.decode(arr[arr.length - 1], "utf-8").replace("yande.re " + pageId + " ", "").replace("Konachan.com - " + pageId + " - ", "").replace("Konachan.com - " + pageId + " ", "").trim();
                            tags = tags.substring(0, tags.length() - 4);
                            md5 = arr[arr.length - 2];
                            // 设置page属性
                            // 被标记删除的图片但是在html中找到链接的page对象含有Post: id, url, md5, tags, created_at; Pool: id, name
                            page = new Page();
                            Post post = new Post();
                            post.setId(pageId).setFile_url(file_url).setMd5(md5).setTags(tags).setCreated_at(Configuration.DELETED_POST_CREATED_AT);
                            page.getPosts().add(post);
                            PostLog.logPageNumByType(Constants.POST_STATUS_READ_BY_DOCUMENT);
                            // 设置是否在pool中属性
                            if (isInPoolByDoc) {
                                page.getPools().add(new Pool());
                                setPoolInfo(page, doc);
                                Logger.info("Post #" + this.pageId + " in the Pool #" + page.getPools().get(0).getId() + "  " + page.getPools().get(0).getName() + "  was deleted");
                            }
                            Logger.warn("Post #" + this.pageId + " read by html tag success");
                            // 只要任何一个tag中找到链接则跳出循环
                            break lab;
                        }
                    }
                }
            }
            // ---------------判断page是否为空----------------------
            // 为空则不添加page,不设置log,log前面已添加
            if (page == null) {
                PostLog.logPageNumByType(Constants.POST_STATUS_NO_LINK_FOUND);
                Logger.error("Post #" + this.pageId + " read page failed. Reason: no url was found");
                ResourcesHolder.readPageFailed();
            } else {
                setLog(page, log);
                ResourcesHolder.getMapid2page().put(this.pageId, page);
            }
        } catch (Exception e) {
            Logger.fatal("Post #" + this.pageId + " read page failed, exception", e);
        }
    }

    // 设置log的post状态和是否包含在pool中, 最终page不为null时调用
    private void setLog(Page page, PostLog log) {
        if (page == null) {
            throw new UnsupportedOperationException();
        }
        Post post = page.getPosts().get(0);
        String[] url = { post.getFile_url(), post.getJpeg_url(), post.getSample_url() };
        for (int i = 0; i < url.length; i++) {
            // 由document-deleted设置时会出现null
            if (url[i] == null) {
                url[i] = "";
            }
        }
        log.setIsReadOk("yes");
        if (page.getPools().size() != 0) {
            log.setIsInPool("true");
        } else {
            log.setIsInPool("false");
        }
        for (int i = 0; i < url.length; i++) {
            if (i == 0 && url[i].contains("/image/")) {
                log.setPostStatus(log.getPostStatus() + 0b100000);
                if (url[i].endsWith(".png")) {
                    log.setPostStatus(log.getPostStatus() + 0b10000);
                }
            } else if (i == 1 && url[i].contains("/jpeg/")) {
                log.setPostStatus(log.getPostStatus() + 0b1000);
                if (url[i].endsWith(".jpg")) {
                    log.setPostStatus(log.getPostStatus() + 0b100);
                }
            } else if (i == 2 && url[i].contains("/sample/")) {
                log.setPostStatus(log.getPostStatus() + 0b10);
                if (url[i].endsWith(".jpg")) {
                    log.setPostStatus(log.getPostStatus() + 0b1);
                }
            }
        }
    }

    // 当页面图片被删除时, 得到图片被删除的原因
    private String getReasonForDeleted(Document doc) {
        String re = null;
        try {
            re = doc.getElementById("post-view").getElementsByClass("status-notice").get(0).text();
        } catch (Exception e) {
            // @see http://konachan.com/post/show/1853
            re = "HTTP/1.1 418: I'm a teapot";
        }
        return re;
    }

    // 当页面图片被删除时, 如果存在设置Post对应Pool的name和id, 只会设置最后一个Pool信息(Pool有多个情况下)
    private void setPoolInfo(Page page, Document doc) {
        Elements classNotice = doc.getElementById("post-view").getElementsByClass("status-notice");
        Elements tagA = classNotice.get(classNotice.size() - 1).getElementsByTag("p").get(0).getElementsByTag("a");
        page.getPools().get(0).setName(tagA.get(tagA.size() - 1).text());
        String attr = tagA.get(tagA.size() - 1).attr("href");
        page.getPools().get(0).setId(Integer.valueOf(attr.substring(attr.lastIndexOf("/") + 1).trim()));
    }
}
