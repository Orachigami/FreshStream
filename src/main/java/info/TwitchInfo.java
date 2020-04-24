/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.info;

/**
 * 
 * @author Orachigami
 */
public final class TwitchInfo extends VideoInfo {
    public String id;
    public String user_id;
    public String user_name;
    public String game_id;
    public String type;
    public String title;
    public int viewer_count;
    public String started_at;
    public String language;
    public String thumbnail_url;
    public String followersCount;
    
    @Override
    public String getVideoId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getConcurrentViewers() {
        return Integer.toString(viewer_count);
    }

    @Override
    public boolean isLive() {
        return "live".equals(type);
    }

    @Override
    public String getChannelId() {
        return user_id;
    }

    @Override
    public String getStartTime() {
        return started_at;
    }

    @Override
    public PLATFORM getPlatform() {
        return VideoInfo.PLATFORM.TWITCH;
    }

    @Override
    public String getSubscribersCount() {
        return followersCount;
    }

    @Override
    public boolean isSubscribersCountHidden() {
        return false;
    }
}
