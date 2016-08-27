package loli.kanojo.moehelper.config;

import java.util.regex.Pattern;

/**
 * 常量
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 * @version moehelper - v1.0.7
 * @url https://github.com/icgeass/moehelper
 */
public class Constants {

    public final static String W_ROOT_DIR = Configuration.getConnParam("host");
    public final static String W_WRITE_DIR = "./" + W_ROOT_DIR + "/" + Configuration.getConnParam("linkType");
    public final static String W_FILE_NAME_PREFIX = Configuration.getConnParam("host") + "_-_" + Configuration.getConnParam("linkType") + "_" + Configuration.getBeginTime() + "_" + Configuration.getFromPage() + "_" + Configuration.getToPage();
    public final static String W_FULL_PATH_PREFIX = W_WRITE_DIR + "/" + W_FILE_NAME_PREFIX;

    // moe主机常量
    public final static String HOST_MOE = "yande.re";
    // kona主机常量
    public final static String HOST_KONA = "konachan.com";

    // Pool-JPG包链接后缀
    public final static String LINK_POOL_ZIP_SUFFIX_JPG = "?jpeg=1";
    // Pool-PNG包链接后缀
    // public final static String LINK_POOL_ZIP_SUFFIX_PNG = "https://yande.re/pool/zip/4161";


    public static Pattern pattern_zip_link = Pattern.compile("^(.*?)(/pool/zip/[1-9][0-9]{0,})(([?]jpeg=1)?)(.*?)$");

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
}
