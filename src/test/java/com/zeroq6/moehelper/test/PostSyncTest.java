package com.zeroq6.moehelper.test;

import com.zeroq6.moehelper.config.Configuration;
import com.zeroq6.moehelper.test.help.*;
import com.zeroq6.moehelper.utils.MyLogUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.List;


public class PostSyncTest {


    @Test
    public void test() throws Exception {
        String srcWorkSpaceDir = "C:\\Users\\yuuki asuna\\Desktop\\workspace";
        String[] targetStorePostDirArray = new String[]{"P:\\BaiduDownload"};
        for (String item : targetStorePostDirArray) {
            syncAndCheck(srcWorkSpaceDir, item);
        }
    }


    public static void syncAndCheck(String srcWorkSpaceDir, String targetStorePostDir) throws Exception {

        FileFilter fileFilter = new FileFilter(srcWorkSpaceDir);


        // 校验文件
        WorkSpaceValidator.checkAndSetReadOnlyWorkSpaceDir(srcWorkSpaceDir);
        MyLogUtils.stdOut(srcWorkSpaceDir + "\t校验成功");

        File[] files = new File(targetStorePostDir).listFiles();
        if (null == files || files.length == 0) {
            return;
        }
        MyLogUtils.stdOut("---------------------BEGIN---------------------");
        for (File item : files) {
            String name = item.getName();
            final EndStringType endStringType;
            if (!name.contains("_-_Pack_")) {
                continue;
            }
            if (item.isFile()) {
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
            MyLogUtils.stdOut("MD5文件复制完成：" + md5File.get(0).getCanonicalPath());


            // 打包关联work文件
            List<File> workFileList = fileFilter.filter(file -> file.getName().startsWith(endStringType.getHost())
                    && file.getName().contains("_" + start + "_" + end));
            if (workFileList.size() != 11 && workFileList.size() != 12 && workFileList.size() != 13) {
                throw new RuntimeException("workFileList.size() != 11 && workFileList.size() != 12 && workFileList.size() != 13");
            }
            File zipFile = new File(item, item.getName() + "_data.zip");
            ZipUtils.zipFileFolders(zipFile, workFileList, item.getName() + "_data");
            MyLogUtils.stdOut("打包成功: " + zipFile.getCanonicalPath());

            // 校验是否存在多余文件
            Collection<File> imageFileList = FileUtils.listFiles(item, new String[]{"jpg", "jpeg", "gif", "png", "swf"}, false);
            Collection<File> imageFileWithMd5ZipList = FileUtils.listFiles(item, null, true);
            // 设置所有文件包括MD5,图片文件,zip打包工作文件只读
            imageFileWithMd5ZipList.stream().forEach(file -> file.setReadOnly());
            if (imageFileList.size() != imageFileWithMd5ZipList.size() && imageFileList.size() + 2 != imageFileWithMd5ZipList.size()) {
                throw new RuntimeException("imageFileList.size(or +2) != imageFileWithMd5ZipList.size");
            }

            // 校验文件数量是否与日志文件中一致
            PostStatistics postStatistics = new PostStatistics(srcWorkSpaceDir + File.separator + endStringType.getHost() + File.separator + "post", start, end);
            // 排除链接中下载404的
            int imageCountInLogFile = postStatistics.getDetailAllCountSuccessJson() + postStatistics.getDetailAllCountSuccessDocPostDeleted();
            if (endStringType == EndStringType.MOE_IN_POOL) {
                imageCountInLogFile = postStatistics.getPoolCountSuccessJsonInPool();
            } else if (endStringType == EndStringType.MOE_NOT_IN_POOL) {
                imageCountInLogFile = postStatistics.getPoolCountSuccessJsonNotInPool() + postStatistics.getPoolCountSuccessDocPostDeletedInPool() + postStatistics.getPoolCountSuccessDocPostDeletedNotInPool();
            }


            if (endStringType == EndStringType.KONA_All || endStringType == EndStringType.MOE_NOT_IN_POOL) {
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
            MyLogUtils.stdOut("同步验证通过，" + name);


        }
        MyLogUtils.stdOut("---------------------END---------------------");
    }


}
