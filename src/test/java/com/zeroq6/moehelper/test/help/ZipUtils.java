package com.zeroq6.moehelper.test.help;


import com.alibaba.fastjson.JSON;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author
 * @date 2018/8/14
 */
public class ZipUtils {


    public static void zipFolders(File zipFile, List<String> folders, String parentDirectoryInZipFile) throws Exception {
        try {
            if (null == folders || folders.isEmpty()) {
                throw new RuntimeException("folders不能为空");
            }
            // 获取文件，去重
            List<String> absPathList = new ArrayList<String>();
            Collection<File> files = new LinkedList<File>();
            for (String folder : folders) {
                File f = new File(folder);
                if (!f.exists()) {
                    throw new IllegalArgumentException("文件（夹）不存在: " + f.getAbsolutePath());
                }
                if (f.isFile()) {
                    if (!absPathList.contains(f.getCanonicalPath())) {
                        files.add(f);
                        absPathList.add(f.getCanonicalPath());
                    }
                } else if (f.isDirectory()) {
                    for (File fileInFolder : FileUtils.listFiles(f, null, true)) {
                        if (!absPathList.contains(fileInFolder.getCanonicalPath())) {
                            files.add(fileInFolder);
                            absPathList.add(fileInFolder.getCanonicalPath());
                        }
                    }

                } else {
                    throw new RuntimeException("非法路径: " + f.getAbsolutePath());
                }
            }
            System.out.println("当前备份文件列表：" + JSON.toJSONString(absPathList));
            OutputStream out = null;
            ArchiveOutputStream os = null;
            InputStream is = null;

            try {
                File parent = zipFile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                out = new FileOutputStream(zipFile);

                os = new ArchiveStreamFactory().createArchiveOutputStream("zip", out);
                Iterator<File> iterator = files.iterator();

                while (iterator.hasNext()) {
                    File f = iterator.next();


                    //
                    ZipArchiveEntry zipArchiveEntry = null;
                    if (StringUtils.isBlank(parentDirectoryInZipFile)) {
                        String path = f.getAbsolutePath();
                        zipArchiveEntry = new ZipArchiveEntry(path);
                    } else {
                        zipArchiveEntry = new ZipArchiveEntry(f, parentDirectoryInZipFile + "/" + f.getName());

                    }
                    os.putArchiveEntry(zipArchiveEntry);
                    is = new FileInputStream(f);
                    IOUtils.copy(is, os);
                    os.closeArchiveEntry();
                    //
                    CloseUtils.closeSilent(is);
                }
                out.flush();
            } finally {
                CloseUtils.closeSilent(os);
                CloseUtils.closeSilent(out);
                CloseUtils.closeSilent(is);
            }


        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    public static void zipFileFolders(File zipFile, List<File> folders, String parentDirectoryInZipFile) throws Exception {
        zipFolders(zipFile,  folders.stream().map(File::getAbsolutePath).collect(Collectors.toList()), parentDirectoryInZipFile);
    }




}
