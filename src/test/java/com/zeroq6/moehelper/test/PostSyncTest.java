package com.zeroq6.moehelper.test;

import com.zeroq6.moehelper.config.Configuration;
import com.zeroq6.moehelper.test.help.EndStringType;
import com.zeroq6.moehelper.test.help.FileFilter;
import com.zeroq6.moehelper.test.help.PostStatistics;
import com.zeroq6.moehelper.test.help.ZipUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class PostSyncTest {


    @Test
    public void test() throws Exception {
        String srcWorkPostDir = "C:\\Users\\yuuki asuna\\Desktop\\!work";
        String targetStorePostDir = "P:\\BaiduDownload";
        FileFilter fileFilter = new FileFilter(srcWorkPostDir);

        for (File item : new File(targetStorePostDir).listFiles()) {
            String name = item.getName();
            final EndStringType endStringType;
            if (!name.contains("_-_Pack_")) {
                continue;
            }
            if (item.isFile() || item.isHidden()) {
                continue;
            }
            //
            if (name.startsWith(Configuration.HOST_KONA)) {
                endStringType = EndStringType.KONA_All;
            } else if (name.startsWith(Configuration.HOST_MOE)) {
                if (name.endsWith(EndStringType.MOE_IN_POOL.getEndString())) {
                    endStringType = EndStringType.MOE_IN_POOL;
                } else if (name.endsWith(EndStringType.MOE_NOT_IN_POOL.getEndString())) {
                    endStringType = EndStringType.MOE_NOT_IN_POOL;
                } else {
                    throw new RuntimeException("非法文件夹名" + name);
                }
            } else {
                throw new RuntimeException("非法文件夹名" + name);
            }
            String[] splitArray = name.split("[_()]");
            int start = Integer.valueOf(splitArray[4]);
            int end = Integer.valueOf(splitArray[5]);


            // 复制md5文件
            List<File> md5File = fileFilter.filter(file -> file.getName().startsWith(endStringType.getHost())
                    && file.getName().contains("_" + start + "_" + end)
                    && file.getName().endsWith(endStringType.getEndStringInMd5File()), 1);
            FileUtils.copyFileToDirectory(md5File.get(0), item);
            System.out.println("MD5文件复制完成：" + md5File.get(0).getCanonicalPath());



            // 打包关联work文件
            List<File> workFileList = fileFilter.filter(file -> file.getName().startsWith(endStringType.getHost())
                    && file.getName().contains("_" + start + "_" + end));
            if (workFileList.size() != 11 && workFileList.size() != 12) {
                throw new RuntimeException("workFileList != 11 and workFileList != 12");
            }
            File zipFile = new File(item, item.getName() + "_data.zip");
            ZipUtils.zipFileFolders(zipFile, workFileList, item.getName() + "_data");
            System.out.println("打包成功: " + zipFile.getCanonicalPath());

            // 校验是否存在多余文件
            Collection<File> imageFileList = FileUtils.listFiles(item, new String[]{"jpg", "jpeg", "gif", "png", "swf"}, false);
            Collection<File> imageFileWithMd5ZipList = FileUtils.listFiles(item, null, true);
            if (imageFileList.size() != imageFileWithMd5ZipList.size() && imageFileList.size() + 2 != imageFileWithMd5ZipList.size()) {
                throw new RuntimeException("imageFileList.size(or +2) != imageFileWithMd5ZipList.size");
            }

            // 校验文件数量是否与日志文件中一致
            PostStatistics postStatistics = new PostStatistics(srcWorkPostDir + File.separator + endStringType.getHost() + File.separator + "post", start, end);
            // 排除链接中下载404的
            int imageCountInLogFile = postStatistics.getDetailAllCountSuccessJson() + postStatistics.getDetailAllCountSuccessDocPostDeleted();
            if (endStringType == EndStringType.MOE_IN_POOL) {
                imageCountInLogFile = postStatistics.getPoolCountSuccessJsonInPool();
            } else if (endStringType == EndStringType.MOE_NOT_IN_POOL) {
                imageCountInLogFile = postStatistics.getPoolCountSuccessJsonNotInPool() + postStatistics.getPoolCountSuccessDocPostDeletedInPool() + postStatistics.getPoolCountSuccessDocPostDeletedNotInPool();
            }


            if (endStringType == EndStringType.KONA_All || endStringType == EndStringType.MOE_NOT_IN_POOL){
                List<File> file404List = fileFilter.filter(file -> file.getName().startsWith(endStringType.getHost())
                        && file.getName().contains("_404_")
                        && file.getName().contains("_" + start + "_" + end));
                if (file404List.size() == 1) {
                    String file404Name = file404List.get(0).getName();
                    imageCountInLogFile -= Integer.valueOf(file404Name.substring(file404Name.lastIndexOf("_") + 1, file404Name.lastIndexOf(".")));
                }
            }
            if (imageFileList.size() != imageCountInLogFile) {
                throw new RuntimeException("imageFile in disk = " + imageFileList.size() + ", in log file = " + imageCountInLogFile);
            }
            System.out.println("同步验证通过，" + name);


        }
        System.out.println("---------------------END---------------------");
    }


}
