package com.zeroq6.moehelper.fetcher.impl;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.zeroq6.moehelper.bean.Page;
import com.zeroq6.moehelper.bean.Post;
import com.zeroq6.moehelper.config.Configuration;
import com.zeroq6.moehelper.fetcher.Fetcher;
import com.zeroq6.moehelper.log.impl.PostLog;
import com.zeroq6.moehelper.resources.ResourcesHolder;
import com.zeroq6.moehelper.bean.Pool;

import com.zeroq6.moehelper.utils.MyLogUtils;
import org.apache.commons.lang3.StringUtils;
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


    private static Pattern pattern_md5 = Pattern.compile("^[0-9a-z]{32}$");


    /**
     * https://yande.re/post/show/370071
     *
      <div class="status-notice">
    This post was deleted.

      Reason: nah. MD5: 76993a9c9fb245648d072613d79621db
  </div>
     *
     */
    private static Pattern pattern_moe_post_deleted = Pattern.compile("^.*This post was deleted[.].*$", Pattern.CASE_INSENSITIVE);
    /**
     * https://yande.re/post/show/384769
     *
    <p>
        <a href="/post/show/383864?pool_id=4496">« Previous</a>
        <a href="/post/show/384770?pool_id=4496">Next »</a>
      This post is <span id="pool-seq-4496">#2</span> in the <a href="/pool/show/4496">Canvas (Morikura En)  - kigae</a> pool.

    </p>
     */

    private static Pattern pattern_moe_post_in_pool = Pattern.compile("^.*This post is.*in the <a href=\"/pool/show/[1-9][0-9]{0,}\">.*pool[.].*$", Pattern.CASE_INSENSITIVE);

    /**
     * https://yande.re/post/show/384769
     *  匹配后缀jpngifsw中3个字符，jpg,png,gif,swf
     *
        <li><a class="original-file-unchanged" id="png" href="https://files.yande.re/image/8ccda771321e2836477beb2d74b2a16e/yande.re%20384769%20canvas_%28morikura_en%29%20morikura_en%20pantsu%20seifuku%20sweater%20undressing.png">Download PNG (3.13 MB)</a>
        </li>
     */
    private static Pattern pattern_moe_post_url_exist = Pattern.compile("^.*https://((.*[.])?)yande[.]re/image/[0-9a-f]{32}/yande[.]re%20[1-9][0-9]{0,}%20.*[.][pngjifswe]{3,4}.*$", Pattern.CASE_INSENSITIVE);


    /**
     * http://konachan.com/post/show/1853
     *
      <div class="status-notice">
    This post was deleted.

      Reason: Very poor quality, rating:e loli. MD5: 8ba08d158c3aff442a4b5f0c1f5f9a42
  </div>
     */
    private static Pattern pattern_kona_post_deleted0 = Pattern.compile("^.*This post was deleted[.].*$", Pattern.CASE_INSENSITIVE);


    /**
     * http://konachan.com/post/show/240519
     *
      This post is <span id="pool-seq-419">#3</span> in the <a href="/pool/show/419">Sugina Miki - Kasou</a> pool.
     */
    private static Pattern pattern_kona_post_in_pool = Pattern.compile("^.*This post is.*in the <a href=\"/pool/show/[1-9][0-9]{0,}\">.*pool[.].*$", Pattern.CASE_INSENSITIVE);


    /**
     * http://konachan.com/post/show/240519
     *
        <li><a class="original-file-unchanged" href="//konachan.com/image/08ff068f6b8292f13f7ef981825b371c/Konachan.com%20-%20240519%20flowers_%28game%29%20innocent_grey%20sugina_miki.png" id="png">Download PNG (12.7 MB)</a>
        </li>
     */
    private static Pattern pattern_kona_post_url_exist = Pattern.compile("^.*//(.*[.])?konachan[.]com/image/[0-9a-f]{32}/Konachan[.]com.*[.][pngjgifswe]{3,4}.*$", Pattern.CASE_INSENSITIVE);


    // 当图片被删除时用于查找文件url的标签属性对
    private static Map<String, String> mapTag2Prop = new HashMap<String, String>();

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
                ResourcesHolder.getMapIdLog().put(this.pageId, new PostLog(this.pageId));
                PostLog.logPageCountByPageStatus(PostLog.POST_STATUS_404);
                MyLogUtils.error("Post #" + this.pageId + " read page failed. reason: 404, page not found");
                return;
            }
            PostLog log = new PostLog(this.pageId);
            ResourcesHolder.getMapIdLog().put(this.pageId, log);
            // ----------------通过json获得Page对象----------------
            Page page = null;
            String[] line = doc.html().split("\n");
            for (int i = 0; i < line.length; i++) {
                if (line[i].contains("Post.register_resp")) {
                    String json = line[i].substring(line[i].indexOf("(") + 1, line[i].lastIndexOf(")"));
                    ResourcesHolder.getMapIdJson().put(this.pageId, json);
                    page = JSON.parseObject(json, Page.class);
                    PostLog.logPageCountByPageStatus(PostLog.POST_STATUS_READ_BY_JSON);
                    break;
                }
            }
            // ----------------通过HTML校验得到的Page信息是否准确----------------
            // 与上面循环分开以便代码结构清晰
            boolean isDeletedByDoc = false;
            boolean isInPoolByDoc = false;
            for (int i = 0; i < line.length; i++) {
                if (isDeletedByDoc && isInPoolByDoc) {
                    break;
                }
                String currLine = line[i];
                if (pattern_moe_post_deleted.matcher(currLine).matches() || pattern_kona_post_deleted0.matcher(currLine).matches()) {
                    isDeletedByDoc = true;
                } else if (pattern_moe_post_in_pool.matcher(currLine).matches() || pattern_kona_post_in_pool.matcher(currLine).matches()) {
                    isInPoolByDoc = true;
                }
            }
            // -------------------校验信息---------------------------
            if (page == null) {
                if (!isDeletedByDoc) {
                    // ---Post是否删除校验1
                    MyLogUtils.fatal("Post #" + this.pageId + " match error. reason: post url was not deleted in html but no json data found");
                }
            } else {
                // moe 305487, 1楼 评论: This post was deleted......., OTL, debug TODO
                if (isDeletedByDoc) {
                    // ---Post是否删除校验2
                    MyLogUtils.fatal("Post #" + this.pageId + " match error. reason: post url was deleted in html but found json data");
                }
                if ((page.getPools().size() == 0 && isInPoolByDoc)) {
                    // ---Post是否在Pool校验1
                    MyLogUtils.fatal("Post #" + this.pageId + " match error. reason: post was in pool in html but pools.size() = 0 in json data");
                }
                if (page.getPools().size() != 0 && !isInPoolByDoc) {
                    // ---Post是否在Pool校验2
                    MyLogUtils.fatal("Post #" + this.pageId + " match error. reason: post was not in pool in html but pools.size() != 0 in json date");
                }
                // ---校验json数据中post文件链接是否为原图/image/
                String fileUrl = page.getPosts().get(0).getFile_url();
                if(fileUrl.startsWith("//")){ // konachan.com的json数据中链接更改为//开头(为了兼容http与https协议)，这里处理成当前协议对应
                    fileUrl = Configuration.getProtocol() + ":" + fileUrl;
                    page.getPosts().get(0).setFile_url(fileUrl);
                }
                if (!(fileUrl.startsWith("http") && fileUrl.contains("/image/"))) {
                    MyLogUtils.fatal("Post #" + this.pageId + " match error. reason: post url was not correct in json, url should be start with \"http\" and contains \"/image/\"");
                }
                boolean isPostUrlMatched = false;
                for (Element element : doc.getElementsByTag("a")) {
                    // 当图片被标记为删除时通过a标签不会获取到连接, 需要通过link meta标签
                    Pattern p = Configuration.HOST_MOE.equals(Configuration.getHost()) ? pattern_moe_post_url_exist : pattern_kona_post_url_exist;
                    if (p.matcher(element.absUrl("href")).matches()) {
                        // ---校验a标签中链接是否与page对象中的链接一致
                        // moe 294894、294895原本未登录只显示/sample/，评论区有回复原图，所以匹配上了，但是该原图链接与json里的原图链接不完全相同, debug TODO
                        if (!page.getPosts().get(0).getFile_url().equals(element.absUrl("href"))) {
                            MyLogUtils.fatal("Post #" + this.pageId + " match error. reason: different url was found. The url from json is " + page.getPosts().get(0).getFile_url() + ", while the url from html in tag a is " + element.absUrl("href"));
                        }
                        isPostUrlMatched = true;
                        // 防止有多个不匹配链接被找到而造成的重复打印 TODO
                        break;
                    }
                }
                // ---校验图片被删除, 则能不能找到匹配链接, 否则能找到匹配链接，校验html内的信息没有涉及json数据，实际可以忽略
                if (isDeletedByDoc == isPostUrlMatched) {
                    if (!isDeletedByDoc) {
                        // moe 304104等, 未登陆只显示 /sample/ 不显示 /image/ 导致在a标签中找不到原图链接, debug TODO
                        MyLogUtils.fatal("Post #" + this.pageId + " match error. reason: this post was not deleted in pattern but not found the matched url in html in tag a.");
                    } else {
                        // moe 305487, 1楼 评论 TODO
                        MyLogUtils.fatal("Post #" + this.pageId + " match error. reason: this post was deleted in pattern but found the matched url in html in tag a.");
                    }
                }
            }
            // ----------------如果未获取到Page对象的处理--------------------
            if (page == null) {
                MyLogUtils.warn("Post #" + this.pageId + " this post was flagged for deletion, try to read by html tag. Details: " + getReasonForDeleted(doc));
                lab: for (String key : mapTag2Prop.keySet()) {
                    String file_url = null;
                    String tags = null;
                    String md5 = null;
                    Pattern p = Configuration.HOST_MOE.equals(Configuration.getHost()) ? pattern_moe_post_url_exist : pattern_kona_post_url_exist;
                    for (Element element : doc.getElementsByTag(key)) {
                        if (p.matcher(element.absUrl(mapTag2Prop.get(key))).matches()) {
                            // https://yuno.yande.re/image/dfad8806778d80d9a6843be2278b909b/yande.re%20286457%20ayase_eli%20love_live%21%20seifuku%20sonoda_umi%20uehara.jpg
                            // 得到连接、md5和标签
                            file_url = element.absUrl(mapTag2Prop.get(key));
                            // 分割成的字符串数组最大长度6
                            String[] arr = file_url.split("/", 6);
                            tags = URLDecoder.decode(arr[arr.length - 1], "utf-8").replace("yande.re " + pageId + " ", "").replace("Konachan.com - " + pageId + " - ", "").replace("Konachan.com - " + pageId + " ", "").trim();
                            tags = tags.substring(0, tags.lastIndexOf("."));
                            md5 = arr[arr.length - 2];
                            // 校验
                            if(StringUtils.containsIgnoreCase(tags,"yande.re") || StringUtils.containsIgnoreCase(tags, "konachan.com")){
                                MyLogUtils.fatal("Post #" + this.pageId + " read by html, error tag format in url " + file_url + ", may be parse error.");
                            }
                            if(!pattern_md5.matcher(md5).matches()){
                                MyLogUtils.fatal("Post #" + this.pageId + " read by html, error md5 format in url " + file_url + ", may be parse error.");
                            }
                            // 设置page属性
                            // 被标记删除的图片但是在html中找到链接的page对象含有Post: id, url, md5, tags, created_at; Pool: id, name
                            page = new Page();
                            Post post = new Post();
                            post.setId(pageId).setFile_url(file_url).setMd5(md5).setTags(tags).setCreated_at(Configuration.DELETED_POST_CREATED_AT);
                            page.getPosts().add(post);
                            PostLog.logPageCountByPageStatus(PostLog.POST_STATUS_READ_BY_DOCUMENT);
                            // 设置是否在pool中属性
                            if (isInPoolByDoc) {
                                page.getPools().add(new Pool());
                                setPoolInfo(page, doc);
                                MyLogUtils.info("Post #" + this.pageId + " in the Pool #" + page.getPools().get(0).getId() + "  " + page.getPools().get(0).getName() + "  was deleted");
                            }
                            MyLogUtils.warn("Post #" + this.pageId + " read by html tag success");
                            // 只要任何一个tag中找到链接则跳出循环
                            break lab;
                        }
                    }
                }
            }
            // ---------------判断page是否为空----------------------
            // 为空则不添加page,不设置log,log前面已添加
            if (page == null) {
                PostLog.logPageCountByPageStatus(PostLog.POST_STATUS_NO_LINK_FOUND);
                MyLogUtils.error("Post #" + this.pageId + " read page failed. reason: no url was found");
                ResourcesHolder.readPageFailed();
            } else {
                setLog(page, log);
                ResourcesHolder.getMapIdPage().put(this.pageId, page);
            }
        } catch (Exception e) {
            PostLog.logPageCountByPageStatus(PostLog.POST_STATUS_EXCEPTION);
            MyLogUtils.fatal("Post #" + this.pageId + " read page failed, exception", e);
        }
    }

    // 设置log的post状态和是否包含在pool中, 最终page不为null时调用
    private void setLog(Page page, PostLog log) {
        if (page == null) {
            MyLogUtils.fatal("page can not be null");
        }
        Post post = page.getPosts().get(0);
        String[] url = { post.getFile_url(), post.getJpeg_url(), post.getSample_url() };
        for (int i = 0; i < url.length; i++) {
            // 由html-deleted设置时会出现null，因为这种情况下只设置了file_url
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
        return doc.getElementById("post-view").getElementsByClass("status-notice").get(0).text();
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
