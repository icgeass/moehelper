package loli.kanojo.moehelper.bean;

/**
 * pool和post的id对应
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 * @version moehelper - v1.0.7
 * @url https://github.com/icgeass/moehelper
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

    public void setId(int id) {
        this.id = id;
    }

    public int getPool_id() {
        return pool_id;
    }

    public void setPool_id(int pool_id) {
        this.pool_id = pool_id;
    }

    public int getPost_id() {
        return post_id;
    }

    public void setPost_id(int post_id) {
        this.post_id = post_id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public int getNext_post_id() {
        return next_post_id;
    }

    public void setNext_post_id(int next_post_id) {
        this.next_post_id = next_post_id;
    }

    public int getPrev_post_id() {
        return prev_post_id;
    }

    public void setPrev_post_id(int prev_post_id) {
        this.prev_post_id = prev_post_id;
    }

    @Override
    public String toString() {
        return "Pool_post [id=" + id + ", pool_id=" + pool_id + ", post_id=" + post_id + ", active=" + active + ", sequence=" + sequence + ", next_post_id=" + next_post_id + ", prev_post_id=" + prev_post_id + "]";
    }

}
