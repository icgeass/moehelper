package com.zeroq6.moehelper.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.zeroq6.moehelper.fetcher.Fetcher;
import com.zeroq6.moehelper.writer.Writer;
import com.zeroq6.moehelper.writer.impl.PoolWriter;
import com.zeroq6.moehelper.writer.impl.PostWriter;
import com.zeroq6.moehelper.conn.ConnManager;
import com.zeroq6.moehelper.fetcher.impl.PoolFetcher;
import com.zeroq6.moehelper.fetcher.impl.PostFetcher;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;

/**
 * 初始化、运行配置
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public class Configuration {

    // 最大pageId范围限制
    public final static int MAX_ID_RANGE = 10000;

    // ConnManager中连接限制
    public final static int DEFAULT_MAX_THREAD_NUM = 5;

    // 为Post页面图片被删除但是在文档中找到链接的图片创建json数据时的默认创建时间
    // 考虑之前兼容设定，不修改
    public final static long DELETED_POST_CREATED_AT = -10086;

    // 用户输入参数
    private static String[] userInputParams = null;

    // ConnThread的连接参数
    private static Map<String, String> connParams = new HashMap<String, String>();

    // 配置是否初始化
    private static boolean isInited = false;

    // 记录程序启动时间, 用于文件名显示
    private static String beginTime = (new SimpleDateFormat("yyMMddHHmmss")).format(Calendar.getInstance().getTime());

    // 起始Id
    private static int fromPage = -1;

    // 结束Id
    private static int toPage = -1;

    static {
        connParams.put("host", "yande.re");
        connParams.put("linkType", "post");
        connParams.put("protocol", "https");
        connParams.put("port", "443");
        connParams.put("maxTotalConnection", "10");
        connParams.put("maxPerSiteConnection", "10");
    }

    private Configuration() {
    }

    /**
     * 得到起始页码
     * 
     * @return int
     */
    public static int getFromPage() {
        return fromPage;
    }

    /**
     * 得到终止页码
     * 
     * @return int
     */
    public static int getToPage() {
        return toPage;
    }

    /**
     * 得到文件写入对象
     * 
     * @return Writer
     */
    public static Writer getWriter() {
        if ("post".equals(connParams.get("linkType"))) {
            return new PostWriter();
        } else if ("pool".equals(connParams.get("linkType"))) {
            return new PoolWriter();
        } else {
            throw new RuntimeException("initial parameter is wrong.");
        }
    }

    /**
     * 得到解析线程对象
     * 
     * @param pageId
     * @param doc
     * @return Fetcher
     */
    public static Fetcher getFetcher(int pageId, Document doc) {
        if ("post".equals(connParams.get("linkType"))) {
            return new PostFetcher(pageId, doc);
        } else if ("pool".equals(connParams.get("linkType"))) {
            return new PoolFetcher(pageId, doc);
        } else {
            throw new RuntimeException("initial parameter is wrong.");
        }
    }

    /**
     * 返回程序启动时间字符串, 格式为yyMMddHHmmss, 用于添加到文件名
     * 
     * @return String
     */
    public static String getBeginTime() {
        return beginTime;
    }

    /**
     * 初始化配置 环境
     * 
     * @param args
     * @throws Exception
     * @return void
     */
    public synchronized static void init(String[] args) throws Exception {
        if (isInited) {
            System.out.println("illegal operation, the method init can be called only once.");
            System.exit(-1);
        }
        if (!procUserInput(args)) {
            System.out.println("usage: \r\n\tjava -jar <jarfile> <fromindex> <toindex> [<--Post|--Pool> [--moe|--kona]]");
            System.exit(-1);
        }
        userInputParams = args;
        fromPage = Integer.valueOf(args[0]);
        toPage = Integer.valueOf(args[1]);
        ConnManager.getInstance().putRange(fromPage, toPage);
        // 标准重定向
        File f = new File(Constants.W_FULL_PATH_PREFIX + "_stdout.txt");
        FileUtils.write(f, "", "utf-8");
        System.setOut(new PrintStream(new FileOutputStream(f), true, "utf-8"));
        // 错误重定向
        f = new File(Constants.W_FULL_PATH_PREFIX + "_stderr.txt");
        FileUtils.write(f, "", "utf-8");
        System.setErr(new PrintStream(new FileOutputStream(f), true, "utf-8"));
        // 提示文件
        FileUtils.write(new File("./" + Constants.W_ROOT_DIR + "/NOTICE"), "Private Use, No Public Distribution", "utf-8", false);
        isInited = true;
    }

    /**
     * 得到key对应的连接参数, 不存在该key将会抛出运行时异常
     * 
     * @param key
     * @return String
     * @throws RuntimeException
     */
    public static String getConnParam(String key) {
        if (connParams.get(key) == null) {
            throw new RuntimeException("the key " + key + "can not be found.");
        }
        return connParams.get(key);
    }

    /**
     * 得到用户传人参数数组
     * 
     * @return String[]
     */
    public static String[] getUserInputParams() {
        return userInputParams;
    }

    /**
     * 用户参数检查, 返回false 如果错误, true 如果正确
     * 
     * @param args
     * @return boolean
     */
    private static boolean procUserInput(String[] args) {
        if (args.length < 2 || args.length > 4) {
            return false;
        }
        // 检查指定的页面id的合法性
        if (Pattern.matches("^[1-9][0-9]{0,5}$", args[0]) && Pattern.matches("^[1-9][0-9]{0,5}$", args[1])) {
            // 检查页面范围的合法性
            if (Integer.valueOf(args[0]) > Integer.valueOf(args[1]) || Integer.valueOf(args[1]) - Integer.valueOf(args[0]) + 1 > MAX_ID_RANGE) {
                return false;
            }
        } else {
            return false;
        }
        // 2参数
        if (args.length == 2) {
            return true;
        }
        // 3, 4个参数
        if (args.length > 2) {
            if ("--Pool".equalsIgnoreCase(args[2])) {
                connParams.put("linkType", "pool");

            } else if (!"--Post".equalsIgnoreCase(args[2])) {
                System.out.println("illegal parameter: " + args[2]);
                return false;
            }
            if (args.length == 4) {
                if ("--kona".equalsIgnoreCase(args[3])) {
                    if ("pool".equals(connParams.get("linkType"))) {
                        return false;
                    }
                    connParams.put("host", "konachan.com");
                    connParams.put("protocol", "http");
                    connParams.put("port", "80");
                } else if (!"--moe".equalsIgnoreCase(args[3])) {
                    System.out.println("illegal parameter: " + args[3]);
                    return false;
                }
            }
        }
        return true;
    }

}
