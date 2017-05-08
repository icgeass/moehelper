# moehelper

一个维护k站、萌站图片画册更新的辅助程序

### 用法

```java -jar <jarfile> <fromindex> <toindex> [<--Post|--Pool> [--moe|--kona]]```

### 注意

0. 版本要求，Java 7及以上
1. 图片下载使用火狐DownThemAll插件，对于链接中包含```...```无法导入情况，使用命令```find "..." <filePath>```（windows）找出，然后手动添加到下载列表
2. 校验图片MD5使用RapidCRC Unicode并设置UTF-8编码
3. jar文件与生成的文件（夹）的相对位置不能改变，抓取pool链接时会读取上一次抓取的pool信息，判断之前的pool中是否有图片更新
4. 由于更新pool需要判断前面的pool是否有更新，所以pool id一律从1开始
5. 为pool添加id前缀用于区分。使用DownThemAll的文件名掩码```[*qstring*]*name*.*ext*```，下载到本地后用ReNamer替换下载好后的文件名的```jpeg=1&```和```pid=```为空字符串即可得到带id文件名
    1. ```[jpeg=1&pid=4474]電撃 おとなの萌王 Vol.06 (JPG).zip => [4474]電撃 おとなの萌王 Vol.06 (JPG).zip```
    2. ```[pid=4474]電撃 おとなの萌王 Vol.06.zip => [4474]電撃 おとなの萌王 Vol.06.zip ```

### 常见问题
 
- 如何判断指定post是否在pool中
    - 判断页面json数据中pools数组长度是否大于0
    - 正则匹配HTML页面内容```This post is #x in the xxx pool.```
    - 如果以上两者结果不一致则会提示并退出
    - post是否在pool中以获取post信息的时间为准
- 怎样确定pool被更新过
    - 每次更新pool保存每个pool对应的post信息，再次更新时如果当前pool的post均在上次更新时该pool的post中（使用MD5判断），则认为该pool没有更新，否则认为更新过
- post.log日志中status属性的10串含义
    - 从左至右依次表示：是否含有原图链接，是否为png格式，是否含有jpeg图链接，是否为jpg格式，是否含有sample图链接，是否为jpg格式
- post.log日志中 写入URL条数 和 写入MD5条数 为什么与 读取成功 数量不一致
    - 程序设计初期post获取方式只是为了抓取不在pool中post链接（post被标记为删除，通过解析HTML文档获得的链接不管是否在pool中均包含在内），后面为了兼容处理，这两者只记录不在pool中数量
    - 可以查看 Pool信息 行，包含了所有链接抓取成功post数量详细，从左至右数字依次表示：
        1. 没有被标记为删除的不在Pool中的post数量
        2. 没有被标记为删除的在Pool中的post数量
        3. 被标记为删除的通过解析HTML获得的不在Pool中的post数量
        4. 被标记为删除的通过解析HTML获得的在Pool中的post数量
- 使用代理
    - 添加命令行参数，如```java -DsocksProxyHost=127.0.0.1 -DsocksProxyPort=1080 -jar ...```
    - [查看详细](http://docs.oracle.com/javase/7/docs/technotes/guides/net/proxies.html)
- 错误日志文件乱码
    - 添加命令行参数，```-Dfile.encoding=UTF-8```


