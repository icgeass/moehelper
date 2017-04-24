> 本文件描述抓取数据的相关特征，和异常情况等
---
- pool对应的json数据中post数组数量小于等于pool_posts数量
    -  一个post如果包含在多个pool中，则pool_posts会有多个，如pool #6中的post #48373，由于同时也存在于pool #1201中，所以pool_posts数量比posts多1
    - pools中存放pool_posts中对应pool信息，两者数量相同，pools会冗余存大量相同的pool信息
    
    
    
    
    
    
