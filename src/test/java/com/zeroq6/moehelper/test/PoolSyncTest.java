package com.zeroq6.moehelper.test;

import com.zeroq6.moehelper.test.help.ArrangeHelper;
import com.zeroq6.moehelper.test.help.PoolChecker;
import com.zeroq6.moehelper.test.help.WorkSpaceValidator;
import com.zeroq6.moehelper.utils.MyLogUtils;
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
     * oldDir中被替换回来的，会放置在oldDir同级目录下的，oldDir_yyyyMMddHHmmss_to_delete，确认无误，手动删除即可
     *
     * @throws Exception
     */
    @Test
    public void sync() throws Exception {
        // 最近更新的pool
        File newDir = new File("I:\\yande.re\\yande.re_-_Pool_20190521");
        // 以前更新的pool，如果是最终移动到Pool_Packages，需要设定为Pool_Packages目录
        File oldDir = new File("I:\\yande.re\\Pool_Packages");

        // 如果finalToPoolPackages = true;则必须设置
        String txtFile = "C:\\Users\\yuuki asuna\\Desktop\\workspace\\yande.re\\pool\\yande.re_-_pool_190517065816_1_6301_info.txt";

        // 最终移动到Pool_Packages时设置为true
        boolean finalToPoolPackages = true;
        ///////////
        String[] suffix = new String[]{"zip"};
        if (!oldDir.isDirectory() || !newDir.isDirectory()) {
            throw new RuntimeException("新旧文件都必须为目录");
        }
        // 校验文件名
        String reg = "yande[.]re_[-]_Pool_\\d{8}";
        if (!Pattern.matches(reg, newDir.getName())) {
            throw new RuntimeException("新目录名必须满足正则" + reg);
        }
        if (finalToPoolPackages) {
            String poolPackages = ArrangeHelper.POOL_PACKAGES_DIR_NAME;
            if (!oldDir.getName().equals(poolPackages)) {
                throw new RuntimeException("旧目录名必须为" + poolPackages);
            }
            // 事先校验
            PoolChecker.check(oldDir.getCanonicalPath(), txtFile);
        } else {
            if (!Pattern.matches(reg, oldDir.getName())) {
                throw new RuntimeException("旧目录名必须满足正则" + reg);
            }
            if (newDir.getName().compareTo(oldDir.getName()) <= 0) {
                throw new RuntimeException("新目录比较必须大于旧目录");
            }
        }
        // 校验空间
        if (FileUtils.sizeOfDirectory(newDir) >= oldDir.getFreeSpace() - (FileUtils.ONE_GB * 10)) {
            throw new RuntimeException("目标空间不足");
        }
        Map<String, List<File>> newMap = transferIdFileMapAndSetReadOnly(FileUtils.listFiles(newDir, suffix, true));
        Map<String, List<File>> oldMap = transferIdFileMapAndSetReadOnly(FileUtils.listFiles(oldDir, suffix, true));
        String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        // 更新和新增
        for (Map.Entry<String, List<File>> item : newMap.entrySet()) {
            List<File> old = oldMap.get(item.getKey());
            if (null == old) {
                for (File f : item.getValue()) {
                    FileUtils.moveFileToDirectory(f, finalToPoolPackages ? genMoveToDirById(Integer.valueOf(item.getKey()), oldDir) : oldDir, true);
                    MyLogUtils.stdOut("new新增: " + f.getAbsolutePath());
                }
            } else {
                // 先移除, 后新增, 避免文件名同步无法新增或错误覆盖
                for (File f : old) {
                    MyLogUtils.stdOut("update移除: " + f.getAbsolutePath());
                    FileUtils.moveFileToDirectory(f, new File(oldDir.getParentFile().getCanonicalPath() + File.separator + oldDir.getName() + "_" + time + "_to_delete"), true);
                }
                for (File f : item.getValue()) {
                    MyLogUtils.stdOut("update新增: " + f.getAbsolutePath());
                    FileUtils.moveFileToDirectory(f, finalToPoolPackages ? genMoveToDirById(Integer.valueOf(item.getKey()), oldDir) : oldDir, true);
                }
            }
        }
        // 事后校验
        PoolChecker.check(oldDir.getCanonicalPath(), txtFile);
        // 如果是最终目录，则将父目录下所有文件readOnly
        if (finalToPoolPackages || ArrangeHelper.POOL_PACKAGES_DIR_NAME.equals(oldDir.getName())) {
            FileUtils.listFiles(oldDir.getParentFile(), null, null).stream().forEach(file -> file.setReadOnly());
        }

    }


    /**
     * 将pool按照每600个分割转为按每500个分割
     *
     * @throws Exception
     */
    @Test
    @Deprecated
    public void arrangeDirTest() throws Exception {
        File parentDir = new File("I:\\yande.re\\Pool_Packages");
        for (File item : FileUtils.listFiles(parentDir, null, true)) {
            String id = ArrangeHelper.getPoolId(item.getName());
            File toDir = genMoveToDirById(Integer.valueOf(id), parentDir);
            FileUtils.moveFileToDirectory(item, toDir, true);
        }

    }

    @Test
    public void checkWorkSpace() throws Exception {
        WorkSpaceValidator.checkAndSetReadOnlyWorkSpaceDir("C:\\Users\\yuuki asuna\\Desktop\\workspace");
    }

    private File genMoveToDirById(int id, File toDir) throws Exception {
        if (id < 1) {
            throw new RuntimeException("id非法, " + id);
        }
        int numSliptDir = 500;
        int index = id % numSliptDir == 0 ? id / numSliptDir : id / numSliptDir + 1;
        int a = (index - 1) * numSliptDir + 1;
        int b = index * numSliptDir;
        String name = String.format("Pool_Packages-%s(id=%s-%s)", StringUtils.leftPad(index + "", 3, "0"), a + "", b + "");
        return new File(toDir.getCanonicalPath() + File.separator + name);

    }

    /**
     * 会校验文件名，和每个id对应的文件数量必须为1或者2
     *
     * @param fileCollection
     * @return
     */
    private Map<String, List<File>> transferIdFileMapAndSetReadOnly(Collection<File> fileCollection) {
        Iterator<File> iterator = fileCollection.iterator();
        Map<String, List<File>> result = new HashMap<String, List<File>>();
        while (iterator.hasNext()) {
            File file = iterator.next();
            String name = file.getName();
            // 已经校验文件名
            String id = ArrangeHelper.getPoolId(name);
            if (null == result.get(id)) {
                result.put(id, new ArrayList<File>());
            }
            result.get(id).add(file);
            if (result.get(id).size() != 1 && result.get(id).size() != 2) {
                throw new RuntimeException("每个pool id对应的文件数量必须为1或者2");
            }
        }
        return result;
    }
}
