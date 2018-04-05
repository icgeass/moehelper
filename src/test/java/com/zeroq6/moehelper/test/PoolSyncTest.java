package com.zeroq6.moehelper.test;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by yuuki asuna on 2017/6/21.
 */
public class PoolSyncTest {


    /**
     * 同步pool
     * 删除的手动处理
     * @throws Exception
     */
    @Test
    public void sync() throws Exception{
        // 最近更新的pool
        File newDir = new File("F:\\yande.re\\yande.re_-_Pool_20180210");
        // 以前更新的pool，如果是最终移动到Pool_Packages，需要设定为Pool_Packages目录
        File oldDir = new File("F:\\yande.re\\Pool_Packages");

        // 最终移动到Pool_Packages时设置为true
        boolean genIdDir = true;
        ///////////
        String[] suffix = new String[]{"zip"};
        if (!oldDir.isDirectory() || !newDir.isDirectory()) {
            throw new RuntimeException("新旧文件都必须为目录");
        }
        // 校验文件名
        String reg = "yande[.]re_[-]_Pool_\\d{8}";
        if(!Pattern.matches(reg, newDir.getName())){
            throw new RuntimeException("新目录名必须满足正则" + reg);
        }
        if(genIdDir){
            String poolPackages = "Pool_Packages";
            if(!oldDir.getName().equals(poolPackages)){
                throw new RuntimeException("旧目录名必须为" + poolPackages);
            }
        }else{
            if(!Pattern.matches(reg, oldDir.getName())){
                throw new RuntimeException("旧目录名必须满足正则" + reg);
            }
            if(newDir.getName().compareTo(oldDir.getName()) <= 0){
                throw new RuntimeException("新目录比较必须大于旧目录");
            }
        }
        // 校验空间
        if(FileUtils.sizeOfDirectory(newDir) >= oldDir.getFreeSpace() - (FileUtils.ONE_GB * 10)){
            throw new RuntimeException("目标空间不足");
        }
        Map<String, List<File>> newMap = transferIdFileMap(FileUtils.listFiles(newDir, suffix, true));
        Map<String, List<File>> oldMap = transferIdFileMap(FileUtils.listFiles(oldDir, suffix, true));
        String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        // 更新和新增
        for (Map.Entry<String, List<File>> item : newMap.entrySet()) {
            List<File> old = oldMap.get(item.getKey());
            if (null == old) {
                for(File f : item.getValue()){
                    FileUtils.moveFileToDirectory(f, genIdDir ? genMoveToDirById(Integer.valueOf(item.getKey()), oldDir) : oldDir, true);
                    System.out.println("new新增: " + f.getAbsolutePath());
                }
            } else {
                // 先移除, 后新增, 避免文件名同步无法新增或错误覆盖
                for(File f : old){
                    System.out.println("update移除: " + f.getAbsolutePath());
                    FileUtils.moveFileToDirectory(f, new File(oldDir.getParentFile().getCanonicalPath() + File.separator + time + ".updated"), true);
                }
                for(File f : item.getValue()){
                    System.out.println("update新增: " + f.getAbsolutePath());
                    FileUtils.moveFileToDirectory(f, genIdDir ? genMoveToDirById(Integer.valueOf(item.getKey()), oldDir) : oldDir, true);
                }
            }
        }


    }

    private File genMoveToDirById(int id, File toDir) throws Exception{
        if (id < 1) {
            throw new RuntimeException("id非法, " + id);
        }
        int index = id % 600 == 0 ? id / 600 : id / 600 + 1;
        int a = (index - 1) * 600 + 1;
        int b = index * 600;
        String name = String.format("Pool_Packages-%s(id=%s-%s)", StringUtils.leftPad(index+"",2,"0"), a + "", b + "");
        return new File(toDir.getCanonicalPath() + File.separator + name);

    }

    private Map<String, List<File>> transferIdFileMap(Collection<File> fileCollection) {
        Iterator<File> iterator = fileCollection.iterator();
        Pattern pattern = Pattern.compile("\\[[0-9]{4}\\].*[.]zip");
        Map<String, List<File>> result = new HashMap<String, List<File>>();
        while (iterator.hasNext()) {
            File file = iterator.next();
            String name = file.getName();
            if (!pattern.matcher(name).matches()) {
                throw new RuntimeException("非法文件名, " + name);
            }
            String id = Integer.valueOf(name.substring(1, 5)) + "";
            if(null == result.get(id)){
                result.put(id, new ArrayList<File>());
            }
            result.get(id).add(file);
        }
        return result;
    }
}
