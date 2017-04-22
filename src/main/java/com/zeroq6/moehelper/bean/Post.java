package com.zeroq6.moehelper.bean;

import com.alibaba.fastjson.JSON;

import java.util.List;

/**
 * Post
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 */
public class Post {

    private int id;
    private String tags;
    private long created_at;
    private int creator_id;
    private String author;
    private long change;
    private String source;
    private int score;
    private String md5;
    private long file_size;
    private String file_url;
    private boolean is_shown_in_index;
    private String preview_url;
    private int preview_width;
    private int preview_height;
    private int actual_preview_width;
    private int actual_preview_height;
    private String sample_url;
    private long sample_width;
    private long sample_height;
    private long sample_file_size;
    private String jpeg_url;
    private long jpeg_width;
    private long jpeg_height;
    private long jpeg_file_size;
    private String rating;
    private boolean has_children;
    private int parent_id;
    private String status;
    private long width;
    private long height;
    private boolean is_held;
    private String frames_pending_string;
    private List<Object> frames_pending;
    private String frames_string;
    private List<Object> frames;

    public int getId() {
        return id;
    }

    public Post setId(int id) {
        this.id = id;
        return this;
    }

    public String getTags() {
        return tags;
    }

    public Post setTags(String tags) {
        this.tags = tags;
        return this;
    }

    public long getCreated_at() {
        return created_at;
    }

    public Post setCreated_at(long created_at) {
        this.created_at = created_at;
        return this;
    }

    public int getCreator_id() {
        return creator_id;
    }

    public Post setCreator_id(int creator_id) {
        this.creator_id = creator_id;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public Post setAuthor(String author) {
        this.author = author;
        return this;
    }

    public long getChange() {
        return change;
    }

    public Post setChange(long change) {
        this.change = change;
        return this;
    }

    public String getSource() {
        return source;
    }

    public Post setSource(String source) {
        this.source = source;
        return this;
    }

    public int getScore() {
        return score;
    }

    public Post setScore(int score) {
        this.score = score;
        return this;
    }

    public String getMd5() {
        return md5;
    }

    public Post setMd5(String md5) {
        this.md5 = md5;
        return this;
    }

    public long getFile_size() {
        return file_size;
    }

    public Post setFile_size(long file_size) {
        this.file_size = file_size;
        return this;
    }

    public String getFile_url() {
        return file_url;
    }

    public Post setFile_url(String file_url) {
        this.file_url = file_url;
        return this;
    }

    public boolean isIs_shown_in_index() {
        return is_shown_in_index;
    }

    public Post setIs_shown_in_index(boolean is_shown_in_index) {
        this.is_shown_in_index = is_shown_in_index;
        return this;
    }

    public String getPreview_url() {
        return preview_url;
    }

    public Post setPreview_url(String preview_url) {
        this.preview_url = preview_url;
        return this;
    }

    public int getPreview_width() {
        return preview_width;
    }

    public Post setPreview_width(int preview_width) {
        this.preview_width = preview_width;
        return this;
    }

    public int getPreview_height() {
        return preview_height;
    }

    public Post setPreview_height(int preview_height) {
        this.preview_height = preview_height;
        return this;
    }

    public int getActual_preview_width() {
        return actual_preview_width;
    }

    public Post setActual_preview_width(int actual_preview_width) {
        this.actual_preview_width = actual_preview_width;
        return this;
    }

    public int getActual_preview_height() {
        return actual_preview_height;
    }

    public Post setActual_preview_height(int actual_preview_height) {
        this.actual_preview_height = actual_preview_height;
        return this;
    }

    public String getSample_url() {
        return sample_url;
    }

    public Post setSample_url(String sample_url) {
        this.sample_url = sample_url;
        return this;
    }

    public long getSample_width() {
        return sample_width;
    }

    public Post setSample_width(long sample_width) {
        this.sample_width = sample_width;
        return this;
    }

    public long getSample_height() {
        return sample_height;
    }

    public Post setSample_height(long sample_height) {
        this.sample_height = sample_height;
        return this;
    }

    public long getSample_file_size() {
        return sample_file_size;
    }

    public Post setSample_file_size(long sample_file_size) {
        this.sample_file_size = sample_file_size;
        return this;
    }

    public String getJpeg_url() {
        return jpeg_url;
    }

    public Post setJpeg_url(String jpeg_url) {
        this.jpeg_url = jpeg_url;
        return this;
    }

    public long getJpeg_width() {
        return jpeg_width;
    }

    public Post setJpeg_width(long jpeg_width) {
        this.jpeg_width = jpeg_width;
        return this;
    }

    public long getJpeg_height() {
        return jpeg_height;
    }

    public Post setJpeg_height(long jpeg_height) {
        this.jpeg_height = jpeg_height;
        return this;
    }

    public long getJpeg_file_size() {
        return jpeg_file_size;
    }

    public Post setJpeg_file_size(long jpeg_file_size) {
        this.jpeg_file_size = jpeg_file_size;
        return this;
    }

    public String getRating() {
        return rating;
    }

    public Post setRating(String rating) {
        this.rating = rating;
        return this;
    }

    public boolean isHas_children() {
        return has_children;
    }

    public Post setHas_children(boolean has_children) {
        this.has_children = has_children;
        return this;
    }

    public int getParent_id() {
        return parent_id;
    }

    public Post setParent_id(int parent_id) {
        this.parent_id = parent_id;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public Post setStatus(String status) {
        this.status = status;
        return this;
    }

    public long getWidth() {
        return width;
    }

    public Post setWidth(long width) {
        this.width = width;
        return this;
    }

    public long getHeight() {
        return height;
    }

    public Post setHeight(long height) {
        this.height = height;
        return this;
    }

    public boolean isIs_held() {
        return is_held;
    }

    public Post setIs_held(boolean is_held) {
        this.is_held = is_held;
        return this;
    }

    public String getFrames_pending_string() {
        return frames_pending_string;
    }

    public Post setFrames_pending_string(String frames_pending_string) {
        this.frames_pending_string = frames_pending_string;
        return this;
    }

    public List<Object> getFrames_pending() {
        return frames_pending;
    }

    public Post setFrames_pending(List<Object> frames_pending) {
        this.frames_pending = frames_pending;
        return this;
    }

    public String getFrames_string() {
        return frames_string;
    }

    public Post setFrames_string(String frames_string) {
        this.frames_string = frames_string;
        return this;
    }

    public List<Object> getFrames() {
        return frames;
    }

    public Post setFrames(List<Object> frames) {
        this.frames = frames;
        return this;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
