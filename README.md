# moehelper
![moehelper logo](https://github.com/icgeass/moehelper/raw/master/etc/logo.png)

### 介绍

一个更新图片的辅助程序

 *`私用, 勿公开传播`* <br />
 *`Private Use, No Public Distribution`*

### 用法

	java -jar <jarfile> <fromindex> <toindex> [<--Post|--Pool> [--moe|--kona]]

### 常见问题
 
1. **如何判断指定post是否在pool中**<br/>
正则和pools.size
2. **怎样确定pool被更新过**<br />
如果该pool上一次更新时含有本次更新所有post的MD5，则未更新，否则认为更新过。注意pool中只出现post被移除将不会被认为更新
3. **post日志中status属性的10串含义**<br />
从左至右依次表示: 是否含有原图链接, 是否为png格式, 是否含有jpeg图链接, 是否为jpg格式, 是否含有sample图链接, 是否为jpg格式

### 注意

* 推荐校验MD5使用RapidCRC Unicode，并选择utf-8编码；导入链接使用DownThemAll!(文件名中含`...`可能无法导入，可用find命令找出)。程序已处理链接中非法字符
* 对于post，更新时id范围按照1..n，n+1..2n，2n+1..3n依次类推；对于pool，每次更新均使用1..MaxPoolId，以方便检查更新。限定id范围1w以内，一般不区分pool，则使用all.lst（no_pool.lst和in_pool）；区分pool则使用no_pool.lst并且定时更新pool
* post日志中文件写入情况行，`JSON条数`记录所有未被标记删除的post的JSON条数，`写入URL条数`和`写入MD5条数`仅统计不在pool中的post（但是含所有被标记为删除但找到链接的post，不区分是否在pool中）
* `pool信息行`加号左边表示不在pool中的图片数量，右边表示在pool中

### 更新历史

* v1.0.7

### 引用

* [fastjson](https://github.com/alibaba/fastjson "fastjson")
* [httpclient](http://hc.apache.org/httpcomponents-client-4.4.x/index.html "httpclient")
* [jsoup](http://jsoup.org/ "jsoup")
* [apache io commons](http://commons.apache.org/proper/commons-io/ "apache io commons")


