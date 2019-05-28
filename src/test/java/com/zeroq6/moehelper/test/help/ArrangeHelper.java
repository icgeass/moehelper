package com.zeroq6.moehelper.test.help;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class ArrangeHelper {

    public static String getFolderName(int startInTenThousand, EndStringType endStringType) {
        String typeString = endStringType.getHost();
        String format = typeString + "_-_Pack_%s_%s_%s%s";
        return String.format(format, StringUtils.leftPad(startInTenThousand + "", 3, "0"), (startInTenThousand - 1) * 10000 + 1, startInTenThousand * 10000, endStringType.getEndString());
    }


    /**
     * 不带前缀0
     *
     * @param name
     * @return
     */
    public static String getPoolId(String name) {
        if (StringUtils.isBlank(name)) {
            throw new RuntimeException("文件名不能为空，" + name);
        }
        Pattern pattern = Pattern.compile("^\\[[0-9]{4,}\\].*[.]zip$");
        if (!pattern.matcher(name).matches()) {
            throw new RuntimeException("非法文件名, " + name);
        }
        return Integer.valueOf(name.substring(name.indexOf("[") + 1, name.indexOf("]"))) + "";
    }


}
