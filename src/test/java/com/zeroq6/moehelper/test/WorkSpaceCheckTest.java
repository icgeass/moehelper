package com.zeroq6.moehelper.test;

import com.zeroq6.moehelper.test.help.WorkSpaceValidator;
import org.junit.Test;

public class WorkSpaceCheckTest {


    @Test
    public void checkWorkSpace() throws Exception {
        WorkSpaceValidator.checkAndSetReadOnlyWorkSpaceDir("C:\\Users\\yuuki asuna\\Desktop\\workspace");
    }

}
