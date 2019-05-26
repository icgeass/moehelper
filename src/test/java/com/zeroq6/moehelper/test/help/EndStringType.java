package com.zeroq6.moehelper.test.help;

import com.zeroq6.moehelper.config.Configuration;

public enum EndStringType {


    KONA_All("", Configuration.HOST_KONA, "_post_all.md5"),
    KONA_IN_POOL("(In_Pool)", Configuration.HOST_KONA, "_post_in_pool.md5"),
    KONA_NOT_IN_POOL("(Not_In_Pool)", Configuration.HOST_KONA, "_post_no_pool.md5"),
    MOE_ALL("", Configuration.HOST_MOE, "_post_all.md5"),
    MOE_IN_POOL("(In_Pool)", Configuration.HOST_MOE, "_post_in_pool.md5"),
    MOE_NOT_IN_POOL("(Not_In_Pool)", Configuration.HOST_MOE, "_post_no_pool.md5");

    private String endString;

    private String host;

    private String endStringInMd5File;

    EndStringType(String endString, String host, String endStringInMd5File) {
        this.endString = endString;
        this.host = host;
        this.endStringInMd5File = endStringInMd5File;
    }

    public String getEndString() {
        return endString;
    }

    public String getHost() {
        return host;
    }

    public String getEndStringInMd5File() {
        return endStringInMd5File;
    }
}

