package com.zeroq6.moehelper.test.help;

import com.alibaba.fastjson.JSON;
import com.zeroq6.moehelper.utils.MyLogUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class PoolChecker {


    public static void check(String packDirName, String txtName) throws Exception {

        File packDirFile = new File(packDirName);
        File txtFile = new File(txtName);
        if (!packDirFile.exists() || !txtFile.exists() || !packDirFile.isDirectory() || !txtFile.isFile()) {
            throw new RuntimeException("packDir必须存在为目录，txtFile必须存在为文件");
        }
        if (!packDirFile.getName().equals(ArrangeHelper.packDirName)) {
            throw new RuntimeException("packDirFile文件名不为" + ArrangeHelper.packDirName);
        }
        if (!Pattern.matches("yande[.]re_-_pool_\\d+_1_\\d+_info[.]txt", txtFile.getName())) {
            throw new RuntimeException("txtFile文件名格式错误" + txtFile.getName());
        }

        // 读取zip文件名
        Collection<File> list = FileUtils.listFiles(packDirFile, null, true);
        if (null == list || list.isEmpty()) {
            throw new RuntimeException("packDirFile中没有文件");
        }
        List<String> nameList = new ArrayList<String>();
        Pattern pattern = Pattern.compile("Pool_Packages-\\d{3}[(]id=\\d+-\\d+00[)]");
        for (File f : list) {
            // 校验文件结构
            if (!f.getParentFile().getParentFile().getCanonicalPath().equals(packDirFile.getCanonicalPath())) {
                throw new RuntimeException(f.getCanonicalPath() + "的父目录的父目录不是packDir");
            }
            if (!pattern.matcher(f.getParentFile().getName()).matches()) {
                throw new RuntimeException(f.getCanonicalPath() + "的父目录不匹配正则，" + pattern.pattern());
            }
            nameList.add(f.getName());
        }
        Collections.sort(nameList);
        Map<String, Item> itemMapPackFile = new TreeMap<String, Item>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Integer.valueOf(o1) - Integer.valueOf(o2);
            }
        });
        int jpegAll = 0;
        int originalAll = 0;
        for (int i = 0; i < nameList.size(); i++) {
            String name = nameList.get(i);
            // 校验文件名
            String id = ArrangeHelper.getPoolId(name);
            if (!itemMapPackFile.containsKey(id)) {
                itemMapPackFile.put(id, new Item());
            }
            Item item = itemMapPackFile.get(id);
            if (name.contains("(JPG)")) {
                jpegAll++;
                item.setJpeg(item.getJpeg() + 1);
            } else {
                originalAll++;
                item.setOriginal(item.getOriginal() + 1);
            }
            item.setJpegAll(jpegAll);
            item.setOriginalAll(originalAll);
        }

        // 读取txt文件
        List<String> lineList = FileUtils.readLines(txtFile, "utf-8");
        Map<String, Item> itemMapTxtFile = new TreeMap<String, Item>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Integer.valueOf(o1) - Integer.valueOf(o2);
            }
        });
        Item last = null;
        for (int i = 0; i < lineList.size(); i++) {
            String line = lineList.get(i);
            if (!line.contains("To #")) {
                continue;
            }
            String id = Integer.valueOf(line.substring(line.indexOf("#") + 1, line.indexOf(":"))) + "";
            if (!itemMapTxtFile.containsKey(id)) {
                itemMapTxtFile.put(id, new Item());
            }
            Item item = itemMapTxtFile.get(id);
            int originalAllTxt = Integer.valueOf(line.substring(line.indexOf("and ") + 4, line.indexOf(" original")));
            int jpegAllTxt = Integer.valueOf(line.substring(line.indexOf("all, ") + 5, line.indexOf(" jpeg")));
            if (null == last) {
                item.setOriginal(originalAllTxt);
                item.setJpeg(jpegAllTxt);
            } else {
                item.setOriginal(originalAllTxt - last.getOriginalAll());
                item.setJpeg(jpegAllTxt - last.getJpegAll());
            }
            item.setJpegAll(jpegAllTxt);
            item.setOriginalAll(originalAllTxt);

            last = item;
        }


        StringBuffer stringBuffer = new StringBuffer();
        // 校验
        for (Map.Entry<String, Item> entry : itemMapPackFile.entrySet()) {
            Item item = itemMapTxtFile.get(entry.getKey());
            if (null == item) {
                stringBuffer.append("zip文件含有，txt记录不含有：" + entry.getKey() + "，" + JSON.toJSONString(entry.getValue()));
                stringBuffer.append("\r\n");
            } else {
                if (!item.equals(entry.getValue())) {
                    stringBuffer.append("zip文件和txt记录不相同，" + entry.getKey() + "，zip文件：" + JSON.toJSONString(entry.getValue()) + ", txt记录：" + JSON.toJSONString(item));
                    stringBuffer.append("\r\n");
                }
                itemMapTxtFile.remove(entry.getKey());
            }

        }
        for (Map.Entry<String, Item> entry : itemMapTxtFile.entrySet()) {
            stringBuffer.append("txt记录含有，zip文件不含有：" + entry.getKey() + "，" + JSON.toJSONString(entry.getValue()));
            stringBuffer.append("\r\n");

        }
        MyLogUtils.stdOut(stringBuffer.toString());
        if (stringBuffer.length() == 0) {
            MyLogUtils.stdOut("校验完成：通过");
        } else {
            throw new RuntimeException("校验完成：未通过");
        }


    }

    static class Item {

        private int jpeg = 0;
        private int original = 0;
        private int jpegAll = 0;
        private int originalAll = 0;

        public int getJpeg() {
            return jpeg;
        }

        public void setJpeg(int jpeg) {
            this.jpeg = jpeg;
        }

        public int getOriginal() {
            return original;
        }

        public void setOriginal(int original) {
            this.original = original;
        }

        public int getJpegAll() {
            return jpegAll;
        }

        public void setJpegAll(int jpegAll) {
            this.jpegAll = jpegAll;
        }

        public int getOriginalAll() {
            return originalAll;
        }

        public void setOriginalAll(int originalAll) {
            this.originalAll = originalAll;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Item item = (Item) o;

            if (jpeg != item.jpeg) return false;
            if (original != item.original) return false;
            if (jpegAll != item.jpegAll) return false;
            return originalAll == item.originalAll;
        }

        @Override
        public int hashCode() {
            int result = jpeg;
            result = 31 * result + original;
            result = 31 * result + jpegAll;
            result = 31 * result + originalAll;
            return result;
        }
    }

}
