package com.zeroq6.moehelper.test;

import com.zeroq6.moehelper.app.App;
import org.junit.Test;

import java.util.Properties;

@SuppressWarnings("unused")
public class AppTest {

    @Test
    public void testApp() {
        String[] args = new String[]{"100664", "100664", "--Post", "--kona"};
        App.main(args);
    }


    @Test
    public void testSystemProperties() {
        Properties props = System.getProperties();
        props.list(System.out);
    }



}
