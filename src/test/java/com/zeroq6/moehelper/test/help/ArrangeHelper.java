package com.zeroq6.moehelper.test.help;

import org.apache.commons.lang3.StringUtils;

public class ArrangeHelper {

    public static String getFolderName(int startInTenThousand, EndStringType endStringType) {
        String typeString = endStringType.getHost();
        String format = typeString + "_-_Pack_%s_%s_%s%s";
        return String.format(format, StringUtils.leftPad(startInTenThousand + "", 3, "0"), (startInTenThousand - 1) * 10000 + 1, startInTenThousand * 10000, endStringType.getEndString());
    }



}
