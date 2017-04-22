package com.zeroq6.moehelper.bean;

import java.util.List;

/**
 * Post
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 * @version moehelper - v1.0.7
 * @url https://github.com/icgeass/moehelper
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

    public Post() {
    }

    public Post(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public int getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(int creator_id) {
        this.creator_id = creator_id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getChange() {
        return change;
    }

    public void setChange(long change) {
        this.change = change;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getFile_size() {
        return file_size;
    }

    public void setFile_size(long file_size) {
        this.file_size = file_size;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    public boolean isIs_shown_in_index() {
        return is_shown_in_index;
    }

    public void setIs_shown_in_index(boolean is_shown_in_index) {
        this.is_shown_in_index = is_shown_in_index;
    }

    public String getPreview_url() {
        return preview_url;
    }

    public void setPreview_url(String preview_url) {
        this.preview_url = preview_url;
    }

    public int getPreview_width() {
        return preview_width;
    }

    public void setPreview_width(int preview_width) {
        this.preview_width = preview_width;
    }

    public int getPreview_height() {
        return preview_height;
    }

    public void setPreview_height(int preview_height) {
        this.preview_height = preview_height;
    }

    public int getActual_preview_width() {
        return actual_preview_width;
    }

    public void setActual_preview_width(int actual_preview_width) {
        this.actual_preview_width = actual_preview_width;
    }

    public int getActual_preview_height() {
        return actual_preview_height;
    }

    public void setActual_preview_height(int actual_preview_height) {
        this.actual_preview_height = actual_preview_height;
    }

    public String getSample_url() {
        return sample_url;
    }

    public void setSample_url(String sample_url) {
        this.sample_url = sample_url;
    }

    public long getSample_width() {
        return sample_width;
    }

    public void setSample_width(long sample_width) {
        this.sample_width = sample_width;
    }

    public long getSample_height() {
        return sample_height;
    }

    public void setSample_height(long sample_height) {
        this.sample_height = sample_height;
    }

    public long getSample_file_size() {
        return sample_file_size;
    }

    public void setSample_file_size(long sample_file_size) {
        this.sample_file_size = sample_file_size;
    }

    public String getJpeg_url() {
        return jpeg_url;
    }

    public void setJpeg_url(String jpeg_url) {
        this.jpeg_url = jpeg_url;
    }

    public long getJpeg_width() {
        return jpeg_width;
    }

    public void setJpeg_width(long jpeg_width) {
        this.jpeg_width = jpeg_width;
    }

    public long getJpeg_height() {
        return jpeg_height;
    }

    public void setJpeg_height(long jpeg_height) {
        this.jpeg_height = jpeg_height;
    }

    public long getJpeg_file_size() {
        return jpeg_file_size;
    }

    public void setJpeg_file_size(long jpeg_file_size) {
        this.jpeg_file_size = jpeg_file_size;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public boolean isHas_children() {
        return has_children;
    }

    public void setHas_children(boolean has_children) {
        this.has_children = has_children;
    }

    public int getParent_id() {
        return parent_id;
    }

    public void setParent_id(int parent_id) {
        this.parent_id = parent_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getWidth() {
        return width;
    }

    public void setWidth(long width) {
        this.width = width;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public boolean isIs_held() {
        return is_held;
    }

    public void setIs_held(boolean is_held) {
        this.is_held = is_held;
    }

    public String getFrames_pending_string() {
        return frames_pending_string;
    }

    public void setFrames_pending_string(String frames_pending_string) {
        this.frames_pending_string = frames_pending_string;
    }

    public List<Object> getFrames_pending() {
        return frames_pending;
    }

    public void setFrames_pending(List<Object> frames_pending) {
        this.frames_pending = frames_pending;
    }

    public String getFrames_string() {
        return frames_string;
    }

    public void setFrames_string(String frames_string) {
        this.frames_string = frames_string;
    }

    public List<Object> getFrames() {
        return frames;
    }

    public void setFrames(List<Object> frames) {
        this.frames = frames;
    }

    @Override
    public String toString() {
        return "Post [id=" + id + ", tags=" + tags + ", created_at=" + created_at + ", creator_id=" + creator_id + ", author=" + author + ", change=" + change + ", source=" + source + ", score=" + score + ", md5=" + md5 + ", file_size=" + file_size + ", file_url=" + file_url + ", is_shown_in_index=" + is_shown_in_index + ", preview_url=" + preview_url + ", preview_width=" + preview_width + ", preview_height=" + preview_height + ", actual_preview_width=" + actual_preview_width + ", actual_preview_height=" + actual_preview_height + ", sample_url=" + sample_url + ", sample_width=" + sample_width
                + ", sample_height=" + sample_height + ", sample_file_size=" + sample_file_size + ", jpeg_url=" + jpeg_url + ", jpeg_width=" + jpeg_width + ", jpeg_height=" + jpeg_height + ", jpeg_file_size=" + jpeg_file_size + ", rating=" + rating + ", has_children=" + has_children + ", parent_id=" + parent_id + ", status=" + status + ", width=" + width + ", height=" + height + ", is_held=" + is_held + ", frames_pending_string=" + frames_pending_string + ", frames_pending=" + frames_pending + ", frames_string=" + frames_string + ", frames=" + frames + "]";
    }

}
