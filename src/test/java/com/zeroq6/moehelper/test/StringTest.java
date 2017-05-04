package com.zeroq6.moehelper.test;

import com.zeroq6.moehelper.utils.MyStringUtils;
import org.junit.Test;

import java.net.URLDecoder;

/**
 * Created by zengshengxin on 2017/4/26.
 */
public class StringTest
{

    @Test
    public void test() throws Exception{
        String source = "https://yande.re/jpeg/eced93c44306c8f95bda9e47cfc0d5ef/yande.re%20268050%20bikini%20cleavage%20fukahire_sanba%20ruinon%20swimsuits.jpg";
        String charString = "/";
        int a= 100;

        int index = MyStringUtils.getInnerStrIndex(source, charString, 5);
        System.out.println(URLDecoder.decode(source.substring(index + 1), "utf-8"));
    }
}
