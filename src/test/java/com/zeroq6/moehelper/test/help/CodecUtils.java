package com.zeroq6.moehelper.test.help;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

public class CodecUtils {


    public static String getFileListSha1(Collection<File> fileList) throws IOException {
        if (CollectionUtils.isEmpty(fileList)) {
            throw new RuntimeException("fileList不能为空");
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (File file : fileList) {
            if (file.isDirectory()) {
                throw new RuntimeException("fileList中不能有文件夹");
            }
            stringBuilder.append(file.getName() + "_");
            FileInputStream fis = new FileInputStream(file);
            stringBuilder.append(DigestUtils.sha1Hex(fis) + "|");
            CloseUtils.closeSilent(fis);
        }
        return DigestUtils.sha1Hex(stringBuilder.toString());
    }
}
