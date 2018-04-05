package com.zeroq6.moehelper.test;

import com.alibaba.fastjson.JSON;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.*;

/**
 * Created by yuuki asuna on 2018/4/2.
 *
 * 校验文件的压缩包zip和txt记录文件中是否一致
 * 包含总original和jpeg包数量，每个id对应的original和jpeg包数量
 */
public class PoolCountInfoTest
{

    @Test
    public void test() throws Exception{
        String packDir = "F:\\yande.re\\Pool_Packages";
        String txtFile = "D:\\yande.re_-_pool_180331222103_1_4953_info.txt";

        // 读取zip文件名
        Collection<File> list = FileUtils.listFiles(new File(packDir), null, true);
        List<String> nameList = new ArrayList<String>();
        for(File f : list){
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
        for(int i = 0; i<nameList.size();i++){
            String name = nameList.get(i);
            String id = Integer.valueOf(name.substring(1, 5)) + "";
            if(!itemMapPackFile.containsKey(id)){
                itemMapPackFile.put(id, new Item());
            }
            Item item = itemMapPackFile.get(id);
            if(name.contains("(JPG)")){
                jpegAll ++;
                item.setJpeg(item.getJpeg() + 1);
            }else{
                originalAll++;
                item.setOriginal(item.getOriginal() + 1);
            }
            item.setJpegAll(jpegAll);
            item.setOriginalAll(originalAll);
        }

        // 读取txt文件
        List<String> lineList = FileUtils.readLines(new File(txtFile), "utf-8");
        Map<String, Item> itemMapTxtFile = new TreeMap<String, Item>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Integer.valueOf(o1) - Integer.valueOf(o2);
            }
        });
        Item last = null;
        for(int i = 0; i< lineList.size(); i++){
            String line = lineList.get(i);
            if(!line.contains("To #")){
                continue;
            }
            String id = Integer.valueOf(line.substring(line.indexOf("#") + 1, line.indexOf(":"))) + "";
            if(!itemMapTxtFile.containsKey(id)){
                itemMapTxtFile.put(id, new Item());
            }
            Item item = itemMapTxtFile.get(id);
            int originalAllTxt = Integer.valueOf(line.substring(line.indexOf("and ") + 4, line.indexOf(" ori")));
            int jpegAllTxt = Integer.valueOf(line.substring(line.indexOf("all, ") + 5, line.indexOf(" jpeg")));
            int allTxt = Integer.valueOf(line.substring(line.indexOf(": ") + 2, line.indexOf(" in ")));
            if(null == last){
                item.setOriginal(originalAllTxt);
                item.setJpeg(jpegAllTxt);
            }else{
                item.setOriginal(originalAllTxt - last.getOriginalAll());
                item.setJpeg(jpegAllTxt - last.getJpegAll());
            }
            item.setJpegAll(jpegAllTxt);
            item.setOriginalAll(originalAllTxt);

            last = item;
        }



        StringBuffer stringBuffer = new StringBuffer();
        // 校验
        for(Map.Entry<String, Item> entry : itemMapPackFile.entrySet()){
            Item item = itemMapTxtFile.get(entry.getKey());
            if(null == item){
               System.out.println("zip文件含有，txt记录不含有：" + entry.getKey() + "，" + JSON.toJSONString(entry.getValue()));
            }else{
               if(!item.equals(entry.getValue())){
                   stringBuffer.append("zip文件和txt记录不相同，" + entry.getKey() + "，zip文件：" + JSON.toJSONString(entry.getValue()) + ", txt记录：" + JSON.toJSONString(item));
                   stringBuffer.append("\r\n");
               }
               itemMapTxtFile.remove(entry.getKey());
           }

        }

        System.out.println("================================================");
        for (Map.Entry<String, Item> entry : itemMapTxtFile.entrySet()) {
            System.out.println("txt记录含有，zip文件不含有：" + entry.getKey() + "，" + JSON.toJSONString(entry.getValue()));

        }
        System.out.println("================================================");
        System.out.print(stringBuffer.toString());
        System.out.println("================================================");
        System.out.println("校验完成");


    }

    class Item{

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
