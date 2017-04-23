package com.zeroq6.moehelper.app;

import com.zeroq6.moehelper.conn.ConnThread;
import com.zeroq6.moehelper.config.Configuration;
import com.zeroq6.moehelper.conn.ConnManager;
import com.zeroq6.moehelper.resources.ResourcesHolder;
import com.zeroq6.moehelper.utils.MyLogUtils;

/**
 * 主方法调用
 *
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public class App {

    public static void main(String[] args) throws Exception {
        Configuration.init(args);
        begin();
        Configuration.newWriter().writeToFile();
        MyLogUtils.stdOut("所有操作已完成");
    }

    private static void begin() throws InterruptedException {
        lab:
        while (true) {
            int pageId = ConnManager.getInstance().getPageId();
            if (pageId != ConnManager.ALL_COMPLETED && pageId != ConnManager.LOCKED) {
                new ConnThread(pageId).start();
            } else if (pageId == ConnManager.ALL_COMPLETED) {
                // 对应下载完成但是本地是否解析完成的判断
                int allPageNum = Configuration.getToPage() - Configuration.getFromPage() + 1;
                if (ResourcesHolder.getMapIdPage().size() + ResourcesHolder.getReadFailedPageCount() != allPageNum || ResourcesHolder.getMapIdLog().size() != allPageNum) {
                    MyLogUtils.stdOut("等待本地处理完成");
                    Thread.sleep(500);
                }
                // 若匹配完成则跳出外层循环,否则循环等待
                while (true) {
                    if (ResourcesHolder.getMapIdPage().size() + ResourcesHolder.getReadFailedPageCount() == allPageNum && ResourcesHolder.getMapIdLog().size() == allPageNum) {
                        MyLogUtils.stdOut("本地处理完成");
                        break lab;
                    } else {
                        Thread.sleep(500);
                    }
                }
            }
            // 每隔0.02秒获取pageId
            Thread.sleep(20);
        }
    }

}
