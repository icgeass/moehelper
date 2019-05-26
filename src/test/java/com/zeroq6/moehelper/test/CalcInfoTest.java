package com.zeroq6.moehelper.test;

import com.zeroq6.moehelper.test.help.PostStatistics;
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
        String logFilesDir = "C:\\Users\\yuuki asuna\\Desktop\\!work\\konachan.com\\post";
        // ------------------------------------

        PostStatistics postStatistics = new PostStatistics(logFilesDir, 50001, 60000);
        System.out.println(postStatistics.toString());
    }
}
