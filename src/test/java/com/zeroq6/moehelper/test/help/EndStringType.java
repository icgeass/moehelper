package com.zeroq6.moehelper.test.help;

public enum EndStringType {

    KONA_EMPTY(""), MOE_IN_POOL("(In_Pool)"), MOE_NOT_IN_POOL("(Not_In_Pool)");

    private String endString;

    EndStringType(String endString) {
        this.endString = endString;
    }

    public String getEndString() {
        return endString;
    }
}

