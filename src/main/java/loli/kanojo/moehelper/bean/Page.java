package loli.kanojo.moehelper.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 对应Post和Pool页面的数据对象
 * 
 * @author icgeass@hotmail.com
 * @date 2015年6月2日
 * @version moehelper - v1.0.7
 * @url https://github.com/icgeass/moehelper
 */
public class Page implements Comparable<Page> {

    private List<Post> posts;
    private List<Pool> pools;
    private List<Pool_post> pool_posts;
    private Tags tags;
    private Votes votes;

    public void initPage() {
        posts = new ArrayList<Post>();
        pools = new ArrayList<Pool>();
        pool_posts = new ArrayList<Pool_post>();
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public List<Pool> getPools() {
        return pools;
    }

    public void setPools(List<Pool> pools) {
        this.pools = pools;
    }

    public List<Pool_post> getPool_posts() {
        return pool_posts;
    }

    public void setPool_posts(List<Pool_post> pool_posts) {
        this.pool_posts = pool_posts;
    }

    public Tags getTags() {
        return tags;
    }

    public void setTags(Tags tags) {
        this.tags = tags;
    }

    public Votes getVotes() {
        return votes;
    }

    public void setVotes(Votes votes) {
        this.votes = votes;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Post post : this.posts) {
            sb.append(post.toString() + "\r\n");
        }
        for (Pool pool : this.pools) {
            sb.append(pool.toString() + "\r\n");
        }
        for (Pool_post pool_post : this.pool_posts) {
            sb.append(pool_post.toString() + "\r\n");
        }
        return "Page \r\n" + sb.toString();
    }

    /**
     * 需要保证Page中至少含有一张Post且该Post含有Id属性
     * 对于Post, 不能有all deleted和empty两种情况
     * 对于Pool, 不能用此接口, 因为这样排是没有意义的, 用循环
     */
    @Override
    public int compareTo(Page o) {
        return this.getPosts().get(0).getId() - o.getPosts().get(0).getId();
    }

}
