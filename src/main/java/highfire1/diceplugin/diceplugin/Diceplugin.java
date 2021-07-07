package highfire1.diceplugin.diceplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public final class Diceplugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender,
                             Command command,
                             String label,
                             String[] args) {
        if (command.getName().equalsIgnoreCase("roll")) {
            Random random = new Random();

            if (args.length > 1) {
                sender.sendMessage("Too many parameters!");
            } else if (args.length == 0) {
                sender.sendMessage("At least 1 parameter required!");
            } else {
                System.out.println("yay");
                String[] diceparts = args[0].split("d");
                int dicenum = Integer.parseInt(diceparts[0]);
                int dicetype = Integer.parseInt(diceparts[1]);

                int[] dicerolls = new int[dicenum];


                for (int i = 0; i < dicenum; i++) {
                    dicerolls[i] = random.nextInt(dicetype) + 1;
                }

                String str1 = "Rolling " + args[0] + " (";
                for (int roll : dicerolls) {
                    str1 += String.valueOf(roll);
                    str1 += ", ";
                }
                str1 = str1.substring(0, str1.length() - 2);
                str1 += ")";

                int total = 0;
                for (int roll : dicerolls) {
                    total += roll;
                }
                String str2 = "Total: " + total;
                sender.sendMessage(str1);
                sender.sendMessage(str2);
            }
            return true;
        }
        return false;
    }
}
