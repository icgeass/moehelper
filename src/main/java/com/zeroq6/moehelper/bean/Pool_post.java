package com.zeroq6.moehelper.bean;

import com.alibaba.fastjson.JSON;

/**
 * pool和post的id对应
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public class Pool_post {

    private int id;
    private int pool_id;
    private int post_id;
    private boolean active;
    private String sequence;
    private int next_post_id;
    private int prev_post_id;

    public int getId() {
        return id;
    }

    public Pool_post setId(int id) {
        this.id = id;
        return this;
    }

    public int getPool_id() {
        return pool_id;
    }

    public Pool_post setPool_id(int pool_id) {
        this.pool_id = pool_id;
        return this;
    }

    public int getPost_id() {
        return post_id;
    }

    public Pool_post setPost_id(int post_id) {
        this.post_id = post_id;
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public Pool_post setActive(boolean active) {
        this.active = active;
        return this;
    }

    public String getSequence() {
        return sequence;
    }

    public Pool_post setSequence(String sequence) {
        this.sequence = sequence;
        return this;
    }

    public int getNext_post_id() {
        return next_post_id;
    }

    public Pool_post setNext_post_id(int next_post_id) {
        this.next_post_id = next_post_id;
        return this;
    }

    public int getPrev_post_id() {
        return prev_post_id;
    }

    public Pool_post setPrev_post_id(int prev_post_id) {
        this.prev_post_id = prev_post_id;
        return this;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
