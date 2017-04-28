package com.zeroq6.moehelper.writer.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.zeroq6.moehelper.fetcher.impl.PoolFetcher;
import com.zeroq6.moehelper.utils.MyLogUtils;
import com.zeroq6.moehelper.utils.MyDateUtils;
import com.zeroq6.moehelper.utils.MyPoolUtils;
import com.zeroq6.moehelper.utils.MyStringUtils;
import com.zeroq6.moehelper.writer.Writer;
import com.zeroq6.moehelper.config.Configuration;
import com.zeroq6.moehelper.fetcher.impl.PrevPoolCheckUtils;
import com.zeroq6.moehelper.log.impl.PoolLog;
import com.zeroq6.moehelper.resources.ResourcesHolder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Pool数据写入, 先从Runtime和PoolLog中获取数据存在实例字段中后进行写入
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public class PoolWriter implements Writer {

    private List<String> liJsonData = new ArrayList<String>();

    private List<String> liPoolDescription = new ArrayList<String>();

    private List<String> liPoolZipUrlUpdated = new ArrayList<String>();

    private List<String> liPoolZipUrlAll = new ArrayList<String>();

    private List<String> liPoolLogUpdated = new ArrayList<String>();

    private List<String> liPoolLogAll = new ArrayList<String>();

    private List<String> liPoolIdToZipNumCount = new ArrayList<String>();

    public PoolWriter() {
    }

    @Override
    public void writeToFile() {
        try {
            init();
            validate();
            logPoolChangeToStdErr();
            writeJson();
            writePoolIdToZipNumCount();
            writePoolDescription();
            writePackageZipUrlAll();
            writePackageZipUrlUpdated();
            writePoolLogUpdated();
            writePoolLogAll();
        } catch (Exception e) {
            MyLogUtils.fatal("文件写入错误", e);
        }
    }


    private void init() {
        int numJpg = 0;
        int numPng = 0;
        int numAll = 0;
        for (int i = Configuration.getFromPage(); i <= Configuration.getToPage(); i++) {
            PoolLog log = (PoolLog) ResourcesHolder.getMapIdLog().get(i);
            String pageStatus = log.getStatus();
            liPoolLogAll.add(String.format("# %5d  |" + log.getJpegPackages() + "   |" + log.getOriginalPackages() + "       |" + log.getAllPackages() + "    |" + log.getIsExist() + "   |" + log.getStatus(), log.getId()));
            if (PoolLog.POOL_STATUS_NULL.equals(pageStatus)) {
                continue;
            }
            String jsonData = PoolLog.getMapPageId2PoolDescription().get(i);
            if(null == jsonData){
                MyLogUtils.fatal("no json found, index: " + i);
            }
            String description = PoolLog.getMapPageId2PoolDescription().get(i);
            if(null == description){
                MyLogUtils.fatal("no description found, index: " + i);
            }
            liPoolDescription.add(description);
            liJsonData.add(jsonData);
            if (PoolLog.POOL_STATUS_EMPTY.equals(pageStatus) || PoolLog.POOL_STATUS_ALL_DELETED.equals(pageStatus)) {
                continue;
            }
            numJpg += log.getJpegPackages();
            numPng += log.getOriginalPackages();
            numAll += log.getAllPackages();
            liPoolIdToZipNumCount.add("To #" + log.getId() + ": " + numAll + " in all, " + numJpg + " jpeg packages and " + numPng + " original packages...");
            if (PoolLog.POOL_STATUS_MODIFIED.equals(pageStatus) || PoolLog.POOL_STATUS_NEW.equals(pageStatus)) {
                liPoolLogUpdated.add(String.format("# %5d  |" + log.getJpegPackages() + "   |" + log.getOriginalPackages() + "       |" + log.getAllPackages() + "    |" + log.getIsExist() + "   |" + log.getStatus(), log.getId()));
                for (String url : PoolLog.getMapPageId2ZipLinkPoolUpdated().get(i)) {
                    liPoolZipUrlUpdated.add(formatZipUrl(url));
                }
            }
            for (String url : PoolLog.getMapPageId2ZipLinkPoolAll().get(i)) {
                liPoolZipUrlAll.add(formatZipUrl(url));
            }
        }
    }

    private void validate() {
        boolean isValidateOk = true;
        isValidateOk = isValidateOk && ResourcesHolder.getMapIdJson().keySet().equals(PoolLog.getMapPageId2PoolDescription().keySet());
        isValidateOk = isValidateOk && liPoolIdToZipNumCount.size() == PoolLog.getMapPageId2ZipLinkPoolAll().size();
        isValidateOk = isValidateOk && liPoolLogUpdated.size() == PoolLog.getMapPageId2ZipLinkPoolUpdated().size();
        isValidateOk = isValidateOk && liPoolLogAll.size() == Configuration.getToPage() - Configuration.getFromPage() + 1;
        int allPageNum = Configuration.getToPage() - Configuration.getFromPage() + 1;
        int logAllPageNumByPageStatus = PoolLog.getPageCountByPageStatus(PoolLog.POOL_STATUS_NULL) + PoolLog.getPageCountByPageStatus(PoolLog.POOL_STATUS_EMPTY) + PoolLog.getPageCountByPageStatus(PoolLog.POOL_STATUS_ALL_DELETED) + PoolLog.getPageCountByPageStatus(PoolLog.POOL_STATUS_NEW) + PoolLog.getPageCountByPageStatus(PoolLog.POOL_STATUS_MODIFIED) + PoolLog.getPageCountByPageStatus(PoolLog.POOL_STATUS_NO_CHANGE);
        isValidateOk = isValidateOk && (allPageNum == logAllPageNumByPageStatus);
        if (!isValidateOk) {
            MyLogUtils.fatal("数据验证失败");
        }
    }

    private void writeJson() throws IOException {
        FileUtils.writeLines(new File(Writer.W_FULL_PATH_PREFIX + ".json"), "utf-8", liJsonData, false);
    }

    private void writePoolIdToZipNumCount() throws IOException {
        List<String> li = new ArrayList<String>();
        li.add(MyDateUtils.formatCurrentTime());
        li.add("from Pool #" + Configuration.getFromPage() + " to Pool #" + Configuration.getToPage() + "\r\n");
        li.addAll(liPoolIdToZipNumCount);
        FileUtils.writeLines(new File(Writer.W_FULL_PATH_PREFIX + "_info.txt"), "utf-8", li, false);
    }

    private void writePoolDescription() throws IOException {
        List<String> li = new ArrayList<String>();
        li.add(MyDateUtils.formatCurrentTime());
        li.add("from Pool #" + Configuration.getFromPage() + " to Pool #" + Configuration.getToPage() + "\r\n");
        li.addAll(liPoolDescription);
        FileUtils.writeLines(new File(Writer.W_FULL_PATH_PREFIX + "_info_details.txt"), "utf-8", li, false);
    }

    private void writePackageZipUrlAll() throws IOException {
        FileUtils.writeLines(new File(Writer.W_FULL_PATH_PREFIX + "_packages_url.lst"), "utf-8", liPoolZipUrlAll, false);
    }

    private void writePackageZipUrlUpdated() throws IOException {
        FileUtils.writeLines(new File(Writer.W_FULL_PATH_PREFIX + "_packages_updated_url.lst"), "utf-8", liPoolZipUrlUpdated, false);
    }

    private void writePoolLogUpdated() throws IOException {
        List<String> li = new ArrayList<String>();
        li.add(MyDateUtils.formatCurrentTime());
        li.add("统计: ");
        li.add("");
        int updatedPoolNum = 0;
        int zipNumUpdatedJpg = 0;
        int zipNumUpdatedPng = 0;
        int zipNumUpdatedAll = 0;
        int zipNumModifiedJpg = 0;
        int zipNumModifiedPng = 0;
        int zipNumModifiedAll = 0;
        int zipNumNewJpg = 0;
        int zipNumNewPng = 0;
        int zipNumNewAll = 0;
        for (int i = Configuration.getFromPage(); i <= Configuration.getToPage(); i++) {
            PoolLog log = (PoolLog) ResourcesHolder.getMapIdLog().get(i);
            if (PoolLog.POOL_STATUS_NEW.equals(log.getStatus()) || PoolLog.POOL_STATUS_MODIFIED.equals(log.getStatus())) {
                zipNumUpdatedJpg += log.getJpegPackages();
                zipNumUpdatedPng += log.getOriginalPackages();
                zipNumUpdatedAll += log.getAllPackages();
                if (PoolLog.POOL_STATUS_MODIFIED.equals(log.getStatus())) {
                    zipNumModifiedJpg += log.getJpegPackages();
                    zipNumModifiedPng += log.getOriginalPackages();
                    zipNumModifiedAll += log.getAllPackages();
                } else if (PoolLog.POOL_STATUS_NEW.equals(log.getStatus())) {
                    zipNumNewJpg += log.getJpegPackages();
                    zipNumNewPng += log.getOriginalPackages();
                    zipNumNewAll += log.getAllPackages();
                }
                updatedPoolNum++;
            }
        }
        li.add("被添加和修改的Pool数目: " + updatedPoolNum);
        li.add("zip数目(modified): Jpeg_packages=" + zipNumModifiedJpg + ", Original_packages=" + zipNumModifiedPng + ", All_packages=" + zipNumModifiedAll);
        li.add("zip数目(new): Jpeg_packages=" + zipNumNewJpg + ", Original_packages=" + zipNumNewPng + ", All_packages=" + zipNumNewAll);
        li.add("zip数目(updated): Jpeg_packages=" + zipNumUpdatedJpg + ", Original_packages=" + zipNumUpdatedPng + ", All_packages=" + zipNumUpdatedAll);
        li.add("");
        li.add("Pool Id  jpeg original all   isexist  status");
        li.add("------------------------------------------------");
        li.addAll(liPoolLogUpdated);
        FileUtils.writeLines(new File(Writer.W_FULL_PATH_PREFIX + "_info_updated.txt"), "utf-8", li, false);
    }

    private void writePoolLogAll() throws IOException {
        List<String> li = new ArrayList<String>();
        li.add(MyDateUtils.formatCurrentTime());
        li.add("统计: ");
        String userOptions = "";
        for (String string : Configuration.getInputParams()) {
            userOptions += string + " ";
        }
        userOptions = userOptions.trim();
        li.add("");
        li.add("用户参数: " + userOptions);
        li.add("页面总数: " + (Configuration.getToPage() - Configuration.getFromPage() + 1));
        li.add("读取成功: " + (PoolLog.getPageCountByPageStatus(PoolLog.POOL_STATUS_EMPTY) + PoolLog.getPageCountByPageStatus(PoolLog.POOL_STATUS_ALL_DELETED) + PoolLog.getPageCountByPageStatus(PoolLog.POOL_STATUS_NEW) + PoolLog.getPageCountByPageStatus(PoolLog.POOL_STATUS_MODIFIED) + PoolLog.getPageCountByPageStatus(PoolLog.POOL_STATUS_NO_CHANGE)));
        li.add("读取失败: " + ResourcesHolder.getReadFailedPageCount());
        li.add("JSON数据条数: " + ResourcesHolder.getMapIdJson().size());
        li.add("详细计数: null=" + PoolLog.getPageCountByPageStatus(PoolLog.POOL_STATUS_NULL) + ", empty=" + PoolLog.getPageCountByPageStatus(PoolLog.POOL_STATUS_EMPTY) + ", all-deleted=" + PoolLog.getPageCountByPageStatus(PoolLog.POOL_STATUS_ALL_DELETED) + ", modified=" + PoolLog.getPageCountByPageStatus(PoolLog.POOL_STATUS_MODIFIED) + ", new=" + PoolLog.getPageCountByPageStatus(PoolLog.POOL_STATUS_NEW) + ", no-change=" + PoolLog.getPageCountByPageStatus(PoolLog.POOL_STATUS_NO_CHANGE));
        li.add("写入情况: Log条数=" + liPoolLogAll.size() + ", json=" + liJsonData.size() + ", package计数=" + liPoolIdToZipNumCount.size() + ", pool-details=" + liPoolDescription.size() + "\r\n\t  all-package-url=" + liPoolZipUrlAll.size() + ", updated-package-url=" + liPoolZipUrlUpdated.size() + ", 被添加和更新Pool数=" + liPoolLogUpdated.size());
        li.add("");
        li.add("Pool Id  jpeg original all   isexist  status");
        li.add("------------------------------------------------");
        li.addAll(liPoolLogAll);
        FileUtils.writeLines(new File(Writer.W_FULL_PATH_PREFIX + ".log"), "utf-8", li, false);

    }


    private void logPoolChangeToStdErr() {
        MyLogUtils.info("====================check zip packages changes begin========================");
        MyLogUtils.info("Pool deleted:");
        int affectedPoolNum = 0;
        int affectedZipNumJpg = 0;
        int affectedZipNumPng = 0;
        int affectedZipNumJpgSum = 0;
        int affectedZipNumPngSum = 0;
        // 比较均只针对于new, modified, no change
        Set<Integer> setLastTimePoolId = PrevPoolCheckUtils.getMapLastTimePageId2ZipLinkCountInfo().keySet();
        Set<Integer> setNowPoolId = PoolLog.getMapPageId2ZipLinkPoolAll().keySet();
        // PoolId在本次更新范围内的上一次更新范围含有而本次不含有, 则认为是被删除
        for (Integer poolId : setLastTimePoolId) {
            if (poolId > Configuration.getToPage() || poolId < Configuration.getFromPage()) {
                continue;
            }
            if (!setNowPoolId.contains(poolId)) {
                // 0b10或0b11
                Integer statusPre = getLastTimePageId2ZipLinkNumInfoById(poolId);
                affectedPoolNum++;
                affectedZipNumJpg -= MyPoolUtils.toPoolJpegCount(statusPre); // jpg位
                affectedZipNumPng -= MyPoolUtils.toPoolOriginalCount(statusPre); // png位
                MyLogUtils.info("Pool #" + poolId + " was deleted, status = " + Integer.toBinaryString(statusPre));
            }
        }
        affectedZipNumJpgSum += affectedZipNumJpg; // 负
        affectedZipNumPngSum += affectedZipNumPng; // 负
        MyLogUtils.warn("Pool that deleted " + affectedPoolNum + " in all, jpeg packages affected: " + MyStringUtils.insertBeforePlusOrMinus(affectedZipNumJpg) + ", original packages affected: " + MyStringUtils.insertBeforePlusOrMinus(affectedZipNumPng));
        MyLogUtils.info("");
        // 注意重置数据
        affectedPoolNum = 0;
        affectedZipNumJpg = 0;
        affectedZipNumPng = 0;
        MyLogUtils.info("Pool new:");
        // PoolId本次更新含有上次更新不含有则认为是新添加, 也可以用日志中status字段判断
        for (Integer poolId : setNowPoolId) {
            if (!setLastTimePoolId.contains(poolId)) {
                Integer statusNow = getNowPageId2ZipLinkNumInfoById(poolId);
                affectedPoolNum++;
                affectedZipNumJpg += MyPoolUtils.toPoolJpegCount(statusNow);
                affectedZipNumPng += MyPoolUtils.toPoolOriginalCount(statusNow);
                MyLogUtils.info("Pool #" + poolId + " is new, status = " + Integer.toBinaryString(statusNow));
            }
        }
        affectedZipNumJpgSum += affectedZipNumJpg; // 正
        affectedZipNumPngSum += affectedZipNumPng; // 正
        MyLogUtils.warn("Pool that new " + affectedPoolNum + " in all, jpeg packages affected: " + MyStringUtils.insertBeforePlusOrMinus(affectedZipNumJpg) + ", original packages affected: " + MyStringUtils.insertBeforePlusOrMinus(affectedZipNumPng));
        MyLogUtils.info("");
        affectedPoolNum = 0;
        affectedZipNumJpg = 0;
        affectedZipNumPng = 0;
        // 检查本次修改过的pool中导致
        MyLogUtils.info("Pool modified:");
        for (int i = Configuration.getFromPage(); i <= Configuration.getToPage(); i++) {
            PoolLog poolLog = (PoolLog) ResourcesHolder.getMapIdLog().get(i);
            if (PoolLog.POOL_STATUS_MODIFIED.equals(poolLog.getStatus())) {
                Integer statusPre = getLastTimePageId2ZipLinkNumInfoById(i);
                Integer statusNow = getNowPageId2ZipLinkNumInfoById(i);
                affectedPoolNum++;
                affectedZipNumJpg += MyPoolUtils.toPoolJpegCount(statusNow) - MyPoolUtils.toPoolJpegCount(statusPre);
                affectedZipNumPng += MyPoolUtils.toPoolOriginalCount(statusNow) -  MyPoolUtils.toPoolOriginalCount(statusPre);
                MyLogUtils.info("Pool #" + poolLog.getId() + " was modified, status previous = " + Integer.toBinaryString(statusNow) + ", status now = " + Integer.toBinaryString(statusPre));
                if (!statusNow.equals(statusPre)) {
                    int jpegAffected = MyPoolUtils.toPoolJpegCount(statusNow) - MyPoolUtils.toPoolJpegCount(statusPre);
                    int pngAffected = MyPoolUtils.toPoolOriginalCount(statusNow) -  MyPoolUtils.toPoolOriginalCount(statusPre);
                    MyLogUtils.warn("Pool #" + poolLog.getId() + " jpeg packages affected " + MyStringUtils.insertBeforePlusOrMinus(jpegAffected) + ", original packages affected " + MyStringUtils.insertBeforePlusOrMinus(pngAffected));
                }
            }
        }
        affectedZipNumJpgSum += affectedZipNumJpg; // 正、负、零
        affectedZipNumPngSum += affectedZipNumPng; // 正、负、零
        MyLogUtils.warn("Pool that modified " + affectedPoolNum + " in all, jpeg packages affected: " + MyStringUtils.insertBeforePlusOrMinus(affectedZipNumJpg) + ", original packages affected: " + MyStringUtils.insertBeforePlusOrMinus(affectedZipNumPng));
        MyLogUtils.info("");
        affectedPoolNum = 0;
        affectedZipNumJpg = 0;
        affectedZipNumPng = 0;
        MyLogUtils.info("-------------result-------------");
        // -----------------------------------------------------
        int jpegZipPreSum = 0;
        int originalZipPreSum = 0;
        int allZipPreSum = 0;
        for (Integer key : setLastTimePoolId) {
            Integer statusPre = getLastTimePageId2ZipLinkNumInfoById(key);
            jpegZipPreSum += MyPoolUtils.toPoolJpegCount(statusPre);
            originalZipPreSum += MyPoolUtils.toPoolOriginalCount(statusPre);
            allZipPreSum += MyPoolUtils.toPoolJpegCount(statusPre) + MyPoolUtils.toPoolOriginalCount(statusPre);
        }
        MyLogUtils.warn("last time zips: ");
        MyLogUtils.warn("all: " + allZipPreSum + ", jpeg: " + jpegZipPreSum + ", original: " + originalZipPreSum);
        MyLogUtils.warn("this time affected zips: ");
        MyLogUtils.warn("all: " + (affectedZipNumJpgSum + affectedZipNumPngSum) + ", jpeg: " + (affectedZipNumJpgSum) + ", original: " + (affectedZipNumPngSum));
        MyLogUtils.warn("this time the zips should be: ");
        MyLogUtils.warn("all: " + (allZipPreSum + affectedZipNumJpgSum + affectedZipNumPngSum) + ", jpeg: " + (jpegZipPreSum + affectedZipNumJpgSum) + ", original: " + (originalZipPreSum + affectedZipNumPngSum));
        int jpegZipNowSum = 0;
        int originalZipNowSum = 0;
        int allZipNowSum = 0;
        // 也可以通过PoolLog判断
        for (Integer key : PoolLog.getMapPageId2ZipLinkPoolAll().keySet()) {
            for (String url : PoolLog.getMapPageId2ZipLinkPoolAll().get(key)) {
                if (url.contains(PoolFetcher.LINK_POOL_ZIP_SUFFIX_JPG)) {
                    jpegZipNowSum++;
                    allZipNowSum++;
                } else {
                    originalZipNowSum++;
                    allZipNowSum++;
                }
            }
        }
        MyLogUtils.warn("this time zips actual: ");
        MyLogUtils.warn("all: " + allZipNowSum + ", jpeg: " + jpegZipNowSum + ", original: " + originalZipNowSum);
        if(allZipNowSum != allZipPreSum + affectedZipNumJpgSum + affectedZipNumPngSum || jpegZipNowSum != jpegZipPreSum + affectedZipNumJpgSum || originalZipNowSum != originalZipPreSum + affectedZipNumPngSum){
            MyLogUtils.fatal("check failed, zips url number info predicted not match actual!");
        }
        MyLogUtils.warn("check success, 'this time zips actual' equal to 'this time the zips should be' !");
        MyLogUtils.info("==================check zip packages changes end======================");
    }

    private Integer getLastTimePageId2ZipLinkNumInfoById(Integer pageId) {
        Integer re = PrevPoolCheckUtils.getMapLastTimePageId2ZipLinkCountInfo().get(pageId);
        if (re != 0b10 && re != 0b11) {
            MyLogUtils.fatal("find a error zip pack number info in last time, the page id is " + pageId);
        }
        return re;
    }

    private Integer getNowPageId2ZipLinkNumInfoById(Integer pageId) {
        Integer re = 0;
        PoolLog poolLog = (PoolLog) ResourcesHolder.getMapIdLog().get(pageId);
        re += poolLog.getJpegPackages();
        re += poolLog.getOriginalPackages() << 1;
        if (re != 0b10 && re != 0b11) {
            MyLogUtils.fatal("find a error zip pack number info in this time, the page id is " + pageId);
        }
        return re;
    }

    /**
     * // https://yande.re/pool/zip/3387/Dengeki%20Moeoh%202014-04%20(JPG).zip?jpeg=1
     * https://yande.re/pool/zip/6?jpeg=1 ,  https://yande.re/pool/zip/6
     * 转为
     * https://yande.re/pool/zip/6?jpeg=1&pid=0006 ,  https://yande.re/pool/zip/6?pid=0006
     * @param url
     * @return String
     */
    private String formatZipUrl(String url) {
        if(!PoolFetcher.pattern_zip_link.matcher(url).matches()){
            MyLogUtils.fatal("Unrecognized url");
        }
        String[] splitStrArr = url.split("/", 6);
        if(null == splitStrArr || splitStrArr.length != 6){
            MyLogUtils.fatal("error Pool url format: " + url);
        }
        String pageIdString = splitStrArr[splitStrArr.length - 1];
        pageIdString = pageIdString.substring(0, pageIdString.indexOf("?") == -1 ? pageIdString.length() : pageIdString.indexOf("?"));
        pageIdString = StringUtils.leftPad(pageIdString, 4, "0");
        if(!StringUtils.isNumeric(pageIdString)){
            MyLogUtils.fatal("pageId获取失败, " + pageIdString);
        }
        if(url.contains(PoolFetcher.LINK_POOL_ZIP_SUFFIX_JPG)){
            url = url + "&pid=" +  pageIdString;
        }else{
            url = url + "?pid=" + pageIdString;
        }
        return url;
    }
}
