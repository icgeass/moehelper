package com.zeroq6.moehelper.bean;

import com.alibaba.fastjson.JSON;

import java.util.List;
import java.util.Map;

/**
 * 对应Post和Pool页面的数据对象
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public class Page {

    private List<Post> posts;
    private List<Pool> pools;
    private List<Pool_post> pool_posts;
    private Map<String, Object> tags;
    private Map<String, Object> votes;

    public List<Post> getPosts() {
        return posts;
    }

    public Page setPosts(List<Post> posts) {
        this.posts = posts;
        return this;
    }

    public List<Pool> getPools() {
        return pools;
    }

    public Page setPools(List<Pool> pools) {
        this.pools = pools;
        return this;
    }

    public List<Pool_post> getPool_posts() {
        return pool_posts;
    }

    public Page setPool_posts(List<Pool_post> pool_posts) {
        this.pool_posts = pool_posts;
        return this;
    }

    public Map<String, Object> getTags() {
        return tags;
    }

    public Page setTags(Map<String, Object> tags) {
        this.tags = tags;
        return this;
    }

    public Map<String, Object> getVotes() {
        return votes;
    }

    public Page setVotes(Map<String, Object> votes) {
        this.votes = votes;
        return this;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }


}
