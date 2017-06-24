package com.zeroq6.moehelper.test;

import com.zeroq6.moehelper.utils.MyDateUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by yuuki asuna on 2017/6/22.
 */
public class CalcInfoTest {


    @Test
    public void calcInfoTest() throws IOException {
        String logFilesDir = "C:\\Users\\yuuki asuna\\Desktop\\!work\\konachan.com\\Post";
        // ------------------------------------
        Integer to = 180000; // 最大id，null表示所有
        String pathToWrite = "./tmp/all_post_info_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".txt";
        File dir = new File(logFilesDir);
        List<String> liLines = new ArrayList<String>();
        List<String> liFileNameReadFrom = new ArrayList<String>();
        List<String> liResult = new ArrayList<String>();
        int allPageNum = 0;
        int readOkPageNum = 0;
        int readFailedPageNum = 0;
        int jsonItemNum = 0;
        int[] countDetailsInfo = new int[5];
        int[] poolInfo = new int[4];
        int[] writeInfo = new int[4];
        for (File file : dir.listFiles()) {
            if (file.getName().endsWith("_post.log")) {
                Integer maxId = Integer.valueOf(file.getName().split("_")[5]);
                if (null == to) {
                    to = null == to ? maxId : (to < maxId ? maxId : to);
                } else {
                    if (maxId > to) {
                        continue;
                    }
                }
                liFileNameReadFrom.add(file.getName());
                List<String> liTmpLines = FileUtils.readLines(file, "utf-8");
                for (int i = 0; i < liTmpLines.size(); i++) {
                    if (i >= 4 && i <= 10) {
                        liLines.add(liTmpLines.get(i));
                    }
                    if (i > 10) {
                        break;
                    }
                }
            }
        }
        for (int i = 0; i < liLines.size(); i++) {
            switch ((i + 1) % 7) {
                case 1:
                    allPageNum += Integer.valueOf(liLines.get(i).split(":")[1].trim());
                    break;
                case 2:
                    readOkPageNum += Integer.valueOf(liLines.get(i).split(":")[1].trim());
                    break;
                case 3:
                    readFailedPageNum += Integer.valueOf(liLines.get(i).split(":")[1].trim());
                    break;
                case 4:
                    jsonItemNum += Integer.valueOf(liLines.get(i).split(":")[1].trim());
                    break;
                case 5:
                    String itemDetails = liLines.get(i);
                    itemDetails += "=";
                    itemDetails = itemDetails.replace(",", "=");
                    String[] itemDetailsSplitedArray = itemDetails.split("=");
                    for (int j = 0; j < countDetailsInfo.length; j++) {
                        countDetailsInfo[j] += Integer.valueOf(itemDetailsSplitedArray[(j + 1) * 2 - 1].trim());
                    }
                    break;
                case 6:
                    String itemPoolInfo = liLines.get(i);
                    itemPoolInfo += "=";
                    itemPoolInfo = itemPoolInfo.replace(",", "=").replace("+", "=");
                    String[] itemPoolInfoSplitedArray = itemPoolInfo.split("=");
                    for (int j = 0; j < poolInfo.length; j++) {
                        poolInfo[j] += Integer.valueOf(itemPoolInfoSplitedArray[j <= 1 ? j + 1 : j + 2].trim());
                    }
                    break;
                case 0:
                    String itemWriteInfo = liLines.get(i);
                    itemWriteInfo += "=";
                    itemWriteInfo = itemWriteInfo.replace(",", "=");
                    String[] itemWriteInfoSplitedArray = itemWriteInfo.split("=");
                    for (int j = 0; j < writeInfo.length; j++) {
                        writeInfo[j] += Integer.valueOf(itemWriteInfoSplitedArray[(j + 1) * 2 - 1].trim());
                    }
                    break;
                default:
            }
        }
        liResult.add(MyDateUtils.formatCurrentTime());
        liResult.add("统计: ");
        liResult.add("");
        liResult.add("Id 范围: 1 - " + to);
        liResult.add("页面总数: " + allPageNum);
        liResult.add("读取成功: " + readOkPageNum);
        liResult.add("读取失败: " + readFailedPageNum);
        liResult.add("JSON数据条数: " + jsonItemNum);
        liResult.add("详细计数: ok-json=" + countDetailsInfo[0] + ", ok-doc-post-deleted=" + countDetailsInfo[1] + ", 404=" + countDetailsInfo[2] + ", exception="
                + countDetailsInfo[3] + ", no url=" + countDetailsInfo[4]);
        liResult.add("Pool信息: " + "ok-json=" + poolInfo[0] + "+" + poolInfo[1] + ", ok-doc-post-deleted=" + poolInfo[2] + "+" + poolInfo[3]);
        liResult.add("文件写入情况: JSON条数=" + writeInfo[0] + ", 写入URL条数=" + writeInfo[1] + ", 写入MD5条数=" + writeInfo[2] + ", 记录Log条数=" + writeInfo[3]);
        liResult.add("");
        liResult.add("Read from: ");
        Collections.sort(liFileNameReadFrom);
        liResult.addAll(liFileNameReadFrom);
        for (String line : liResult) {
            System.out.println(line);
        }
        FileUtils.writeLines(new File(pathToWrite), "utf-8", liResult, false);

    }
}
