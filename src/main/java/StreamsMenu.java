/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java;

import main.java.info.YouTubeInfo;
import main.java.info.TwitchInfo;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import main.java.info.VideoInfo;
import main.java.utils.JSONMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Orachigami
 */
public class StreamsMenu implements Listener {
    private final int maxSize = 21;
    private int size = 0;
    private int[] map = new int[] {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
    };
    private Inventory inventory;
    /** Player name and ItemStack HashMap */
    private ConcurrentHashMap<String, ItemStack> previews;
    /** Player name and URL HashMap */
    private ConcurrentHashMap<String, String> urls;
    private ConcurrentHashMap<String, Integer> slots;
    private String youtubeApiKey;
    private String twitchApiKey;

    public StreamsMenu(String youtubeApiKey, String twitchApiKey) {
        this.youtubeApiKey = youtubeApiKey;
        this.twitchApiKey = twitchApiKey;
        inventory = Bukkit.createInventory(null, 45, Messages.TITLE);
        previews = new ConcurrentHashMap<>();
        urls = new ConcurrentHashMap<>();
        slots = new ConcurrentHashMap<>();
        ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE);
        
        for (int i = 0; i < 9; i++)  { inventory.setItem(i, item); inventory.setItem(i + 36, item); }
        for (int i = 9; i < 36; i+=9) { inventory.setItem(i, item); inventory.setItem(i + 8, item); }
    }
    
    /**
     * Open menu to the player
     * @param player Player
     */
    public void showTo(Player player) {
        player.openInventory(inventory);
    }
    
    /**
     * Returns video info for url
     * @param url URL to YouTube or Twitch stream
     */
    private VideoInfo getVideoInfo(String url) {
        Pattern twitchPattern = Pattern.compile("^(https?:\\/\\/)?(www\\.)?twitch\\.tv\\/([^&/?\\s]+)", Pattern.CASE_INSENSITIVE);
        Pattern youtubePattern = Pattern.compile("^(https?:\\/\\/)?((www\\.)?youtube\\.com\\/watch\\?v=|youtu\\.be\\/)([^&/?\\s]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher;
        if ((matcher = twitchPattern.matcher(url)).find()) {
            String id = matcher.group(3);
            return getTwitchVideoInfo(id);
        } else if ((matcher = youtubePattern.matcher(url)).find()) {
            String id = matcher.group(4);
            return getYouTubeVideoInfo(id);
        } else return null;
    }
    
    /**
     * Generates lore according to Player name and VideoInfo
     * @param player Player nickname
     * @param info VideoInfo instance
     * @return 
     */
    private List<String> generateLore(String player, VideoInfo info) {
        String lore = Messages.INFO
                    .replaceAll("%player%", player)
                    .replaceAll("%viewers%", info.getConcurrentViewers().equals("-1") ? "0" : info.getConcurrentViewers())
                    .replaceAll("%status%", ""+info.isLive())
                    .replaceAll("%started%", info.getStartTime())
                    .replaceAll("%subscribers%", info.getSubscribersCount())
                    .replaceAll("%platform%", info.getPlatform() == VideoInfo.PLATFORM.YOUTUBE ? Messages.YOUTUBE : Messages.TWITCH );
        return Arrays.asList(lore.split("\n"));
    }
    
    /**
     * Adds stream to the list
     * @param item An item that will be used as a preview
     * @param player Player nickname who will be the owner of a stream
     * @param url Stream URL
     * @return true if added, false if exists
     */
    public boolean addStream(ItemStack item, String player, String url) {
        if (size >= maxSize) return false;
        VideoInfo info = getVideoInfo(url);
        if (info == null) return false;
        System.err.println("TITLE: " + info.getTitle());
        System.err.println("VIEWERS: " + info.getConcurrentViewers());
        System.err.println("LIVE: " + info.isLive());
        System.err.println("PLATFORM: " + (info.getPlatform() == VideoInfo.PLATFORM.YOUTUBE ? Messages.YOUTUBE : Messages.TWITCH));
        if (!previews.containsKey(player)) {
            ItemStack preview = item.clone();
            preview.setAmount(1);
            ItemMeta meta = preview.getItemMeta();
            meta.setDisplayName(info.getTitle().split("\n")[0]);
            meta.setLore(generateLore(player, info));
            preview.setItemMeta(meta);
            
            previews.put(player, preview);
            urls.put(player, url);
            inventory.setItem(map[size], preview);
            slots.put(player, map[size]);
            size++;
            return true;
        }
        return false;
    }
    
    /**
     * Removes player stream from the list if exists
     * @param player Player nickname who has a stream
     * @return true if removed, false if not found
     */
    public boolean deleteStream(String player) {
        if (previews.containsKey(player)) {
            previews.remove(player);
            urls.remove(player);
            inventory.setItem(slots.get(player), null);
            slots.remove(player);
            
            for (int i = 0; i < map.length - 1; i++) {
                inventory.setItem(map[i], inventory.getItem(map[i+1]));
            }
            inventory.setItem(map[map.length - 1], null);
            size--;
            return true;
        }
        return  false;
    }
    
    /**
     * Updates info in menu
     */
    public void updateStreams() {
        Iterator<Map.Entry<String,String>> it = urls.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,String> pair = it.next();
            VideoInfo info = getVideoInfo(pair.getValue());
            String player = pair.getKey();
            ItemStack preview = previews.get(player);
            ItemMeta meta = preview.getItemMeta();
            meta.setDisplayName(info.getTitle().split("\n")[0]);
            meta.setLore(generateLore(player, info));
            preview.setItemMeta(meta);
            inventory.setItem(slots.get(player), preview);
        }
    }
    
    private String doGetRequest(String url, String header, String value) {
        try {
            URL _url = new URL(url);
            URLConnection con = _url.openConnection();
            if (header != value && value != null) {
                con.setRequestProperty (header, value);
            }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                StringBuilder sb = new StringBuilder();
                while ((inputLine = in.readLine()) != null) 
                    sb.append(inputLine);
                in.close();
                return sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public TwitchInfo getTwitchVideoInfo(String id) {
        TwitchInfo info;
        Gson gson = new Gson();
        try {
            String url = String.format("https://api.twitch.tv/helix/streams?user_login=%s", id);
            String response = doGetRequest(url, "Client-ID", twitchApiKey);
            JsonObject jo = gson.fromJson(response, JsonElement.class).getAsJsonObject();
            info = gson.fromJson(jo.getAsJsonArray("data").get(0), TwitchInfo.class);
            url = String.format("https://api.twitch.tv/helix/users/follows?to_id=%s", info.user_id);
            response = doGetRequest(url, "Client-ID", twitchApiKey);
            jo = gson.fromJson(response, JsonElement.class).getAsJsonObject();
            info.followersCount = jo.get("total").getAsString();
            return info;
        } catch (NullPointerException e) {
            e.printStackTrace(); 
        }
        return null;
    }
    
    public YouTubeInfo getYouTubeVideoInfo(String id) {
        YouTubeInfo info;
        Gson gson = new Gson();
        try {
            String url = String.format("https://www.googleapis.com/youtube/v3/videos?id=%s&key=%s&part=snippet,liveStreamingDetails,statistics", id, youtubeApiKey);
            String response = doGetRequest(url, null, null);
            JsonObject jo = gson.fromJson(response, JsonElement.class).getAsJsonObject();
            info = gson.fromJson(jo.getAsJsonArray("items").get(0), YouTubeInfo.class);
            url = String.format("https://www.googleapis.com/youtube/v3/channels?id=%s&key=%s&part=statistics", info.snippet.channelId, youtubeApiKey);
            response = doGetRequest(url, null, null);
            jo = gson.fromJson(response, JsonElement.class).getAsJsonObject();
            info.statistics = gson.fromJson(jo.getAsJsonArray("items").get(0).getAsJsonObject().get("statistics"), YouTubeInfo.Statistics.class);
            return info;
        } catch (NullPointerException e) {
            e.printStackTrace(); 
        }
        return null;
    }
    
    @EventHandler
    public void InventoryClick(InventoryClickEvent e){
        if(e.getInventory().equals(inventory)){
            System.out.println(inventory.getItem(e.getRawSlot()));
            Player player = (Player)e.getWhoClicked();  
            e.setCancelled(true);
            if(e.getCurrentItem() == null){
                return;
            }
            
            int slot = e.getRawSlot();
            String playerName = null;
            for (Map.Entry<String, Integer> entry : slots.entrySet()) {
                if (entry.getValue().equals(slot)) playerName = entry.getKey();
            }
            if (playerName == null) return;
            
            ItemStack item = e.getCurrentItem();
            if (item.hasItemMeta()) {
                String url = urls.get(playerName);
                if (!url.startsWith("http")) url = "https://" + url;
                JSONMessage.create(Messages.STREAM_INVITE)
                        .color(ChatColor.GREEN)
                        .tooltip(Messages.STREAM_INVITE_TOOLTIP)
                        .openURL(url)
                        .send(player);
                Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("FreshStream"), ()->{player.closeInventory();});
            }
        }
    }
}
