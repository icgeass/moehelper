package com.zeroq6.moehelper.test.help;

import com.zeroq6.moehelper.config.Configuration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WorkSpaceValidator {

    private static String[] postWorkFileNameSuffix = new String[]{"0000.deleted_post.json", "0000.json", "0000_post.log", "0000_post_all.lst",
            "0000_post_all.md5", "0000_post_in_pool.lst", "0000_post_in_pool.md5", "0000_post_no_pool.lst", "0000_post_no_pool.md5",
            "0000_stderr.txt", "0000_stdout.txt"};

    private static String[] poolWorkFileNameSuffix = new String[]{".json", ".log", "_info.txt", "_info_details.txt",
            "_info_updated.txt", "_packages_updated_url.lst", "_packages_url.lst", "_stderr.txt", "_stdout.txt"};


    public static void checkAndSetReadOnlyWorkSpaceDir(String srcWorkSpaceDir) throws Exception {


        FileFilter fileFilter = new FileFilter(srcWorkSpaceDir);

        // 设置workspace里面的文件只读
        fileFilter.filter(null).stream().forEach(file -> {
            if (!"NOTICE".equals(file.getName())) {
                file.setReadOnly();
            }
        });

        // 校验post,pool文件数量
        if (fileFilter.filter(file -> file.getParentFile().getName().equals(Configuration.POST)).size() % 11 != 0) {
            throw new RuntimeException(Configuration.POST + "记录文件数量不能被11整除");
        }
        if (fileFilter.filter(file -> file.getParentFile().getName().equals(Configuration.POOL)).size() % 9 != 0) {
            throw new RuntimeException(Configuration.POOL + "记录文件数量不能被9整除");
        }


        // 开始校验post文件夹
        List<File> postDirList = new ArrayList<File>() {{
            add(new File(fileFilter.getRootDir().getCanonicalPath() + File.separator + Configuration.HOST_MOE + File.separator + Configuration.POST));
            add(new File(fileFilter.getRootDir().getCanonicalPath() + File.separator + Configuration.HOST_KONA + File.separator + Configuration.POST));

        }};


        // post文件夹
        for (File dir : postDirList) {
            PostStatistics postStatistics = new PostStatistics(dir.getCanonicalPath());
            if (FileUtils.listFiles(dir, null, true).size() != postStatistics.getMaxPostId() / 10000 * 11) {
                throw new RuntimeException("post目录中文件数不等于(w)s*11");
            }
            // 每1w文件
            for (int i = 1; i <= postStatistics.getMaxPostId() / 10000; i++) {
                // 每1w的workFile
                final int start = (i - 1) * 10000 + 1;
                final int end = i * 10000;
                List<File> postWorkFileList = fileFilter.filter(file -> {
                    try {
                        return file.getName().contains("_" + start + "_" + end) && file.getParentFile().getCanonicalPath().equals(dir.getCanonicalPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });
                int postWorkFileNum = 0;
                for (File workFile : postWorkFileList) {
                    String workFileName = workFile.getName();
                    for (String suffix : postWorkFileNameSuffix) {
                        if (workFileName.endsWith(suffix)) {
                            postWorkFileNum++;
                        }
                    }

                }
                if (postWorkFileNum != postWorkFileNameSuffix.length) {
                    throw new RuntimeException("postWorkFileNum错误");
                }
            }

        }

        // 开始校验pool文件夹

        FileFilter poolFilter = new FileFilter(fileFilter.getRootDir().getCanonicalPath() + File.separator + Configuration.HOST_MOE + File.separator + Configuration.POOL);
        int poolWorkFileTypeNum = 0;
        int times = -1;
        for (String suffix : poolWorkFileNameSuffix) {
            List<File> fileList = poolFilter.filter(file -> file.getName().endsWith(suffix));
            if (times == -1) {
                times = fileList.size();
            } else if (times != fileList.size()) {
                throw new RuntimeException("pool同步更新文件数量不一致，times = " + times + ", fileList.size = " + fileList.size());
            }
            if (fileList.size() != 0) {
                poolWorkFileTypeNum++;
            }
        }
        if (poolWorkFileTypeNum != poolWorkFileNameSuffix.length) {
            throw new RuntimeException("poolWorkFileTypeNum错误");
        }


    }

}
