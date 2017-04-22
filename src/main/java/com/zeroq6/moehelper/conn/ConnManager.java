package com.zeroq6.moehelper.conn;

import com.zeroq6.moehelper.config.Configuration;
import com.zeroq6.moehelper.utils.Logger;

/**
 * 连接管理
 * 一个id对应一个连接线程
 * 由配置类初始化存入id, 主方法获取id并启动连接线程, 再由连接线程反馈结果给连接管理
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public final class ConnManager {

    // 需要为负数且两两之间不相同
    public final static int FIND_PAGE_ID_ALL_COMPLETED = -100;
    public final static int FIND_PAGE_ID_LOCKED = -101;
    private final static int FIND_PAGE_ID_DEFAULT = -102;

    private final static int PAGE_DONE = -10086; // China Mobile~

    // 起始页 结束页
    private int fromPage = -1;
    @SuppressWarnings("unused")
    private int toPage = -1;

    // 存储起始页到结束页的编号及对应状态的数组
    private int[] index = null;
    private boolean[] islocked = null;

    // 当前线程数和剩余页面数
    private int currentLeftPageNum = 0;
    private int currentConnThreadNum = 0;
    private int maxThreadNum = 0;
    private boolean isInited = false;

    // 类单实例对象
    private static ConnManager connManager = new ConnManager();

    // 私有构造
    private ConnManager() {
    }

    /**
     * 通过静态方法得到的该类的单实例对象
     * 
     * @return ConnManager
     */
    public static ConnManager getInstance() {
        return ConnManager.connManager;
    }

    /**
     * 配置类初始化时调用, 设置页面范围
     * 
     * @param from
     * @param to
     * @return void
     */
    public synchronized void putRange(int from, int to) {
        this.putRange(from, to, Configuration.DEFAULT_MAX_THREAD_NUM);
    }

    /**
     * 配置类初始化时调用, 设置页面范围, 需传人最大连接线程数
     * 
     * @param from
     * @param to
     * @param maxThreadNum
     * @return void
     */
    public synchronized void putRange(int from, int to, int maxThreadNum) {
        if (this.isInited) {
            Logger.fatal("Illegal Operation");
        }
        this.fromPage = from;
        this.toPage = to;
        this.index = new int[to - from + 1];
        this.islocked = new boolean[to - from + 1];
        for (int i = from; i <= to; i++) {
            this.index[i - from] = i;
            this.islocked[i - from] = false;
        }
        this.currentLeftPageNum = to - from + 1;
        this.currentConnThreadNum = 0;
        this.maxThreadNum = maxThreadNum;
        this.isInited = true;
    }

    /**
     * 主方法调用, 返回可用页面id或错误标识
     * 
     * @return int
     */
    public synchronized int getPageId() {
        boolean hasNoReadOkPage = false;
        int re = ConnManager.FIND_PAGE_ID_DEFAULT;
        for (int i = 0; i < this.index.length; i++) {
            if (this.index[i] != PAGE_DONE) {
                hasNoReadOkPage = true;
            }
            // 返回可用的Page Id, 限制条件为: 该页面非处理完成, 未锁住, 未超过最大线程
            if (this.index[i] != PAGE_DONE && !this.islocked[i] && this.currentConnThreadNum < this.maxThreadNum) {
                this.islocked[i] = true;
                this.currentConnThreadNum++;
                Logger.stdOut("开始下载页面 # " + index[i] + ", 当前连接数 " + currentConnThreadNum);
                re = this.index[i]; // 可用id
                break;
            }
        }
        if (re == FIND_PAGE_ID_DEFAULT) {
            if (hasNoReadOkPage) {
                re = ConnManager.FIND_PAGE_ID_LOCKED; // 页面被锁住标识
            } else {
                Logger.stdOut("所有下载线程已结束");
                re = ConnManager.FIND_PAGE_ID_ALL_COMPLETED;// 页面均完成标识
            }
        }
        return re;
    }

    /**
     * Conn线程调用, 传入页面id, 记录该页面处理结束
     * 
     * @param id
     * @return void
     */
    synchronized void readPageOK(int id) {
        if (this.index[id - fromPage] == id && this.islocked[id - fromPage]) {
            this.index[id - fromPage] = PAGE_DONE;// 标示读取成功
            this.currentLeftPageNum--;
            this.islocked[id - fromPage] = false;
            this.currentConnThreadNum--;
            Logger.stdOut("页面 # " + id + "下载成功, 剩余页面数 " + this.currentLeftPageNum + ", 当前连接数 " + currentConnThreadNum);
        } else {
            Logger.fatal("Unreachable code");
        }
    }

    /**
     * 当Conn线程获取页面失败后, 仅释放资源, 让其他线程继续处理该页面
     * 
     * @param id
     * @return void
     */
    synchronized void unlockCurrentPage(int id) {
        if (this.index[id - fromPage] == id && this.islocked[id - fromPage]) {
            this.islocked[id - fromPage] = false;
            this.currentConnThreadNum--;
            Logger.stdOut("页面 # " + id + "下载失败, 等待其它线程处理, 剩余页面数 " + this.currentLeftPageNum + ", 当前连接数 " + currentConnThreadNum);
        } else {
            Logger.fatal("Unreachable code");
        }
    }
}
