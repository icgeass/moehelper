package com.zeroq6.moehelper.test;

import org.apache.commons.lang3.StringUtils;
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
                File f1 = new File(parentDir, getFolderName(typeMoe, i, EndStringType.MOE_IN_POOL));
                f1.mkdirs();

                File f2 = new File(parentDir, getFolderName(typeMoe, i, EndStringType.MOE_NOT_IN_POOL));
                f2.mkdirs();

                System.out.println(f1.getCanonicalPath());
                System.out.println(f2.getCanonicalPath());
            } else {
                File f3 = new File(parentDir, getFolderName(typeMoe, i, EndStringType.KONA_EMPTY));
                f3.mkdirs();

                System.out.println(f3.getCanonicalPath());
            }
        }

    }

    enum EndStringType {

        KONA_EMPTY(""), MOE_IN_POOL("(In_Pool)"), MOE_NOT_IN_POOL("(Not_In_Pool)");

        private String endString;

        EndStringType(String endString) {
            this.endString = endString;
        }
    }

    private String getFolderName(boolean typeMoe, int start, EndStringType endStringType) {
        String typeString = typeMoe ? "yande.re" : "Konachan.com";
        String format = typeString + "_-_Pack_%s_%s_%s%s";
        return String.format(format, StringUtils.leftPad(start + "", 3, "0"), (start - 1) * 10000 + 1, start * 10000, endStringType.endString);
    }


}
