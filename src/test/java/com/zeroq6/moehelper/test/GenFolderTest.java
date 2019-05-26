package com.zeroq6.moehelper.test;

import com.zeroq6.moehelper.test.help.ArrangeHelper;
import com.zeroq6.moehelper.test.help.EndStringType;
import org.junit.Test;

import java.io.File;

public class GenFolderTest {


    /**
     * 在parentDir下生成生成类似
     * yande.re_-_Pack_049_480001_490000(In_Pool)
     * yande.re_-_Pack_049_480001_490000(Not_In_Pool)
     * Konachan.com_-_Pack_028_270001_280000
     * 的文件夹
     *
     * @throws Exception
     */
    @Test
    public void genFolder() throws Exception {
        String parentDir = "F:\\moe_post";
        int from = 28;
        int to = 28;
        boolean typeMoe = false;
        for (int i = from; i <= to; i++) {
            if (typeMoe) {
                File f1 = new File(parentDir, ArrangeHelper.getFolderName(typeMoe, i, EndStringType.MOE_IN_POOL));
                f1.mkdirs();

                File f2 = new File(parentDir, ArrangeHelper.getFolderName(typeMoe, i, EndStringType.MOE_NOT_IN_POOL));
                f2.mkdirs();

                System.out.println(f1.getCanonicalPath());
                System.out.println(f2.getCanonicalPath());
            } else {
                File f3 = new File(parentDir, ArrangeHelper.getFolderName(typeMoe, i, EndStringType.KONA_EMPTY));
                f3.mkdirs();

                System.out.println(f3.getCanonicalPath());
            }
        }

    }




}
