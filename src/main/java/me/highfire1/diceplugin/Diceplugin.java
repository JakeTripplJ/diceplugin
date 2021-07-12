/*
TODO :
inline rolls
permissions and all they entail
bold on nat 1/20s
test other versions
custom events

make .jar smaller (?)

 */

package me.highfire1.diceplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class Diceplugin extends JavaPlugin {
    static ArrayList<String> default_mode_types = new ArrayList<>();
    static String default_mode;
    static Integer max_char_per_dice;
    static Integer max_dice_per_roll;
    static Boolean inline_dice;

    @Override
    public void onEnable() {
        // get configs from file
        this.saveDefaultConfig();
        default_mode = this.getConfig().getString("defaultmode");
        default_mode_types = new ArrayList<>();
        default_mode_types.add(this.getConfig().getString("defaultmode1"));
        default_mode_types.add(this.getConfig().getString("defaultmode2"));

        max_char_per_dice = this.getConfig().getInt("max_char_per_dice");
        max_dice_per_roll = this.getConfig().getInt("max_dice_per_roll");

        inline_dice = this.getConfig().getBoolean("inline_dice");

        getCommand("roll").setExecutor(new diceparser());

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


            } catch (Exception e) {
                sender.sendMessage("Sorry, something went wrong. Error: " + e.getMessage());
            }
            return true;

        } else {
                return false;
            }
        }
    }

