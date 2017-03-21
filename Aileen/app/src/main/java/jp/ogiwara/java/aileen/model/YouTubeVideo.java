package jp.ogiwara.java.aileen.model;

import java.io.Serializable;

/**
 * Created by ogiwara on 2017/03/20.
 */

public class YouTubeVideo implements Serializable{
    public String id;
    public String title;
    public String thumbnailURL;
    public String duration;
    public String viewCount;

    public YouTubeVideo(String id, String title, String thumbnailURL, String duration, String viewCount) {
        this.id = id;
        this.title = title;
        this.thumbnailURL = thumbnailURL;
        this.duration = duration;
        this.viewCount = viewCount;
    }

    public YouTubeVideo(){

    }
}
