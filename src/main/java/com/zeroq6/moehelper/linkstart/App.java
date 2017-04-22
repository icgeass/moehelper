package com.zeroq6.moehelper.linkstart;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.zeroq6.moehelper.conn.ConnThread;
import com.zeroq6.moehelper.config.Configuration;
import com.zeroq6.moehelper.conn.ConnManager;
import com.zeroq6.moehelper.rt.Runtime;
import com.zeroq6.moehelper.utils.Logger;

/**
 * 主方法调用
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public class App {
    
    private final static ExecutorService executorService = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        try {
            System.setProperty("line.separator", "\r\n");
            Configuration.init(args);
            linkStart();// ヽ(✿ﾟ▽ﾟ)ノ
            Configuration.getWriter().writeToFile();
            Logger.stdOut("所有操作已完成");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void linkStart() throws InterruptedException {
        lab: while (true) {
            int pageId = ConnManager.getInstance().getPageId();
            if (pageId != ConnManager.FIND_PAGE_ID_ALL_COMPLETED && pageId != ConnManager.FIND_PAGE_ID_LOCKED) {
                executorService.execute(new ConnThread(pageId));
            } else if (pageId == ConnManager.FIND_PAGE_ID_ALL_COMPLETED) {
                // 对应下载完成但是本地是否解析完成的判断
                int allPageNum = Configuration.getToPage() - Configuration.getFromPage() + 1;
                if (Runtime.getMapid2page().size() + Runtime.getFailedPageNum() != allPageNum || Runtime.getMapid2log().size() != allPageNum) {
                    Logger.stdOut("等待本地处理完成");
                    Thread.sleep(500);
                }
                // 若匹配完成则跳出外层循环,否则循环等待
                while (true) {
                    if (Runtime.getMapid2page().size() + Runtime.getFailedPageNum() == allPageNum && Runtime.getMapid2log().size() == allPageNum) {
                        Logger.stdOut("本地处理完成");
                        break lab;
                    } else {
                        Thread.sleep(500);
                    }
                }
            }
            // 每隔0.02秒获取pageId
            Thread.sleep(20);
        }
        // 关闭线程池
        executorService.shutdown();
    }

    
    public static ExecutorService getExecutorservice() {
        return executorService;
    }
    
}
