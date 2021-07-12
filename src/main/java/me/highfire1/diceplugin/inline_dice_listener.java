package me.highfire1.diceplugin;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class inline_dice_listener implements Listener {
    @EventHandler
    public void onChat(AsyncChatEvent event) {
        String msg = event.message().toString();
        if (msg.contains("[[") && msg.contains("]]")) {
            String roll = msg.substring(msg.indexOf("[["), msg.indexOf("]]"));
        }

    }
}
