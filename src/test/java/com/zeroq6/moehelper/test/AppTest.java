package com.zeroq6.moehelper.test;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import com.zeroq6.moehelper.app.App;
import com.zeroq6.moehelper.bean.Page;
import com.zeroq6.moehelper.bean.Post;
import com.zeroq6.moehelper.fetcher.impl.PoolFetcher;
import com.zeroq6.moehelper.utils.MyDateUtils;

import com.zeroq6.moehelper.utils.MyLogUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

@SuppressWarnings("unused")
public class AppTest {

    @Test
    public void testApp() {
        String[] args = new String[]{"100664", "100664", "--Post", "--kona"};
        App.main(args);
    }


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
                    Integer productId = Integer.valueOf(decode.split(" ", 3)[1]);
                    Page page = new Page();
                    page.setPosts(new ArrayList<Post>());
                    Post post = new Post();
                    page.getPosts().add(post);
                    postId2Page.put(productId + "", page);
                    //
                    post.setCreated_at(-10086);
                    post.setId(productId);
                    post.setFile_url(link);
                    post.setMd5(link.substring(link.indexOf("/image/") + "/image/".length(), link.lastIndexOf("/yande.re")));
                    if (!pattern.matcher(post.getMd5()).matches()) {
                        MyLogUtils.fatal("非法md5格式");
                    }
                    post.setTags(URLDecoder.decode(link.substring(link.indexOf(post.getMd5()) + post.getMd5().length() + 1), "utf-8").split(" ", 3)[2]);
                    post.setTags(post.getTags().substring(0, post.getTags().lastIndexOf(".")));
                }
            }else if(file.getName().replace(".deleted_post.json", "").endsWith(".json")){
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
                System.out.println(key + "--->" + stringList.size());
                all += stringList.size();
            }
        }

        System.out.println(all);

    }


    @Test
    public void testMerge() throws Exception {
        String basePath = "E:\\sh\\图\\WORK!!!\\!work\\yande.re\\Post";
        String type = "lst";
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
                if (string.toLowerCase().startsWith("http")) {
                    productId = Integer.valueOf(decode.split(" ", 3)[1]);
                } else if (string.contains(" *yande.re")) {
                    productId = Integer.valueOf(decode.split(" ", 4)[2]);
                }
                postId2String.put(productId + "", string);
                // System.out.println(productId + "------------" + string);
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
                System.out.println(id + "--->" + stringList.size());
            }

        }
    }


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
                System.out.println(newName);
            }
        }
        System.out.println(size);
        System.out.println();
    }

    @Test
    public void test() {
        Properties props = System.getProperties();
        props.list(System.out);
    }

    @Test
    public void countPostNumInfoAll() throws IOException {
        int to = 380000;
        String logFilesDir = "E:\\sh\\图\\WORK!!!\\!work\\yande.re\\Post";
        String pathToWrite = "./tmp/kona_all_post_info.txt";
        File dir = new File(logFilesDir);
        List<String> liLines = new ArrayList<String>();
        List<String> liFileNameReadFrom = new ArrayList<String>();
        List<String> liResult = new ArrayList<String>();
        int allPageNum = 0;
        int readOkPageNum = 0;
        int readFailedPageNum = 0;
        int josnItemNum = 0;
        int[] countDetailsInfo = new int[5];
        int[] poolInfo = new int[4];
        int[] writeInfo = new int[4];
        for (File file : dir.listFiles()) {
            if (file.getName().endsWith("_post.log") && Integer.valueOf(file.getName().split("_")[5]) <= to) {
                liFileNameReadFrom.add(file.getName());
                List<String> liTmpLines = FileUtils.readLines(file, "utf-8");
                for (int i = 0; i < liTmpLines.size(); i++) {
                    if (i >= 4 && i <= 10) {
                        liLines.add(liTmpLines.get(i));
                    }
                    if (i > 10) {
                        break;
                    }
                }
            }
        }
        for (int i = 0; i < liLines.size(); i++) {
            switch ((i + 1) % 7) {
                case 1:
                    allPageNum += Integer.valueOf(liLines.get(i).split(":")[1].trim());
                    break;
                case 2:
                    readOkPageNum += Integer.valueOf(liLines.get(i).split(":")[1].trim());
                    break;
                case 3:
                    readFailedPageNum += Integer.valueOf(liLines.get(i).split(":")[1].trim());
                    break;
                case 4:
                    josnItemNum += Integer.valueOf(liLines.get(i).split(":")[1].trim());
                    break;
                case 5:
                    String itemDetails = liLines.get(i);
                    itemDetails += "=";
                    itemDetails = itemDetails.replace(",", "=");
                    String[] itemDetailsSplitedArray = itemDetails.split("=");
                    for (int j = 0; j < countDetailsInfo.length; j++) {
                        countDetailsInfo[j] += Integer.valueOf(itemDetailsSplitedArray[(j + 1) * 2 - 1].trim());
                    }
                    break;
                case 6:
                    String itemPoolInfo = liLines.get(i);
                    itemPoolInfo += "=";
                    itemPoolInfo = itemPoolInfo.replace(",", "=").replace("+", "=");
                    String[] itemPoolInfoSplitedArray = itemPoolInfo.split("=");
                    for (int j = 0; j < poolInfo.length; j++) {
                        poolInfo[j] += Integer.valueOf(itemPoolInfoSplitedArray[j <= 1 ? j + 1 : j + 2].trim());
                    }
                    break;
                case 0:
                    String itemWriteInfo = liLines.get(i);
                    itemWriteInfo += "=";
                    itemWriteInfo = itemWriteInfo.replace(",", "=");
                    String[] itemWriteInfoSplitedArray = itemWriteInfo.split("=");
                    for (int j = 0; j < writeInfo.length; j++) {
                        writeInfo[j] += Integer.valueOf(itemWriteInfoSplitedArray[(j + 1) * 2 - 1].trim());
                    }
                    break;
                default:
            }
        }
        liResult.add(MyDateUtils.formatCurrentTime());
        liResult.add("统计: ");
        liResult.add("");
        liResult.add("Id 范围: 1 - " + to);
        liResult.add("页面总数: " + allPageNum);
        liResult.add("读取成功: " + readOkPageNum);
        liResult.add("读取失败: " + readFailedPageNum);
        liResult.add("JSON数据条数: " + josnItemNum);
        liResult.add("详细计数: ok-json=" + countDetailsInfo[0] + ", ok-doc-post-deleted=" + countDetailsInfo[1] + ", 404=" + countDetailsInfo[2] + ", exception="
                + countDetailsInfo[3] + ", no url=" + countDetailsInfo[4]);
        liResult.add("Pool信息: " + "ok-json=" + poolInfo[0] + "+" + poolInfo[1] + ", ok-doc-post-deleted=" + poolInfo[2] + "+" + poolInfo[3]);
        liResult.add("文件写入情况: JSON条数=" + writeInfo[0] + ", 写入URL条数=" + writeInfo[1] + ", 写入MD5条数=" + writeInfo[2] + ", 记录Log条数=" + writeInfo[3]);
        liResult.add("");
        liResult.add("Read from: ");
        Collections.sort(liFileNameReadFrom);
        liResult.addAll(liFileNameReadFrom);
        for (String line : liResult) {
            System.out.println(line);
        }
        FileUtils.writeLines(new File(pathToWrite), "utf-8", liResult, false);

    }

    @Test
    public void processZipUrl() throws Exception{
        String srcPath = "D://yande.re_-_pool_170204103710_1_4431_packages_updated_url.lst";
        if(!srcPath.endsWith(".lst")){
            MyLogUtils.fatal("不支持的文件格式");
        }
        String desPath = srcPath.replace(".lst", "." + System.currentTimeMillis() + ".lst");
        List<String> li = FileUtils.readLines(new File(srcPath), "utf-8");
        List<String> result = new ArrayList<String>();
        for(int i =0; i<li.size() ; i++){
            String url = li.get(i);
            String[] splitStrArr = url.split("/", 6);
            if(null == splitStrArr || splitStrArr.length != 6){
                MyLogUtils.fatal("Error Pool url format: " + url);
            }
            String pageIdString = splitStrArr[splitStrArr.length - 1];
            pageIdString = pageIdString.substring(0, pageIdString.indexOf("?") == -1 ? pageIdString.length() : pageIdString.indexOf("?"));
            pageIdString = StringUtils.leftPad(pageIdString, 4, "0");
            if(!StringUtils.isNumeric(pageIdString)){
                MyLogUtils.fatal("pageId获取失败, " + pageIdString);
            }
            if(url.contains(PoolFetcher.LINK_POOL_ZIP_SUFFIX_JPG)){
                url = url + "&myPoolId=" +  pageIdString;
            }else{
                url = url + "?myPoolId=" + pageIdString;
            }
            result.add(url);
        }
        FileUtils.writeLines(new File(desPath), "utf-8", result, false);
    }

}
