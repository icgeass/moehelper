package com.zeroq6.moehelper.conn;


import com.zeroq6.moehelper.utils.MyLogUtils;

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
    public final static int ALL_COMPLETED = -100;
    public final static int LOCKED = -101;

    // 处理成功的index修改为该值
    private final static int PAGE_ID_FINISHED = -1;


    private final static int MAX_THREAD_COUNT = 5;

    // 初始配置
    private int[] index = null;
    private boolean[] locked = null;
    private boolean init = false;
    private int fromPage = -1;
    private int currentLeftPageCount = 0;
    private int currentConnThreadCount = 0;

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
     * 配置类初始化时调用, 设置页面范围, 需传人最大连接线程数
     *
     * @param from
     * @param to
     * @return void
     */
    public synchronized void putRange(int from, int to) {
        if (init) {
            throw new RuntimeException("只能初始化一次");
        }
        this.fromPage = from;
        this.index = new int[to - from + 1];
        this.locked = new boolean[to - from + 1];
        for (int i = from; i <= to; i++) {
            this.index[i - from] = i;
            this.locked[i - from] = false;
        }
        this.currentLeftPageCount = to - from + 1;
        this.currentConnThreadCount = 0;
        init = true;
    }

    /**
     * 主方法调用, 返回可用页面id或错误标识
     *
     * @return int
     */
    public synchronized int getPageId() {
        boolean findNoFinishedPage = false;
        int pageId = Integer.MIN_VALUE;
        for (int i = 0; i < this.index.length; i++) {
            if (this.index[i] != PAGE_ID_FINISHED) {
                // 标识还有未处理的页面
                findNoFinishedPage = true;
                // 找到可用
                if (!this.locked[i] && this.currentConnThreadCount < MAX_THREAD_COUNT) {
                    pageId = this.index[i]; // 可用pageId
                    this.locked[i] = true;
                    this.currentConnThreadCount++;
                    MyLogUtils.stdOut("开始下载页面 # " + index[i] + ", 当前连接数 " + currentConnThreadCount);
                    break;
                }
            }
        }
        if (pageId == Integer.MIN_VALUE) {
            if (findNoFinishedPage) {
                pageId = ConnManager.LOCKED;
            } else {
                pageId = ConnManager.ALL_COMPLETED;
                MyLogUtils.stdOut("所有页面已下载完成");
            }
        }
        return pageId;
    }

    /**
     * Conn线程调用, 传入页面id, 记录该页面处理结束
     *
     * @param id
     * @return void
     */
    synchronized void readPageOK(int id) {
        if (this.index[id - fromPage] == id && this.locked[id - fromPage]) {
            this.index[id - fromPage] = PAGE_ID_FINISHED;// 标示读取成功
            this.locked[id - fromPage] = false;
            this.currentLeftPageCount--;
            this.currentConnThreadCount--;
            MyLogUtils.stdOut("页面 # " + id + "下载成功, 剩余页面数 " + this.currentLeftPageCount + ", 当前连接数 " + this.currentConnThreadCount);
        } else {
            throw new RuntimeException("Unreachable Code");
        }
    }

    /**
     * 当Conn线程获取页面失败后, 仅释放资源, 让其他线程继续处理该页面
     *
     * @param id
     * @return void
     */
    synchronized void unlockCurrentPage(int id) {
        if (this.index[id - fromPage] == id && this.locked[id - fromPage]) {
            this.locked[id - fromPage] = false;
            this.currentConnThreadCount--;
            MyLogUtils.stdOut("页面 # " + id + "下载失败, 等待其它线程处理, 剩余页面数 " + this.currentLeftPageCount + ", 当前连接数 " + this.currentConnThreadCount);
        } else {
            throw new RuntimeException("Unreachable Code");
        }
    }
}
