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
        File fromDir = new File("F:\\yande.re.pool2");
        File toDir = new File("F:\\yande.re\\Pool_Packages");
        ///////////
        String[] suffix = new String[]{"zip"};
        final String toDirName = "Pool_Packages";
        if (!fromDir.isDirectory()) {
            throw new RuntimeException("来源非法");
        }
        if (!toDir.getName().equals(toDirName) || !toDir.isDirectory()) {
            throw new RuntimeException("目标非法");
        }
        if(FileUtils.sizeOfDirectory(fromDir) >= toDir.getFreeSpace() + FileUtils.ONE_GB){
            throw new RuntimeException("目标空间不足");
        }
        Map<String, List<File>> fromMap = transferIdFileMap(FileUtils.listFiles(fromDir, suffix, true));
        Map<String, List<File>> toMap = transferIdFileMap(FileUtils.listFiles(toDir, suffix, true));
        String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        // 更新和新增
        for (Map.Entry<String, List<File>> item : fromMap.entrySet()) {
            List<File> to = toMap.get(item.getKey());
            if (null == to) {
                for(File f : item.getValue()){
                    FileUtils.moveFileToDirectory(f, genMoveToDirById(Integer.valueOf(item.getKey()), toDir), true);
                    System.out.println("new新增: " + f.getAbsolutePath());
                }
            } else {
                // 先移除, 后新增, 避免文件名同步无法新增或错误覆盖
                for(File f : to){
                    System.out.println("update移除: " + f.getAbsolutePath());
                    FileUtils.moveFileToDirectory(f, new File(toDir.getParentFile().getCanonicalPath() + File.separator + time + ".updated"), true);
                }
                for(File f : item.getValue()){
                    System.out.println("update新增: " + f.getAbsolutePath());
                    FileUtils.moveFileToDirectory(f, genMoveToDirById(Integer.valueOf(item.getKey()), toDir), true);
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
