package loli.kanojo.moehelper.linkstart;

import loli.kanojo.moehelper.config.Configuration;
import loli.kanojo.moehelper.conn.ConnManager;
import loli.kanojo.moehelper.conn.ConnThread;
import loli.kanojo.moehelper.rt.Runtime;
import loli.kanojo.moehelper.utils.Logger;

/**
 * 主方法调用
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 * @version moehelper - v1.0.7
 * @url https://github.com/icgeass/moehelper
 */
public class App {

    public static void main(String[] args) {
        try {
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
                new Thread(new ConnThread(pageId)).start();
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
    }
}
