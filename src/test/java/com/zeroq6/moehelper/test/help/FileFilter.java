package com.zeroq6.moehelper.test.help;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class FileFilter{

    private Collection<File> fileCollection = null;



    public FileFilter(String dir, String[] extensions, boolean recursive){
        fileCollection = FileUtils.listFiles(new File(dir), extensions, recursive);
    }

    public List<File> filter(Predicate<File> p){
        List<File> fileList = new ArrayList<>();
        for(File f : fileCollection){
            if(p.test(f)){
                fileList.add(f);
            }
        }
        return fileList;
    }

}
