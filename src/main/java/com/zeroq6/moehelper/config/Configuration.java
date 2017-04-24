package com.zeroq6.moehelper.config;

import java.io.*;
import java.util.Date;
import java.util.regex.Pattern;

import com.zeroq6.moehelper.fetcher.Fetcher;
import com.zeroq6.moehelper.utils.MyDateUtils;
import com.zeroq6.moehelper.utils.MyLogUtils;
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

    public final static String HOST_MOE = "yande.re";
    public final static String HOST_KONA = "konachan.com";
    public final static String POST = "post";
    public final static String POOL = "pool";


    public final static int MAX_ID_RANGE = 10000;
    public final static long DELETED_POST_CREATED_AT = -10086;

    //////

    private static String host = null;

    private static String linkType = null;

    private static String protocol = null;

    private static int port = -1;

    /////


    // 其他初始配置
    private static boolean init = false;
    private static String[] inputParams = null;
    private static String beginTime = MyDateUtils.format(new Date(), "yyMMddHHmmss");
    private static int fromPage = -1;
    private static int toPage = -1;



    private Configuration() {
    }

    static {
        Configuration.linkType = POST;
        Configuration.host = HOST_MOE;
        Configuration.protocol = "https";
        Configuration.port = 443;
    }


    /**
     * 得到文件写入对象
     * 
     * @return Writer
     */
    public static Writer newWriter() {
        if (POST.equals(getLinkType())) {
            return new PostWriter();
        } else if (POOL.equals(getLinkType())) {
            return new PoolWriter();
        } else {
            MyLogUtils.fatal("Unreachable Code");
            return null;
        }
    }

    /**
     * 得到解析线程对象
     * 
     * @param pageId
     * @param doc
     * @return Fetcher
     */
    public static Fetcher newFetcher(int pageId, Document doc) {
        if (POST.equals(getLinkType())) {
            return new PostFetcher(pageId, doc);
        } else if (POOL.equals(getLinkType())) {
            return new PoolFetcher(pageId, doc);
        } else {
            MyLogUtils.fatal("Unreachable Code");
            return null;
        }
    }

    /**
     * 初始化配置 环境
     * 
     * @param args
     * @throws Exception
     * @return void
     */
    public synchronized static void init(String[] args) throws Exception {
        if (init) {
            MyLogUtils.fatal("只能初始化一次");
        }
        if (!checkParams(args)) {
            System.out.println("usage: \r\n\tjava -jar <jarfile> <fromindex> <toindex> [<--Post|--Pool> [--moe|--kona]]");
            System.exit(-1);
        }
        System.setProperty("line.separator", "\r\n");
        Configuration.inputParams = args;
        Configuration.fromPage = Integer.valueOf(args[0]);
        Configuration.toPage = Integer.valueOf(args[1]);
        ConnManager.getInstance().putRange(fromPage, toPage);
        // 错误重定向
        File f = new File(Writer.W_FULL_PATH_PREFIX + "_stderr.txt");
        System.setErr(new PrintStream(new FileOutputStream(f), true, "utf-8"));
        f = new File(Writer.W_FULL_PATH_PREFIX + "_stdout.txt");
        // 标准重定向
        System.setOut(new PrintStream(new FileOutputStream(f), true, "utf-8"));
        //
        MyLogUtils.info("标准错误输出重定向");
        MyLogUtils.stdOut("标准输出重定向");
        // 提示文件
        FileUtils.write(new File("./" + Writer.W_ROOT_DIR + "/NOTICE"), "Private Use Only", "utf-8", false);
        init = true;
    }

    /**
     * 用户参数检查, 返回false 如果错误, true 如果正确
     * 
     * @param args
     * @return boolean
     */
    private static boolean checkParams(String[] args) {
        if (null == args || args.length < 2 || args.length > 4) {
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
                Configuration.linkType = POOL;
            } else if (!"--Post".equalsIgnoreCase(args[2])) {
                return false;
            }
            if (args.length == 4) {
                if ("--kona".equalsIgnoreCase(args[3])) {
                    if (POOL.equals(getLinkType())) {
                        return false;
                    }
                    Configuration.host = HOST_KONA;
                    Configuration.protocol = "http";
                    Configuration.port = 80;
                } else if (!"--moe".equalsIgnoreCase(args[3])) {
                    return false;
                }
            }
        }
        return true;
    }




    public static String getHost() {
        return host;
    }


    public static String getLinkType() {
        return linkType;
    }


    public static String getProtocol() {
        return protocol;
    }

    public static int getPort() {
        return port;
    }



    /////////////////////////////////

    public static String[] getInputParams() {
        return inputParams;
    }

    public static String getBeginTime() {
        return beginTime;
    }

    public static int getFromPage() {
        return fromPage;
    }

    public static int getToPage() {
        return toPage;
    }



}
