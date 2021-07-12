package me.highfire1.diceplugin;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static me.highfire1.diceplugin.Diceplugin.*;


public class inline_dice_listener implements Listener {
    @EventHandler
    public void onChat(AsyncChatEvent event) {

        if (!inline_dice) {
            return;
        }

       String msg = PlainTextComponentSerializer.plainText().serialize(event.message());


        String[] msg_args = msg.split(" ");

        String param = "";
        for (int i = 0; i < msg_args.length; i++) {
            param = msg_args[i];

            // check if param starts and ends with brackets
            if (param.startsWith(inline_dice_start) && param.startsWith(inline_dice_end, param.length() - 2)) {

                String[] dice_output = diceparser.dice_logic(new String[]{param.substring(2, param.length()-2)});

                if (dice_output.length == 1) {
                    msg_args[i] = ChatColor.RED + "ERROR" + ChatColor.RESET;
                } else {
                    msg_args[i] = ChatColor.BOLD + dice_output[2] + ChatColor.RESET;
                }

            }
        }
        event.message(Component.text(String.join(" ", msg_args)));
    }
}
