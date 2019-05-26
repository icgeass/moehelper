package com.zeroq6.moehelper.test.help;

import com.zeroq6.moehelper.utils.MyDateUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PostStatistics {

    private int allPageNum = 0;
    private int readSuccessPageNum = 0;
    private int readFailedPageNum = 0;
    private int jsonDataItemNum = 0;


    private int[] detailAllCount = new int[5];
    private int[] poolCount = new int[4];
    private int[] writeCount = new int[4];


    // 
    private int detailAllCountSuccessJson;
    private int detailAllCountSuccessDocPostDeleted;
    private int detailAllCountFailed404;
    private int detailAllCountFailedException;
    private int detailAllCountFailedNoUrl;


    private int poolCountSuccessJsonInPool;
    private int poolCountSuccessJsonNotInPool;
    private int poolCountSuccessDocPostDeletedInPool;
    private int poolCountSuccessDocPostDeletedNotInPool;


    private int writeCountJson;
    private int writeCountUrl;
    private int writeCountMd5;
    private int writeCountLog;


    //
    private Integer minPostId = null;
    private Integer maxPostId = null;
    //
    private Integer fromPostId;
    private Integer toPostId;
    //
    private List<String> fileNameReadFrom = null;

    public PostStatistics(String logFileFolder) {
        this(logFileFolder, null, null);
    }


    public PostStatistics(String logFileFolder, Integer from, Integer to) {
        try {
            String logFileSuffix = "_post.log";

            File dir = new File(logFileFolder);
            List<String> logLines = new ArrayList<String>();
            fileNameReadFrom = new ArrayList<String>();
            allPageNum = 0;
            readSuccessPageNum = 0;
            readFailedPageNum = 0;
            jsonDataItemNum = 0;
            detailAllCount = new int[5];
            poolCount = new int[4];
            writeCount = new int[4];
            maxPostId = null;
            minPostId = null;
            for (File file : dir.listFiles()) {
                if (file.getName().endsWith(logFileSuffix)) {
                    String[] splitArray = file.getName().split("_");
                    Integer recentMaxPostId = Integer.valueOf(splitArray[5]);
                    Integer recentMinPostId = Integer.valueOf(splitArray[4]);
                    maxPostId = null == maxPostId ? recentMaxPostId : (maxPostId < recentMaxPostId ? recentMaxPostId : maxPostId);
                    minPostId = null == minPostId ? recentMinPostId : (minPostId > recentMinPostId ? recentMinPostId : minPostId);
                }
            }
            toPostId = null == to ? maxPostId : to;
            fromPostId = null == from ? minPostId : from;
            for (File file : dir.listFiles()) {
                if (file.getName().endsWith(logFileSuffix)) {
                    String[] splitArray = file.getName().split("_");
                    Integer recentMaxPostId = Integer.valueOf(splitArray[5]);
                    Integer recentMinPostId = Integer.valueOf(splitArray[4]);
                    if (recentMaxPostId > toPostId || recentMinPostId < fromPostId) {
                        continue;
                    }
                    fileNameReadFrom.add(file.getName());
                    List<String> liTmpLines = FileUtils.readLines(file, "utf-8");
                    for (int i = 0; i < liTmpLines.size(); i++) {
                        if (i >= 4 && i <= 10) {
                            logLines.add(liTmpLines.get(i));
                        }
                        if (i > 10) {
                            break;
                        }
                    }
                }
            }
            for (int i = 0; i < logLines.size(); i++) {
                switch ((i + 1) % 7) {
                    case 1:
                        allPageNum += Integer.valueOf(logLines.get(i).split(":")[1].trim());
                        break;
                    case 2:
                        readSuccessPageNum += Integer.valueOf(logLines.get(i).split(":")[1].trim());
                        break;
                    case 3:
                        readFailedPageNum += Integer.valueOf(logLines.get(i).split(":")[1].trim());
                        break;
                    case 4:
                        jsonDataItemNum += Integer.valueOf(logLines.get(i).split(":")[1].trim());
                        break;
                    case 5:
                        String itemDetails = logLines.get(i);
                        itemDetails += "=";
                        itemDetails = itemDetails.replace(",", "=");
                        String[] itemDetailsSplitedArray = itemDetails.split("=");
                        for (int j = 0; j < detailAllCount.length; j++) {
                            detailAllCount[j] += Integer.valueOf(itemDetailsSplitedArray[(j + 1) * 2 - 1].trim());
                        }
                        break;
                    case 6:
                        String itemPoolInfo = logLines.get(i);
                        itemPoolInfo += "=";
                        itemPoolInfo = itemPoolInfo.replace(",", "=").replace("+", "=");
                        String[] itemPoolInfoSplitedArray = itemPoolInfo.split("=");
                        for (int j = 0; j < poolCount.length; j++) {
                            poolCount[j] += Integer.valueOf(itemPoolInfoSplitedArray[j <= 1 ? j + 1 : j + 2].trim());
                        }
                        break;
                    case 0:
                        String itemWriteInfo = logLines.get(i);
                        itemWriteInfo += "=";
                        itemWriteInfo = itemWriteInfo.replace(",", "=");
                        String[] itemWriteInfoSplitedArray = itemWriteInfo.split("=");
                        for (int j = 0; j < writeCount.length; j++) {
                            writeCount[j] += Integer.valueOf(itemWriteInfoSplitedArray[(j + 1) * 2 - 1].trim());
                        }
                        break;
                    default:
                }
            }
            // 
            detailAllCountSuccessJson = detailAllCount[0];
            detailAllCountSuccessDocPostDeleted = detailAllCount[1];
            detailAllCountFailed404 = detailAllCount[2];
            detailAllCountFailedException = detailAllCount[3];
            detailAllCountFailedNoUrl = detailAllCount[4];


            poolCountSuccessJsonNotInPool = poolCount[0];
            poolCountSuccessJsonInPool = poolCount[1];
            poolCountSuccessDocPostDeletedNotInPool = poolCount[2];
            poolCountSuccessDocPostDeletedInPool = poolCount[3];


            writeCountJson = writeCount[0];
            writeCountUrl = writeCount[1];
            writeCountMd5 = writeCount[2];
            writeCountLog = writeCount[3];


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void writeToFile(File writeToFile) {
        if (null != writeToFile) {
            try {
                FileUtils.writeLines(writeToFile, "utf-8", Arrays.asList(this.toString().split("\\\\r\\\\n")), false);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }


    public int getAllPageNum() {
        return allPageNum;
    }

    public int getReadSuccessPageNum() {
        return readSuccessPageNum;
    }

    public int getReadFailedPageNum() {
        return readFailedPageNum;
    }

    public int getJsonDataItemNum() {
        return jsonDataItemNum;
    }

    public int getDetailAllCountSuccessJson() {
        return detailAllCountSuccessJson;
    }

    public int getDetailAllCountSuccessDocPostDeleted() {
        return detailAllCountSuccessDocPostDeleted;
    }

    public int getDetailAllCountFailed404() {
        return detailAllCountFailed404;
    }

    public int getDetailAllCountFailedException() {
        return detailAllCountFailedException;
    }

    public int getDetailAllCountFailedNoUrl() {
        return detailAllCountFailedNoUrl;
    }

    public int getPoolCountSuccessJsonInPool() {
        return poolCountSuccessJsonInPool;
    }

    public int getPoolCountSuccessJsonNotInPool() {
        return poolCountSuccessJsonNotInPool;
    }

    public int getPoolCountSuccessDocPostDeletedInPool() {
        return poolCountSuccessDocPostDeletedInPool;
    }

    public int getPoolCountSuccessDocPostDeletedNotInPool() {
        return poolCountSuccessDocPostDeletedNotInPool;
    }

    public int getWriteCountJson() {
        return writeCountJson;
    }

    public int getWriteCountUrl() {
        return writeCountUrl;
    }

    public int getWriteCountMd5() {
        return writeCountMd5;
    }

    public int getWriteCountLog() {
        return writeCountLog;
    }

    public int getMinPostId() {
        return minPostId;
    }

    public int getMaxPostId() {
        return maxPostId;
    }

    public int getFromPostId() {
        return fromPostId;
    }

    public int getToPostId() {
        return toPostId;
    }

    public List<String> getFileNameReadFrom() {
        return Collections.unmodifiableList(fileNameReadFrom);
    }

    @Override
    public String toString() {
        List<String> liResult = new ArrayList<String>();
        liResult.add(MyDateUtils.formatCurrentTime());
        liResult.add("统计: ");
        liResult.add("");
        liResult.add("Id 范围: " + fromPostId + " - " + toPostId);
        liResult.add("页面总数: " + allPageNum);
        liResult.add("读取成功: " + readSuccessPageNum);
        liResult.add("读取失败: " + readFailedPageNum);
        liResult.add("JSON数据条数: " + jsonDataItemNum);
        liResult.add("详细计数: ok-json=" + detailAllCountSuccessJson + ", ok-doc-post-deleted=" + detailAllCountSuccessDocPostDeleted + ", 404=" + detailAllCountFailed404 + ", exception="
                + detailAllCountFailedException + ", no url=" + detailAllCountFailedNoUrl);
        liResult.add("Pool信息: " + "ok-json=" + poolCountSuccessJsonInPool + "+" + poolCountSuccessJsonNotInPool + ", ok-doc-post-deleted=" + poolCountSuccessDocPostDeletedInPool + "+" + poolCountSuccessDocPostDeletedNotInPool);
        liResult.add("文件写入情况: JSON条数=" + writeCountJson + ", 写入URL条数=" + writeCountUrl + ", 写入MD5条数=" + writeCountMd5 + ", 记录Log条数=" + writeCountLog);
        liResult.add("");
        liResult.add("Read from: ");
        Collections.sort(fileNameReadFrom);
        liResult.addAll(fileNameReadFrom);
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : liResult) {
            stringBuilder.append(line).append("\r\n");
        }
        return stringBuilder.toString();
    }
}
