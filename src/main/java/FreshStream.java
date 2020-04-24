/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java;

import java.io.File;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import main.java.info.TwitchInfo;
import main.java.info.YouTubeInfo;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author Orachigami
 */
public class FreshStream extends JavaPlugin {
    
    private StreamsMenu menu;
    private BukkitTask updateTask;
    
    @Override
    public void onEnable() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }
        
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        YamlConfiguration messages = YamlConfiguration.loadConfiguration(messagesFile);
        Messages.TITLE = messages.getString("title");
        Messages.STREAM_ADDED = messages.getString("streamAdded");
        Messages.STREAM_REMOVED = messages.getString("streamRemoved");
        Messages.STREAM_INVITE = String.join("\n", (List<String>)messages.getList("streamInvite"));
        Messages.STREAM_INVITE_TOOLTIP = messages.getString("streamInviteTooltip");
        Messages.RELOADED = messages.getString("reloaded");
        Messages.HELP = String.join("\n", (List<String>)messages.getList("help"));
        Messages.YOUTUBE = messages.getString("youtube");
        Messages.TWITCH = messages.getString("twitch");
        Messages.INFO = String.join("\n",(List<String>)messages.getList("info"));
        
        Messages.NO_PERMISSIONS = messages.getString("noPermissions");
        Messages.NO_ITEM = messages.getString("noItem");
        Messages.NOT_ENOUGH_ARGUMENTS = messages.getString("notEnoughArguments");
        Messages.BAD_URL = messages.getString("badUrl");
        Messages.NOT_SETUP = messages.getString("notSetup");
        
        String youTubeApiKey = getConfig().getString("youTubeApiKey");
        String twitchApiKey = getConfig().getString("twitchApiKey");
        if (youTubeApiKey.equals("") || twitchApiKey.equals("")) {
            System.out.println(Messages.NOT_SETUP);
            return;
        }
        menu = new StreamsMenu(youTubeApiKey, twitchApiKey);
        long period = getConfig().getInt("updateTimeout") * (20L);
        getServer().getPluginManager().registerEvents(menu, this);
        updateTask = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            menu.updateStreams();
        }, period, period);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Messages.HELP);
            return false;
        }
        if (menu == null && !args[0].equals("reload")) {
            sender.sendMessage(Messages.NOT_SETUP);
            return false;
        }
        switch (args[0]) {
            case "add": 
                if (!sender.hasPermission("freshstream.streamer")) {
                    sender.sendMessage(Messages.NO_PERMISSIONS);
                    return false;
                }
                if (args.length < 2) {
                    sender.sendMessage(Messages.NOT_ENOUGH_ARGUMENTS);
                    return false;
                }
                ItemStack item = getServer().getPlayer(sender.getName()).getInventory().getItemInMainHand();
                if (item == null || item.getType() == Material.AIR) {
                    sender.sendMessage(Messages.NO_ITEM);
                    return false;
                }
                if (!menu.addStream(item, sender.getName(), args[1])) {
                    sender.sendMessage(Messages.BAD_URL);
                    return false;
                }
                sender.sendMessage(Messages.STREAM_ADDED);
                break;
            case "list": 
                if (!sender.hasPermission("freshstream.viewer")) {
                    sender.sendMessage(Messages.NO_PERMISSIONS);
                    return false;
                }
                Player p = getServer().getPlayer(sender.getName());
                menu.showTo(p);
                break;
            case "delete":
                if (args.length < 2) {
                    if (!sender.hasPermission("freshstream.streamer")) {
                        sender.sendMessage(Messages.NO_PERMISSIONS);
                        return false;
                    }
                    menu.deleteStream(sender.getName());
                    sender.sendMessage(Messages.STREAM_REMOVED);
                } else {
                    if (!sender.hasPermission("freshstream.moderator")) {
                        sender.sendMessage(Messages.NO_PERMISSIONS);
                        return false;
                    }
                    menu.deleteStream(args[1]);
                    sender.sendMessage(Messages.STREAM_REMOVED);
                }
                break;
            case "reload":
                if (!sender.hasPermission("freshstream.admin")) {
                    sender.sendMessage(Messages.NO_PERMISSIONS);
                    return false;
                }
                if (menu != null) HandlerList.unregisterAll(menu);
                if (updateTask != null) getServer().getScheduler().cancelTask(updateTask.getTaskId());
                reloadConfig();
                onEnable();
                sender.sendMessage(Messages.RELOADED);
                break;
            default: sender.sendMessage(Messages.HELP); break;
        }
        return true;
    }
}
