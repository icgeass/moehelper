package com.zeroq6.moehelper.bean;

import com.alibaba.fastjson.JSON;

/**
 * Pool
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public class Pool {

    private int id;
    private String name;
    private String created_at;
    private String updated_at;
    private int user_id;
    private boolean is_public;
    private int post_count;
    private String description;

    public int getId() {
        return id;
    }

    public Pool setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Pool setName(String name) {
        this.name = name;
        return this;
    }

    public String getCreated_at() {
        return created_at;
    }

    public Pool setCreated_at(String created_at) {
        this.created_at = created_at;
        return this;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public Pool setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
        return this;
    }

    public int getUser_id() {
        return user_id;
    }

    public Pool setUser_id(int user_id) {
        this.user_id = user_id;
        return this;
    }

    public boolean isIs_public() {
        return is_public;
    }

    public Pool setIs_public(boolean is_public) {
        this.is_public = is_public;
        return this;
    }

    public int getPost_count() {
        return post_count;
    }

    public Pool setPost_count(int post_count) {
        this.post_count = post_count;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Pool setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
