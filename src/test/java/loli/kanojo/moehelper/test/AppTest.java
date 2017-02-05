package loli.kanojo.moehelper.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import loli.kanojo.moehelper.config.Constants;
import loli.kanojo.moehelper.utils.Kit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("unused")
public class AppTest {

    @Test
    public void testApp() {
        String[] args = new String[] { "1", "10" };
        //App.main(args);
    }
    
    @Test
    public void test(){
        Properties props = System.getProperties();
        props.list(System.out);
    }

    @Test
    @Ignore
    public void countPostNumInfoAll() throws IOException {
        int to = 20000;
        String logFilesDir = "D:\\moe\\Konachan.com\\Post";
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
        liResult.add(Kit.getFormatedCurrentTime());
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
            throw new RuntimeException("不支持的文件格式");
        }
        String desPath = srcPath.replace(".lst", "." + System.currentTimeMillis() + ".lst");
        List<String> li = FileUtils.readLines(new File(srcPath), "utf-8");
        List<String> result = new ArrayList<String>();
        for(int i =0; i<li.size() ; i++){
            String url = li.get(i);
            String[] splitStrArr = url.split("/", 6);
            if(null == splitStrArr || splitStrArr.length != 6){
                throw new RuntimeException("Error Pool url format: " + url);
            }
            String pageIdString = splitStrArr[splitStrArr.length - 1];
            pageIdString = pageIdString.substring(0, pageIdString.indexOf("?") == -1 ? pageIdString.length() : pageIdString.indexOf("?"));
            pageIdString = StringUtils.leftPad(pageIdString, 4, "0");
            if(!StringUtils.isNumeric(pageIdString)){
                throw new RuntimeException("pageId获取失败, " + pageIdString);
            }
            if(url.contains(Constants.LINK_POOL_ZIP_SUFFIX_JPG)){
                url = url + "&myPoolId=" +  pageIdString;
            }else{
                url = url + "?myPoolId=" + pageIdString;
            }
            result.add(url);
        }
        FileUtils.writeLines(new File(desPath), "utf-8", result, false);
    }

}
