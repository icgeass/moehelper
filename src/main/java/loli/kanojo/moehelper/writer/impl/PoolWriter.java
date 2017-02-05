package loli.kanojo.moehelper.writer.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import loli.kanojo.moehelper.config.Configuration;
import loli.kanojo.moehelper.config.Constants;
import loli.kanojo.moehelper.fetcher.impl.PoolUpdatedValidator;
import loli.kanojo.moehelper.log.impl.PoolLog;
import loli.kanojo.moehelper.rt.Runtime;
import loli.kanojo.moehelper.utils.Kit;
import loli.kanojo.moehelper.utils.Logger;
import loli.kanojo.moehelper.writer.Writer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Pool数据写入, 先从Runtime和PoolLog中获取数据存在实例字段中后进行写入
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 * @version moehelper - v1.0.7
 * @url https://github.com/icgeass/moehelper
 */
public class PoolWriter implements Writer {

    private List<String> liJsonData = new ArrayList<String>(100);

    private List<String> liPoolDescription = new ArrayList<String>(100);

    private List<String> liPoolZipUrlUpdated = new ArrayList<String>(100);

    private List<String> liPoolZipUrlAll = new ArrayList<String>(100);

    private List<String> liPoolLogUpdated = new ArrayList<String>(100);

    private List<String> liPoolLogAll = new ArrayList<String>(100);

    private List<String> liPoolIdToZipNumCount = new ArrayList<String>(100);

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
            writeUpdatedPoolLog();
            writeLog();
        } catch (Exception e) {
            Logger.info("error occur while writing, now exiting");
            e.printStackTrace();
        }
    }

    private void writeJson() throws IOException {
        FileUtils.writeLines(new File(Constants.W_FULL_PATH_PREFIX + ".json"), "utf-8", liJsonData, false);
    }

    private void writePoolIdToZipNumCount() throws IOException {
        List<String> li = new ArrayList<String>(100);
        li.add(Kit.getFormatedCurrentTime());
        li.add("from Pool #" + Configuration.getFromPage() + " to Pool #" + Configuration.getToPage() + "\r\n");
        li.addAll(liPoolIdToZipNumCount);
        FileUtils.writeLines(new File(Constants.W_FULL_PATH_PREFIX + "_info.txt"), "utf-8", li, false);
    }

    private void writePoolDescription() throws IOException {
        List<String> li = new ArrayList<String>(100);
        li.add(Kit.getFormatedCurrentTime());
        li.add("from Pool #" + Configuration.getFromPage() + " to Pool #" + Configuration.getToPage() + "\r\n");
        li.addAll(liPoolDescription);
        FileUtils.writeLines(new File(Constants.W_FULL_PATH_PREFIX + "_info_details.txt"), "utf-8", li, false);
    }

    private void writePackageZipUrlAll() throws IOException {
        FileUtils.writeLines(new File(Constants.W_FULL_PATH_PREFIX + "_packages_url.lst"), "utf-8", liPoolZipUrlAll, false);
    }

    private void writePackageZipUrlUpdated() throws IOException {
        FileUtils.writeLines(new File(Constants.W_FULL_PATH_PREFIX + "_packages_updated_url.lst"), "utf-8", liPoolZipUrlUpdated, false);
    }

    private void writeUpdatedPoolLog() throws IOException {
        List<String> li = new ArrayList<String>(20);
        li.add(Kit.getFormatedCurrentTime());
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
            PoolLog log = (PoolLog) Runtime.getMapid2log().get(i);
            if (Constants.POOL_STATUS_NEW.equals(log.getStatus()) || Constants.POOL_STATUS_MODIFIED.equals(log.getStatus())) {
                zipNumUpdatedJpg += log.getJpegPackages();
                zipNumUpdatedPng += log.getOriginalPackages();
                zipNumUpdatedAll += log.getAllPackages();
                if (Constants.POOL_STATUS_MODIFIED.equals(log.getStatus())) {
                    zipNumModifiedJpg += log.getJpegPackages();
                    zipNumModifiedPng += log.getOriginalPackages();
                    zipNumModifiedAll += log.getAllPackages();
                } else if (Constants.POOL_STATUS_NEW.equals(log.getStatus())) {
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
        FileUtils.writeLines(new File(Constants.W_FULL_PATH_PREFIX + "_info_updated.txt"), "utf-8", li, false);
    }

    private void writeLog() throws IOException {
        List<String> li = new ArrayList<String>(200);
        li.add(Kit.getFormatedCurrentTime());
        li.add("统计: ");
        String userOptions = "";
        for (String string : Configuration.getUserInputParams()) {
            userOptions += string + " ";
        }
        li.add("");
        li.add("用户参数: " + userOptions.substring(0, userOptions.length() - 1));
        li.add("页面总数: " + (Configuration.getToPage() - Configuration.getFromPage() + 1));
        li.add("读取成功: " + (PoolLog.getPageNumStatus(Constants.POOL_STATUS_EMPTY) + PoolLog.getPageNumStatus(Constants.POOL_STATUS_ALL_DELETED) + PoolLog.getPageNumStatus(Constants.POOL_STATUS_NEW) + PoolLog.getPageNumStatus(Constants.POOL_STATUS_MODIFIED) + PoolLog.getPageNumStatus(Constants.POOL_STATUS_NO_CHANGE)));
        li.add("读取失败: " + Runtime.getFailedPageNum());
        li.add("JSON数据条数: " + Runtime.getMapid2jsondata().size());
        li.add("详细计数: null=" + PoolLog.getPageNumStatus(Constants.POOL_STATUS_NULL) + ", empty=" + PoolLog.getPageNumStatus(Constants.POOL_STATUS_EMPTY) + ", all-deleted=" + PoolLog.getPageNumStatus(Constants.POOL_STATUS_ALL_DELETED) + ", modified=" + PoolLog.getPageNumStatus(Constants.POOL_STATUS_MODIFIED) + ", new=" + PoolLog.getPageNumStatus(Constants.POOL_STATUS_NEW) + ", no-change=" + PoolLog.getPageNumStatus(Constants.POOL_STATUS_NO_CHANGE));
        li.add("写入情况: Log条数=" + liPoolLogAll.size() + ", json=" + liJsonData.size() + ", package计数=" + liPoolIdToZipNumCount.size() + ", pool-details=" + liPoolDescription.size() + "\r\n\t  all-package-url=" + liPoolZipUrlAll.size() + ", updated-package-url=" + liPoolZipUrlUpdated.size() + ", 被添加和更新Pool数=" + liPoolLogUpdated.size());
        li.add("");
        li.add("Pool Id  jpeg original all   isexist  status");
        li.add("------------------------------------------------");
        li.addAll(liPoolLogAll);
        FileUtils.writeLines(new File(Constants.W_FULL_PATH_PREFIX + ".log"), "utf-8", li, false);

    }

    private void init() {
        int numJpg = 0;
        int numPng = 0;
        int numAll = 0;
        for (int i = Configuration.getFromPage(); i <= Configuration.getToPage(); i++) {
            PoolLog log = (PoolLog) Runtime.getMapid2log().get(i);
            String pageStatus = log.getStatus();
            liPoolLogAll.add(String.format("# %5d  |" + log.getJpegPackages() + "   |" + log.getOriginalPackages() + "       |" + log.getAllPackages() + "    |" + log.getIsExist() + "   |" + log.getStatus(), log.getId()));
            if (Constants.POOL_STATUS_NULL.equals(pageStatus)) {
                continue;
            }
            liPoolDescription.add(PoolLog.getMapPageId2PoolDescription().get(i));
            liJsonData.add(Runtime.getMapid2jsondata().get(i));
            if (Constants.POOL_STATUS_EMPTY.equals(pageStatus) || Constants.POOL_STATUS_ALL_DELETED.equals(pageStatus)) {
                continue;
            }
            numJpg += log.getJpegPackages();
            numPng += log.getOriginalPackages();
            numAll += log.getAllPackages();
            liPoolIdToZipNumCount.add("To #" + log.getId() + ": " + numAll + " in all, " + numJpg + " jpeg packages and " + numPng + " original packages...");
            if (Constants.POOL_STATUS_MODIFIED.equals(pageStatus) || Constants.POOL_STATUS_NEW.equals(pageStatus)) {
                liPoolLogUpdated.add(String.format("# %5d  |" + log.getJpegPackages() + "   |" + log.getOriginalPackages() + "       |" + log.getAllPackages() + "    |" + log.getIsExist() + "   |" + log.getStatus(), log.getId()));
                for (String url : PoolLog.getMapPageId2ZipLinkPoolUpdated().get(i)) {
                    liPoolZipUrlUpdated.add(getFormatedZipUrl(url));
                }
            }
            for (String url : PoolLog.getMapPageId2ZipLinkPoolAll().get(i)) {
                liPoolZipUrlAll.add(getFormatedZipUrl(url));
            }
        }
    }

    private void validate() {
        boolean isValidateOk = true;
        isValidateOk = isValidateOk && Runtime.getMapid2jsondata().keySet().equals(PoolLog.getMapPageId2PoolDescription().keySet());
        isValidateOk = isValidateOk && liPoolIdToZipNumCount.size() == PoolLog.getMapPageId2ZipLinkPoolAll().size();
        isValidateOk = isValidateOk && liPoolLogUpdated.size() == PoolLog.getMapPageId2ZipLinkPoolUpdated().size();
        isValidateOk = isValidateOk && liPoolLogAll.size() == Configuration.getToPage() - Configuration.getFromPage() + 1;
        int allPageNum = Configuration.getToPage() - Configuration.getFromPage() + 1;
        int logAllPageNumByPageStatus = PoolLog.getPageNumStatus(Constants.POOL_STATUS_NULL) + PoolLog.getPageNumStatus(Constants.POOL_STATUS_EMPTY) + PoolLog.getPageNumStatus(Constants.POOL_STATUS_ALL_DELETED) + PoolLog.getPageNumStatus(Constants.POOL_STATUS_NEW) + PoolLog.getPageNumStatus(Constants.POOL_STATUS_MODIFIED) + PoolLog.getPageNumStatus(Constants.POOL_STATUS_NO_CHANGE);
        isValidateOk = isValidateOk && (allPageNum == logAllPageNumByPageStatus);
        if (!isValidateOk) {
            Logger.fatal("validate failed");
        }
    }

    private void logPoolChangeToStdErr() {
        Logger.info("====================Check zip packages changes========================");
        Logger.info("Pool deleted:");
        int affectedPoolNum = 0;
        int affectedZipNumJpg = 0;
        int affectedZipNumPng = 0;
        int affectedZipNumJpgSum = 0;
        int affectedZipNumPngSum = 0;
        // 比较均只针对于new, modified, no change
        Set<Integer> setLastTimePoolId = PoolUpdatedValidator.getMapLastTimePageId2ZipLinkNumInfo().keySet();
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
                affectedZipNumJpg -= statusPre & 0b01; // jpg位
                affectedZipNumPng -= statusPre >>> 1; // png位
                Logger.info("Pool #" + poolId + " was deleted, status = " + Integer.toBinaryString(statusPre));
            }
        }
        affectedZipNumJpgSum += affectedZipNumJpg; // 负
        affectedZipNumPngSum += affectedZipNumPng; // 负
        Logger.warn("Pool that deleted " + affectedPoolNum + " in all, jpeg packages affected: " + Kit.parseDigitWithSymbol(affectedZipNumJpg, "-") + ", original packages affected: " + Kit.parseDigitWithSymbol(affectedZipNumPng, "-"));
        Logger.info("");
        // 注意重置数据
        affectedPoolNum = 0;
        affectedZipNumJpg = 0;
        affectedZipNumPng = 0;
        Logger.info("Pool new:");
        // PoolId本次更新含有上次更新不含有则认为是新添加, 也可以用日志中status字段判断
        for (Integer poolId : setNowPoolId) {
            if (!setLastTimePoolId.contains(poolId)) {
                Integer statusNow = getNowPageId2ZipLinkNumInfoById(poolId);
                affectedPoolNum++;
                affectedZipNumJpg += statusNow & 0b01;
                affectedZipNumPng += statusNow >>> 1;
                Logger.info("Pool #" + poolId + " is new, status = " + Integer.toBinaryString(statusNow));
            }
        }
        affectedZipNumJpgSum += affectedZipNumJpg; // 正
        affectedZipNumPngSum += affectedZipNumPng; // 正
        Logger.warn("Pool that new " + affectedPoolNum + " in all, jpeg packages affected: " + Kit.parseDigitWithSymbol(affectedZipNumJpg, "+") + ", original packages affected: " + Kit.parseDigitWithSymbol(affectedZipNumPng, "+"));
        Logger.info("");
        affectedPoolNum = 0;
        affectedZipNumJpg = 0;
        affectedZipNumPng = 0;
        // 检查本次修改过的pool中导致
        Logger.info("Pool modified:");
        for (int i = Configuration.getFromPage(); i <= Configuration.getToPage(); i++) {
            PoolLog poolLog = (PoolLog) Runtime.getMapid2log().get(i);
            if (Constants.POOL_STATUS_MODIFIED.equals(poolLog.getStatus())) {
                Integer statusPre = getLastTimePageId2ZipLinkNumInfoById(i);
                Integer statusNow = getNowPageId2ZipLinkNumInfoById(i);
                affectedPoolNum++;
                affectedZipNumJpg += (statusNow & 0b01) - (statusPre & 0b01);
                affectedZipNumPng += (statusNow >>> 1) - (statusPre >>> 1);
                Logger.info("Pool #" + poolLog.getId() + " was modified, status = " + Integer.toBinaryString(statusNow));
                if (!statusNow.equals(statusPre)) {
                    int jpegAffected = (statusNow & 0b01) - (statusPre & 0b01);
                    int pngAffected = (statusNow >>> 1) - (statusPre >>> 1);
                    Logger.warn("Pool #" + poolLog.getId() + " jpeg packages affected " + Kit.parseDigitWithSymbol(jpegAffected, "-") + ", original packages affected " + Kit.parseDigitWithSymbol(pngAffected, "-"));
                }
            }
        }
        affectedZipNumJpgSum += affectedZipNumJpg; // 正、负、零
        affectedZipNumPngSum += affectedZipNumPng; // 正、负、零
        Logger.warn("Pool that modified " + affectedPoolNum + " in all, jpeg packages affected: " + Kit.parseDigitWithSymbol(affectedZipNumJpg, "-") + ", original packages affected: " + Kit.parseDigitWithSymbol(affectedZipNumPng, "-"));
        Logger.info("");
        affectedPoolNum = 0;
        affectedZipNumJpg = 0;
        affectedZipNumPng = 0;
        Logger.info("-------------result-------------");
        Logger.warn("zip num affected: jpeg packages " + Kit.parseDigitWithSymbol(affectedZipNumJpgSum, "-") + " in all, original packages: " + Kit.parseDigitWithSymbol(affectedZipNumPngSum, "-") + " in all");
        // -----------------------------------------------------
        int jpegZipsPre = 0;
        int originalZipsPre = 0;
        int allZipPre = 0;
        for (Integer key : setLastTimePoolId) {
            Integer statusPre = getLastTimePageId2ZipLinkNumInfoById(key);
            jpegZipsPre += statusPre & 0b01;
            originalZipsPre += statusPre >>> 1;
            allZipPre += (statusPre & 0b01) + (statusPre >>> 1);
        }
        Logger.warn("now the zips should be:");
        Logger.warn("all: " + (allZipPre + affectedZipNumJpgSum + affectedZipNumPngSum) + ", jpeg: " + (jpegZipsPre + affectedZipNumJpgSum) + ", original: " + (originalZipsPre + affectedZipNumPngSum));
        int jpegZipsNow = 0;
        int originalZipsNow = 0;
        int allZipNow = 0;
        // 也可以通过PoolLog判断
        for (Integer key : PoolLog.getMapPageId2ZipLinkPoolAll().keySet()) {
            for (String url : PoolLog.getMapPageId2ZipLinkPoolAll().get(key)) {
                if (url.contains(Constants.LINK_POOL_ZIP_SUFFIX_JPG)) {
                    jpegZipsNow++;
                    allZipNow++;
                } else {
                    originalZipsNow++;
                    allZipNow++;
                }
            }
        }
        Logger.warn("all url this time:");
        Logger.warn("all: " + allZipNow + ", jpeg: " + jpegZipsNow + ", original: " + originalZipsNow);
        if(allZipNow != allZipPre + affectedZipNumJpgSum + affectedZipNumPngSum || jpegZipsNow != jpegZipsPre + affectedZipNumJpgSum || originalZipsNow != originalZipsPre + affectedZipNumPngSum){
            throw new RuntimeException("zips url number info predicted not match actual!");
        }
    }

    private Integer getLastTimePageId2ZipLinkNumInfoById(Integer pageId) {
        Integer re = PoolUpdatedValidator.getMapLastTimePageId2ZipLinkNumInfo().get(pageId);
        if (re != 0b10 && re != 0b11) {
            Logger.fatal("Find a error zip pack number info, the page id is " + pageId);
        }
        return re;
    }

    private Integer getNowPageId2ZipLinkNumInfoById(Integer pageId) {
        Integer re = 0;
        PoolLog poolLog = (PoolLog) Runtime.getMapid2log().get(pageId);
        re += poolLog.getJpegPackages();
        re += poolLog.getOriginalPackages() << 1;
        if (re != 0b10 && re != 0b11) {
            Logger.fatal("Find a error zip pack number info, the page id is " + pageId);
        }
        return re;
    }

    /**
     * // https://yande.re/pool/zip/3387/Dengeki%20Moeoh%202014-04%20(JPG).zip?jpeg=1
     * https://yande.re/pool/zip/6?jpeg=1 ,  https://yande.re/pool/zip/6
     * 转为
     * https://yande.re/pool/zip/6?jpeg=1&myPoolId=0006 ,  https://yande.re/pool/zip/6?myPoolId=0006
     * @param url
     * @return String
     */
    private String getFormatedZipUrl(String url) {
        if(!Constants.pattern_zip_link.matcher(url).matches()){
            Logger.fatal("Unrecognized url");
        }
        String[] splitStrArr = url.split("/", 6);
        if(null == splitStrArr || splitStrArr.length != 6){
            throw new RuntimeException("Error Pool url format: " + url);
        }
        String pageIdString = splitStrArr[splitStrArr.length - 1];
        pageIdString = pageIdString.substring(0, pageIdString.indexOf("?") == -1 ? pageIdString.length() : pageIdString.indexOf("?"));
        pageIdString = StringUtils.leftPad(pageIdString, 4, "0");
        if(!StringUtils.isNumeric(pageIdString)){
            throw new RuntimeException("pageId获取失败, " + pageIdString);
        }
        if(url.contains(Constants.LINK_POOL_ZIP_SUFFIX_JPG)){
            url = url + "&myPoolId=" +  pageIdString;
        }else{
            url = url + "?myPoolId=" + pageIdString;
        }
        return url;
        /*int index = Kit.getInnerStrIndex(url, "/", 6);
        String prefix = url.substring(0, index + 1);
        String suffix = url.substring(index + 1);
        String pid = url.split("/", 7)[5].trim();
        String formattedId = ("[" + ("0000" + pid).substring(pid.length()) + "]").trim();
        return Kit.formatUrlLink(prefix + formattedId + suffix.replace("/", "_")).replace(".zip_jpeg%3D1", Constants.LINK_POOL_ZIP_SUFFIX_JPG);*/
    }
}
