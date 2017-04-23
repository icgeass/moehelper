package com.zeroq6.moehelper.writer;

import com.zeroq6.moehelper.config.Configuration;

/**
 * 文件写入对象接口
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public interface Writer {


    String W_ROOT_DIR = Configuration.getHost();
    String W_WRITE_DIR = "./" + W_ROOT_DIR + "/" + Configuration.getLinkType();
    String W_FILE_NAME_PREFIX = Configuration.getHost() + "_-_" + Configuration.getLinkType() + "_" + Configuration.getBeginTime() + "_" + Configuration.getFromPage() + "_" + Configuration.getToPage();
    String W_FULL_PATH_PREFIX = W_WRITE_DIR + "/" + W_FILE_NAME_PREFIX;

    void writeToFile();
}
