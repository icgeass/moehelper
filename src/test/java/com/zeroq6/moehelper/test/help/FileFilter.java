package com.zeroq6.moehelper.test.help;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class FileFilter {

    private Collection<File> fileCollection = null;


    public FileFilter(String dir, String[] extensions, boolean recursive) {
        fileCollection = FileUtils.listFiles(new File(dir), extensions, recursive);
    }


    public FileFilter(String dir) {
        fileCollection = FileUtils.listFiles(new File(dir), null, true);
    }

    public List<File> filter(Predicate<File> p, Integer acceptNum) {
        List<File> fileList = new ArrayList<>();
        for (File f : fileCollection) {
            if (null == p || p.test(f)) {
                fileList.add(f);
            }
        }
        if (null != acceptNum) {
            if (acceptNum != fileList.size()) {
                throw new RuntimeException("acceptNum(" + acceptNum + ") not equals fileList.size(" + fileList.size() + ")");
            }
        }
        return fileList;
    }

    public List<File> filter(Predicate<File> p) {
        return filter(p, null);
    }

}
