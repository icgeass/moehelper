package com.zeroq6.moehelper.test.help;

import org.apache.commons.lang3.StringUtils;

public class ArrangeHelper {

    public static String getFolderName(boolean typeMoe, int start, EndStringType endStringType) {
        String typeString = typeMoe ? "yande.re" : "Konachan.com";
        String format = typeString + "_-_Pack_%s_%s_%s%s";
        return String.format(format, StringUtils.leftPad(start + "", 3, "0"), (start - 1) * 10000 + 1, start * 10000, endStringType.getEndString());
    }


}
