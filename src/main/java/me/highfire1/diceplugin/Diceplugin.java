/*
TODO :
test other versions
custom events/api

make .jar smaller (?)

 */

package me.highfire1.diceplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class Diceplugin extends JavaPlugin implements Listener {
    static ArrayList<String> default_mode_types = new ArrayList<>();
    static String default_mode;
    static Integer max_char_per_dice;
    static Integer max_dice_per_roll;
    static Boolean inline_dice;
    static String inline_dice_start;
    static String inline_dice_end;
    static String message_style;
    static String default_roll;
    static Boolean convert_regular_rolls_to_pools;
    static Double roll_message_range;
    static Boolean enable_placeholderapi_for_range;
    static String roll_message_range_placeholder;
    static Boolean include_pool_text_in_roll;
    static Boolean enable_placeholderapi_for_player_names;
    static String player_name_placeholder;
    static Integer max_results_length;


    @Override
    public void onEnable() {

        // Check if PAPI is installed
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            /*
             * We register the EventListener here, when PlaceholderAPI is installed.
             * Since all events are in the main class (this class), we simply use "this"
             */
            Bukkit.getPluginManager().registerEvents(this, this);
        } else {
            /*
             * We inform about the fact that PlaceholderAPI isn't installed and then
             * disable this plugin to prevent issues.
             */
            getLogger().warning("Could not find PlaceholderAPI! Running without support");
            // Bukkit.getPluginManager().disablePlugin(this);
        }
        
        // get configs from file
        this.saveDefaultConfig();
        default_mode = this.getConfig().getString("defaultmode");
        default_mode_types = new ArrayList<>();
        default_mode_types.add(this.getConfig().getString("defaultmode1"));
        default_mode_types.add(this.getConfig().getString("defaultmode2"));
        default_mode_types.add(this.getConfig().getString("defaultmode3"));

        max_char_per_dice = this.getConfig().getInt("max_char_per_dice");
        max_dice_per_roll = this.getConfig().getInt("max_dice_per_roll");
        max_results_length = this.getConfig().getInt("max_results_length");

        enable_placeholderapi_for_range = this.getConfig().getBoolean("enable_placeholderapi_for_range");
        roll_message_range = this.getConfig().getDouble("roll_message_range");
        roll_message_range_placeholder = this.getConfig().getString("roll_message_range_placeholder");        

        inline_dice = this.getConfig().getBoolean("inline_dice");
        inline_dice_start = this.getConfig().getString("inline_dice_start");
        inline_dice_end = this.getConfig().getString("inline_dice_end");

        message_style = this.getConfig().getString("message_style");
        default_roll = this.getConfig().getString("default_roll");

        convert_regular_rolls_to_pools = this.getConfig().getBoolean("convert_regular_rolls_to_pools");
        include_pool_text_in_roll = this.getConfig().getBoolean("include_pool_text_in_roll");

        enable_placeholderapi_for_player_names = this.getConfig().getBoolean("enable_placeholderapi_for_player_names");
        player_name_placeholder = this.getConfig().getString("player_name_placeholder");

        this.saveConfig();

        getCommand("roll").setExecutor(new diceparser());
        getServer().getPluginManager().registerEvents(new inline_dice_listener(), this);


    }

    @Override
    public void onDisable() {
    }


    @Override
    public boolean onCommand(CommandSender sender,
                             Command command,
                             String label,
                             String[] args) {

        if(command.getName().equalsIgnoreCase("diceplugin_config")) {


            // input validation
            if (args.length == 0) {
                sender.sendMessage("No arguments provided. Correct usage: /Diceplugin_config <config> <new value (optional)>");
                return true;
            } else if (args.length > 2) {
                sender.sendMessage("Too many arguments. Correct usage: /Diceplugin_config <config> <new value (optional)>");
                return true;
            }

            // try to access/write value
            String config_value;
            try {
                if (this.getConfig().getString(args[0]) == null) {
                    sender.sendMessage("Config doesn't exist.");

                } else {
                    config_value = this.getConfig().getString(args[0]);

                    if(args.length == 2) {
                        // save to config
                        this.getConfig().set(args[0], args[1]);
                        // save to actual config file
                        this.saveConfig();
                        // reload in plugin variables
                        onEnable();

                        sender.sendMessage("[" + this.getName() + "] " + args[0] + " is now " + args[1]);
                    } else {
                        sender.sendMessage("[" + this.getName() + "] " + args[0] + " is " + config_value);
                    }
                }

            } catch (Exception e) {
                sender.sendMessage("Sorry, something went wrong. Error: " + e.getMessage());
            }
            return true;

        } else {
                return false;
            }
        }
    }

