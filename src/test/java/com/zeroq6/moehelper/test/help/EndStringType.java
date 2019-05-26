package com.zeroq6.moehelper.test.help;

import com.zeroq6.moehelper.config.Configuration;

public enum EndStringType {


    KONA_EMPTY("", Configuration.HOST_KONA), MOE_IN_POOL("(In_Pool)", Configuration.HOST_MOE), MOE_NOT_IN_POOL("(Not_In_Pool)", Configuration.HOST_MOE);

    private String endString;

    private String host;

    EndStringType(String endString, String host) {
        this.endString = endString;
        this.host = host;
    }

    public String getEndString() {
        return endString;
    }

    public String getHost() {
        return host;
    }
}

