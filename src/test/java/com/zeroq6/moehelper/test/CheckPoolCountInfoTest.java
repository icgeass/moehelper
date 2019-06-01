package com.zeroq6.moehelper.test;

import com.alibaba.fastjson.JSON;
import com.zeroq6.moehelper.test.help.ArrangeHelper;
import com.zeroq6.moehelper.test.help.PoolChecker;
import com.zeroq6.moehelper.utils.MyLogUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by yuuki asuna on 2018/4/2.
 * <p>
 * 校验文件的压缩包zip和txt记录文件中是否一致
 * 包含总original和jpeg包数量，每个id对应的original和jpeg包数量
 */
public class CheckPoolCountInfoTest {

    @Test
    public void test() throws Exception {
        String packDir = "I:\\yande.re\\Pool_Packages";
        String txtFile = "C:\\Users\\yuuki asuna\\Desktop\\workspace\\yande.re\\pool\\yande.re_-_pool_190517065816_1_6301_info.txt";
        PoolChecker.check(packDir, txtFile);

    }
}
