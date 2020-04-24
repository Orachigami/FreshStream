/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.info;

/**
 * Abstract class for retrieving data
 * @author Orachigami
 */
public abstract class VideoInfo {
    public enum PLATFORM {
        YOUTUBE,
        TWITCH
    }
    
    public abstract String getVideoId();
    public abstract String getTitle();
    public abstract String getConcurrentViewers();
    public abstract boolean isSubscribersCountHidden();
    public abstract String getSubscribersCount();
    public abstract boolean isLive();
    public abstract String getChannelId();
    public abstract String getStartTime();
    public abstract PLATFORM getPlatform();
}
