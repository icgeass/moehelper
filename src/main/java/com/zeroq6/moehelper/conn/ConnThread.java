package com.zeroq6.moehelper.conn;

import com.zeroq6.moehelper.config.Configuration;
import com.zeroq6.moehelper.linkstart.App;
import com.zeroq6.moehelper.utils.Logger;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * 连接线程
 * 由主方法启动, 页面下载完成后向连接管理反馈结果并启动页面解析线程处理
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public class ConnThread implements Runnable {
    
    // 每个CloseableHttpClient默认只允许两个连接, 需要设置cm
    private static PoolingHttpClientConnectionManager cm = null;
    private static CloseableHttpClient httpClient = null;
    private static RequestConfig requestConfig = null;
    static {
        try {
            cm = new PoolingHttpClientConnectionManager();
            // 全局最大连接数
            cm.setMaxTotal(Integer.valueOf(Configuration.getConnParam("maxTotalConnection")));
            // 设置每个站点的默认连接数
            cm.setDefaultMaxPerRoute(Integer.valueOf(Configuration.getConnParam("maxPerSiteConnection")));
            httpClient = HttpClients.custom().setConnectionManager(cm).build();
            requestConfig = RequestConfig.custom().setSocketTimeout(4000).build();
        } catch (Exception e) {
            Logger.fatal("init failed.", e);
        }
    }
    private int pageId = -10010; // China Unicom~
    private String linkType = Configuration.getConnParam("linkType");
    private String host = Configuration.getConnParam("host");
    private String protocol = Configuration.getConnParam("protocol");
    private int port = Integer.valueOf(Configuration.getConnParam("port"));

    public ConnThread(int pageId) {
        this.pageId = pageId;
    }

    @Override
    public void run() {
        HttpGet req = null;
        CloseableHttpResponse resp = null;
        String connectUrl = null;
        boolean isDocProcessing = false;
        try {
            HttpHost target = new HttpHost(this.host, this.port, this.protocol);
            req = new HttpGet("/" + this.linkType + "/show/" + "0" + this.pageId); // 目前在页面id前加0可以防止moe的pool的404屏蔽
            req.addHeader("Host", this.host);
            req.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.2; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
            req.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            req.addHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
            req.addHeader("Accept-Encoding", "gzip, deflate");
            req.addHeader("Connection", "close");
            req.addHeader("Pragma", "no-cache");
            req.addHeader("Cache-Control", "no-cache");
            req.setConfig(requestConfig);
            resp = httpClient.execute(target, req);
            connectUrl = this.protocol + "://" + this.host + "/" + this.linkType + "/show/" + this.pageId;
            if (resp.getStatusLine().getStatusCode() != 200 && resp.getStatusLine().getStatusCode() != 404) {
                throw new org.jsoup.HttpStatusException("HTTP error fetching URL", resp.getStatusLine().getStatusCode(), connectUrl);
            }
            // 开始调用线程本地处理
            isDocProcessing = true;
            if (resp.getStatusLine().getStatusCode() == 404) {
                new Thread(Configuration.getFetcher(pageId, null)).start();
                ConnManager.getInstance().readPageOK(this.pageId);
            } else if (resp.getStatusLine().getStatusCode() == 200) {
                Document doc = Jsoup.parse(resp.getEntity().getContent(), "utf-8", connectUrl);
                App.getExecutorservice().execute(Configuration.getFetcher(pageId, doc));
                ConnManager.getInstance().readPageOK(this.pageId);
            } else {
                throw new RuntimeException("lyh, I miss you now. though this exception can't be thrown, it compiled");
            }
        } catch (Exception e) {
            if (e instanceof java.net.SocketTimeoutException) {
                // 连接超时可以忽略重新处理
                ConnManager.getInstance().unlockCurrentPage(this.pageId);
                Logger.info("Page #" + this.pageId + " Connection timeout, now redo...");
            } else if (!isDocProcessing) {
                // 在调用子线程之前出现的异常均可忽略重新处理
                ConnManager.getInstance().unlockCurrentPage(this.pageId);
                Logger.info("Page #" + this.pageId + " Connection exception, now redo...");
            } else {
                Logger.fatal("Page #" + this.pageId + " Exception occured while calling sub thread to handle this page.", e);
            }
        } finally {
            try {
                if (resp != null) {
                    resp.close();
                }
                if (req != null) {
                    req.releaseConnection();
                }
            } catch (Exception e) {
                Logger.info("Page #" + this.pageId + " release connection failed.");
            }
        }
    }
}
