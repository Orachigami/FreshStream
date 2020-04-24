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
public final class YouTubeInfo extends VideoInfo {

    public static class Snippet {
        public String title;
        public String channelId;
        public String description;
        public String liveBroadcastContent;
    }
    
    public static class LiveStreamingDetails {
        public String actualStartTime;
        public String scheduledStartTime;
        public String concurrentViewers;
    }
    
    public static class Statistics {
        public String subscriberCount;
        public boolean hiddenSubscriberCount;
    }
    
    public String id;
    public Snippet snippet;
    public LiveStreamingDetails liveStreamingDetails;
    public Statistics statistics;
    
    @Override
    public String getVideoId() {
        return id;
    }

    @Override
    public String getTitle() {
        return snippet.title;
    }

    @Override
    public String getConcurrentViewers() {
        return liveStreamingDetails.concurrentViewers;
    }

    @Override
    public boolean isLive() {
        return "live".equals(snippet.liveBroadcastContent);
    }

    @Override
    public String getChannelId() {
        return snippet.channelId;
    }

    @Override
    public String getStartTime() {
        return liveStreamingDetails.scheduledStartTime;
    }

    @Override
    public PLATFORM getPlatform() {
        return PLATFORM.YOUTUBE;
    }

    @Override
    public boolean isSubscribersCountHidden() {
        return statistics.hiddenSubscriberCount;
    }

    @Override
    public String getSubscribersCount() {
        return statistics.subscriberCount;
    }
}
