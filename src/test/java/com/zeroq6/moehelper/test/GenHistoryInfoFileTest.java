package com.zeroq6.moehelper.test;

import com.alibaba.fastjson.JSON;
import com.zeroq6.moehelper.bean.Page;
import com.zeroq6.moehelper.bean.Post;
import com.zeroq6.moehelper.utils.MyLogUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by yuuki asuna on 2017/6/22.
 */
public class GenHistoryInfoFileTest {


    /**
     * step 4
     *
     *  写被标记为删除的post的json文件，文件名保持一致
     * @throws Exception
     */
    @Test
    public void genDeletedPostJson() throws Exception {
        String basePath = "E:\\sh\\图\\WORK!!!\\!work\\yande.re\\Post";
        File dir = new File(basePath);
        Map<String, Page> postId2Page = new HashMap<String, Page>();
        Map<String, String> postId2JsonOk = new HashMap<String, String>();
        Pattern pattern = Pattern.compile("^[0-9a-f]{32}$");

        for (File file : dir.listFiles()) {
            if (file.getName().endsWith("post_all.lst")) {
                List<String> list = FileUtils.readLines(file, "utf-8");
                for (String link : list) {
                    String decode = URLDecoder.decode(link, "utf-8");
                    Integer postId = Integer.valueOf(decode.split(" ", 3)[1]);
                    Page page = new Page();
                    page.setPosts(new ArrayList<Post>());
                    Post post = new Post();
                    page.getPosts().add(post);
                    postId2Page.put(postId + "", page);
                    //
                    post.setCreated_at(-10086);
                    post.setId(postId);
                    post.setFile_url(link);
                    post.setMd5(link.substring(link.indexOf("/image/") + "/image/".length(), link.lastIndexOf("/yande.re")));
                    if (!pattern.matcher(post.getMd5()).matches()) {
                        MyLogUtils.fatal("非法md5格式");
                    }
                    post.setTags(URLDecoder.decode(link.substring(link.indexOf(post.getMd5()) + post.getMd5().length() + 1), "utf-8").split(" ", 3)[2]);
                    post.setTags(post.getTags().substring(0, post.getTags().lastIndexOf(".")));
                }
            }else if(file.getName().replace(".deleted_post.json", "").endsWith(".json")){ //
                List<String> list = FileUtils.readLines(file, "utf-8");
                for(String string : list){
                    Page page = JSON.parseObject(string, Page.class);
                    postId2JsonOk.put(page.getPosts().get(0).getId()+ "", true+"");
                }
            }
        }

        Map<String, String> list = genHashMap(dir, "_post.log");
        int all =  0;
        for (String key : list.keySet()) {
            int index = Integer.valueOf(key);
            int from = (index - 1) * 10000 + 1;
            int to = index * 10000;
            List<String> stringList = new ArrayList<String>();
            for (int i = from; i <= to; i++) {
                Page page = postId2Page.get(i + "");
                if (null == page) {
                    continue;
                }
                if(null == postId2JsonOk.get(i + "")){
                    stringList.add(JSON.toJSONString(page));
                }
            }
            if (!stringList.isEmpty()) {
                String newName = list.get(key).replace("_post.log", "") + ".deleted_post.json";
                FileUtils.writeLines(new File("E:\\new111" + File.separator + newName), "utf-8", stringList, false);
                MyLogUtils.stdOut(key + "--->" + stringList.size());
                all += stringList.size();
            }
        }

        MyLogUtils.stdOut(all);

    }


    /**
     *
     * step 3
     * 将step 2 中的文件合并成_post_all（lst或md5）
     * @throws Exception
     */
    @Test
    public void testMerge() throws Exception {
        String type = "lst";

        // -------------------
        String basePath = "E:\\sh\\图\\WORK!!!\\!work\\yande.re\\Post";
        String baseSuffix = "post_no_pool." + type;
        // base
        Map<String, String> baseId2FileName = genHashMap(new File(basePath), baseSuffix);


        String targetPath = "E:";
        String targetSuffix = "post_in_pool." + type;
        // to change
        Map<String, String> targetId2FileName = genHashMap(new File(targetPath), targetSuffix);


        for (String id : targetId2FileName.keySet()) {
            File baseFile = new File(basePath + File.separator + baseId2FileName.get(id));
            File targetFile = new File(targetPath + File.separator + targetId2FileName.get(id));
            List<String> list = FileUtils.readLines(baseFile, "utf-8");
            list.addAll(FileUtils.readLines(targetFile, "utf-8"));
            Map<String, String> postId2String = new HashMap<String, String>();
            for (String string : list) {
                String decode = URLDecoder.decode(string, "utf-8");
                Integer productId = null;
                if (string.toLowerCase().startsWith("http")) { // https://yande.re 1234 xxx.jpg
                    productId = Integer.valueOf(decode.split(" ", 3)[1]);
                } else if (string.contains(" *yande.re")) { // 0000000 *yande.re 1111 xxx.jpg
                    productId = Integer.valueOf(decode.split(" ", 4)[2]);
                }
                postId2String.put(productId + "", string);
                // MyLogUtils.stdOut(productId + "------------" + string);
            }
            int index = Integer.valueOf(id);
            int from = (index - 1) * 10000 + 1;
            int to = index * 10000;
            List<String> stringList = new ArrayList<String>();
            for (int i = from; i <= to; i++) {
                String value = postId2String.get(i + "");
                if (StringUtils.isBlank(value)) {
                    continue;
                }
                stringList.add(value);
            }
            if (!stringList.isEmpty()) {
                String newName = baseId2FileName.get(id).replace("no_pool", "all");
                FileUtils.writeLines(new File("E:\\new" + File.separator + newName), "utf-8", stringList, false);
                MyLogUtils.stdOut(id + "--->" + stringList.size());
            }

        }
    }


    /**
     *
     * step 2
     *
     * 最开始post_in_pool.lst（修改suffix  post_in_pool.md5适用于md5文件）是用zip打包（从总json文件中读取），
     * 且文件名不对应，这里将文件名对应上
     * @throws Exception
     */
    @Test
    public void testRename() throws Exception {
        // yande.re_-_post_130908133938_1_10000_post_no_pool
        // yande.re_-_post_140528210534_1_10000_post_in_pool
        // yande.re_-_post_170126111234_370001_380000_post_all

        String basePath = "E:\\sh\\图\\WORK!!!\\!work\\yande.re\\Post";
        String baseSuffix = "post_no_pool.lst";
        // base
        Map<String, String> baseId2FileName = genHashMap(new File(basePath), baseSuffix);


        String targetPath = "E:\\sh\\图\\WORK!!!\\!work\\yande.re\\tmp";
        String targetSuffix = "post_in_pool.lst";
        // to change
        Map<String, String> targetId2FileName = genHashMap(new File(targetPath), targetSuffix);


        for (String id : targetId2FileName.keySet()) {
            String newName = baseId2FileName.get(id).replace(baseSuffix, targetSuffix);
            File f = new File(targetPath + File.separator + targetId2FileName.get(id));
            f.renameTo(new File("E://" + newName));
        }


    }

    /**
     *
     * step 1
     *
     * 将yande.re.json从一个总文件按每次更新id拆分成单独文件，并且文件前缀名保存和对应id文件名一致
     * @throws Exception
     */
    @Test
    public void testGenJsonFile() throws Exception {
        String path = "E:\\sh\\图\\WORK!!!\\!work\\yande.re\\Post";
        String jsonFileName = "yande.re.json";
        File dir = new File(path);
        Map<String, String> id2Path = genHashMap(dir, ".log");
        Map<Integer, String> id2String = new HashMap<Integer, String>();
        List<String> jsonStringList = FileUtils.readLines(new File(path + File.separator + jsonFileName), "utf-8");
        for (String s : jsonStringList) {
            Page page = JSON.parseObject(s, Page.class);
            Integer postId = page.getPosts().get(0).getId();
            id2String.put(postId, s);
        }

        int size = 0;
        for (String string : id2Path.keySet()) {
            String logName = id2Path.get(string).replace("_post.log", "");
            String newName = logName + "." + jsonFileName.replace("konachan.com.", "").replace("yande.re.", "");
            List<String> stringList = new ArrayList<String>();
            int index = Integer.valueOf(string);
            int from = (index - 1) * 10000 + 1;
            int to = index * 10000;
            for (int i = from; i <= to; i++) {
                String value = id2String.get(i);
                if (StringUtils.isBlank(value)) {
                    continue;
                }
                stringList.add(value);
            }
            if (!stringList.isEmpty()) {
                FileUtils.writeLines(new File("E:\\" + newName), "utf-8", stringList, false);
                size += stringList.size();
                MyLogUtils.stdOut(newName);
            }
        }
        MyLogUtils.stdOut(size);
        MyLogUtils.stdOut();
    }



    public static Map<String, String> genHashMap(File dir, String suffix) throws Exception {
        Map<String, String> re = new HashMap<String, String>();
        for (File f : dir.listFiles()) {
            if (f.getName().endsWith(suffix)) {
                // konachan.com_-_post_140510201904_1_10000_post
                Integer n = Integer.valueOf(f.getName().split("_", 7)[5]) / 10000;
                re.put("" + n, f.getName());
            }
        }
        return re;
    }


}
