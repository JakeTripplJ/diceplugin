package me.highfire1.diceplugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import java.util.List;
import static me.highfire1.diceplugin.Diceplugin.*;

public class diceparser implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("roll")) {

            String[] dice_output = dice_logic(args);
            Boolean isDicePool = false;

                // mode 0, aka error
            if (dice_output.length == 1) {
                sender.sendMessage(dice_output[0]);

            } else {
                isDicePool = Boolean.parseBoolean(dice_output[5]); // if not error, check if dice pool
            
                // mode 1, aka self
                if (dice_output[1].equals(default_mode_types.get(0))) {
                        if (isDicePool) {
                            sender.spigot().sendMessage(buildRollMessage(dice_output, sender, true));
                        } else {                
                            sender.sendMessage(dice_output[3] + dice_output[4]);
                            sender.sendMessage("Total: " + dice_output[2]);
                    }

                    // mode 2, aka to everyone
                } else if (dice_output[1].equals(default_mode_types.get(1))) {

                    sendToAll(sender, dice_output, isDicePool); // made into a function due to repetition of logic

                } else if ((dice_output[1].equals(default_mode_types.get(2)))) { // mode 2 - in range 

                    if (enable_placeholderapi_for_range) { // check placeholder for range
                        try {

                            String placeHolderString = PlaceholderAPI.setPlaceholders((Player) sender, roll_message_range_placeholder);

                            if (placeHolderString.equalsIgnoreCase("none")) { // if range placeholder is "none" then send roll to all instead
                                sendToAll(sender, dice_output, isDicePool);
                                return true;
                            } else { // if it's not "none" then treat it as a double
                                roll_message_range = Double.parseDouble(placeHolderString);
                            }

                        } catch (NumberFormatException e) {
                            sender.sendMessage("Your range placeholder is not set up properly");
                            return false;
                        }
                    }

                    String sendername = sender.getName();
                    String str1 = sendername + ", " + dice_output[3] + dice_output[4];
                    String str2 = "";
                    TextComponent hoverResultsToAll = new TextComponent();

                    if (isDicePool) { // if pool, send fancy hovertext
                        str2 = dice_output[2];
                        hoverResultsToAll = buildRollMessage(dice_output, sender, false);
                    } else { // if not, normal message
                        str2 = "Total: " + dice_output[2];
                    }

                    Player playerRolling = (Player) sender; 
                    // get nearby players
                    List<Entity> nearbyEntities = playerRolling.getNearbyEntities(roll_message_range, roll_message_range, roll_message_range);
                    nearbyEntities.add((Entity) playerRolling); // include sender

                    for (Entity nearbyEntity : nearbyEntities) { // send to all nearby players
                        if (nearbyEntity.getType().equals(EntityType.PLAYER)) { 
                            if (isDicePool) { // send hover text if dice pool
                                nearbyEntity.spigot().sendMessage(hoverResultsToAll);
                            } else { // send normal text if not
                                nearbyEntity.sendMessage(str1);
                                nearbyEntity.sendMessage(str2);
                            }
                        }
                    }

                } else {
                    sender.sendMessage("Something has gone catastrophically wrong. Dumping values: ");
                    for (String val : dice_output) {
                        sender.sendMessage(val);
                    }
                }
            }
        }
        return true;
    }
    // TODO convert to hashmap + exceptions at some point
    // OUTPUTS
    // [0] - error       - if no error, blank
    // [1] - mode        - all / self / range
    // [2] - total       - 23
    // [3] - title       - Rolling:
    // [4] - dice rolls  - 1d20 (2, 4) + 23
    // [5] - dice pool   - true if a pool, false otherwise 

    public static String[] dice_logic(String[] args) {
        // check if a mode was selected
        String mode = default_mode;
        boolean dontAddArgToString = false;
        String str1 = "";
        boolean isDicePool = false;
        String poolResults = "";

        // default to 1d20 if "no" args provided
        if (args.length == 0 || !args[0].contains("d")) { // default roll behavior
            String[] first =  new String[]{default_roll};
            
            // from stackoverflow
            // adds d20 and args
            int length = first.length + args.length;
            String[] new_array = new String[length];
            System.arraycopy(first, 0, new_array, 0, first.length);
            System.arraycopy(args, 0, new_array, first.length, args.length);

            args = new_array;
        }

        // iterate through args while also building str1
        for (int i = 1; i < args.length; i++) {

            // if the argument doesn't start with a hyphen or match dice rules, throw an error
            if (!args[i].matches("^d[0-9]+|^[0-9]+d[0-9]+|^-.+")) {
                // dontAddArgToString = true;
                // break;
                return new String[]{"Malformed input."};
            } 

            // iterate through all default modes
            for (String default_mode : default_mode_types) {
                if (args[i].equals(("-" + default_mode))) {
                    mode = default_mode;
                    dontAddArgToString = true;
                    break;
                }
            }
            // build to str1 if not parameter
            if (!dontAddArgToString) {
                str1 += args[i] + " ";
            }
            dontAddArgToString = false; // replacement for if/else
        }

        if (str1.length() > 0) {
            str1 = str1.substring(0, str1.length() - 1) + ": ";
        } else {
            str1 = "Rolling: ";
        }

        String title = str1;
        str1 = "";

        // build output string
        // use custom text if exists, else default to generic message
        //String str1 = (args.length >= 2 && (!param_exists || args.length >= 3)) ?
        //        String.join(" ", args).substring(1).substring(args[0].length()) + " : " :
        //        "Rolling: ";
        //str1.replace(" -" + mode, "");

        // Preprocess args
        // generates string list in the form of {"1d20", "+", "35"}
        // iterates through every char in args, generates a split at every operator
        // operators are +-*/^()
        ArrayList<String> dicereader = new ArrayList<>();
        StringBuilder temp_holder = new StringBuilder();

        for (char s : args[0].toCharArray()) {

            // check for pool
            if (Character.toString(s).equals("@")) { // if the message has an @ then assume it's a pool
                isDicePool = true;
            }

            if ("+-*/^()".contains(Character.toString(s))) {
                if (temp_holder.length() > 0) {
                    dicereader.add(temp_holder.toString());
                    temp_holder = new StringBuilder(); // equivalent to clearing StringBuilder as setLength is bad
                }
                dicereader.add(String.valueOf(s));

            } else if (s != ' ') {
                temp_holder.append(s);
            }
        }
        dicereader.add(temp_holder.toString());

        int dicecount = 0;

        // Convert dice into numbers by iterating through dicereader
        for (int i = 0; i < dicereader.size(); i++) {
            String param = dicereader.get(i); // will be either a roll/num or operator e.g. 1d20, +, -, 15, etc.

            // if parameter looks like dice then try to roll it
            if (param.contains("d")) {


                // make d20 -> 1d20
                if (param.charAt(0) == 'd') {
                    param = "1".concat(param);
                }

                String[] dice_parts;
                if (isDicePool) {
                    dice_parts = param.split("d|@", 3);
                } else {
                    dice_parts = param.split("d", 2);
                    if (convert_regular_rolls_to_pools) { // convert to pool if config setting is enabled

                        try {
                            Integer.parseInt(dice_parts[1]);
                        } catch (Exception e) {
                            return new String[]{"Malformed input."};
                        }

                        Integer pool_value = (Integer.parseInt(dice_parts[1]) / 2) + 1; // (n/2)+1 logic

                        String[] temp_dice_parts = new String[dice_parts.length + 1];
                        System.arraycopy(dice_parts, 0, temp_dice_parts, 0, dice_parts.length); // copy array to new array with one more slot
                        temp_dice_parts[2] = pool_value.toString(); // fill new slot with pool value
                        dice_parts = temp_dice_parts;
                        param += "@" + pool_value; // append pool designator 
                        
                        isDicePool = true; // it is now a pool, treat it as such
                    }
                }
                
                if (dice_parts[0].equals("") || dice_parts[1].equals("")) {
                    return new String[]{"Malformed input."};
                }
                // Catch bad args
                try {
                    Integer.parseInt(dice_parts[0]);
                    Integer.parseInt(dice_parts[1]);
                    if (isDicePool) Integer.parseInt(dice_parts[2]);

                } catch (Exception e) {
                    return new String[]{"Malformed input."};
                }

                // Finally roll dice now that dice parts are clean
                str1 = str1.concat(param + " (");
                int dice_num = Integer.parseInt(dice_parts[0]);
                int dice_val = Integer.parseInt(dice_parts[1]);
                int pool_criteria = 0;
                if (isDicePool) pool_criteria = Integer.parseInt(dice_parts[2]); // number for pool value to be equal or greater to

                String tempstring = "";

                // max_dice_per_roll check
                dicecount += dice_num;
                if (dicecount > max_dice_per_roll) {
                    return new String[]{"Too many dice!"};
                }

                int total = 0;
                int poolSuccesses = 0;

                for (int j = 0; j < dice_num; j++) {
                    int roll = ThreadLocalRandom.current().nextInt(dice_val) + 1;

                    String colorModifiers = "";

                    if (isDicePool) {
                        if (roll >= pool_criteria) {
                            poolSuccesses += 1;
                            colorModifiers += ChatColor.GREEN; // green on pool success
                        } else {
                            colorModifiers += ChatColor.RED; // red on pool failure
                        }
                    }

                    if (roll == dice_val || roll == 1) {
                        colorModifiers += ChatColor.BOLD; // embolden rolls of 1 and perfect rolls 
                    }

                    if (colorModifiers != "") {
                        tempstring = tempstring.concat(colorModifiers + Integer.toString(roll) + ChatColor.RESET + ", ");
                    } else {
                        tempstring = tempstring.concat(roll + ", ");
                    }
                    total += roll;
                }

                // pool results get their own variable due to complexity
                if (isDicePool) { 
                    poolResults = ChatColor.GREEN + Integer.toString(poolSuccesses)            + ChatColor.RESET + " successes, " 
                                + ChatColor.RED   + Integer.toString(dice_num - poolSuccesses) + ChatColor.RESET + " failures";
                } 

                // if string for dice is longer than max_char_per_dice, cut it
                if ((max_char_per_dice != 0) && (tempstring.length() > max_char_per_dice)) {
                    str1 += tempstring.substring(0, max_char_per_dice) + "...)";
                } else {
                    str1 += tempstring.substring(0, tempstring.length() - 2) + ")";
                }
                
                // replace dice string with dice "integer" to use in evaluation
                dicereader.set(i, Integer.toString(total));

            // if parameter not like dice then just add it to str1
            } else if (param.contains("(") || param.contains(")")) {
                str1 = str1.concat(param);

            } else {
                str1 = str1.concat(" " + param);
            }
        }

        String[] listOfRolls = str1.split(" "); // create arraylist of rolls
        
        if ((max_results_length != 0) && ((listOfRolls.length - 1) > max_results_length)) { // subtract 1 because the type of roll is included in this list
            String[] trimmedListOfRolls = new String[max_results_length + 1];
            System.arraycopy(listOfRolls, 0, trimmedListOfRolls, 0, max_results_length + 1);
            str1 = String.join(" ", trimmedListOfRolls);
            str1 += " ... )";
        }
        
        // evaluate the expression to output a total, taking into account parentheses/math
        String total_ = "";
        if (!isDicePool){ // only check math for non pools 
            try {
                total_ = Integer.toString(math_eval(String.join("", dicereader)));
            } catch (Exception e) {
                return new String[]{"Math failed :/ Error: " + e.getMessage()};
            }
        }

        // duplicate comment for convenience
        // [0] - error       - if no error, blank
        // [1] - mode        - all / self / range
        // [2] - total       - 23 (If dice pool, is replaced with a string showing results)
        // [3] - title       - Rolling:
        // [4] - dice rolls  - 1d20 (2, 4) + 23
        // [5] - dice pool   - true if a pool, false otherwise 

        if (isDicePool) {
            return new String[]{"", mode, poolResults, title, str1, Boolean.toString(isDicePool)};
        } else {
            return new String[]{"", mode, total_, title, str1, Boolean.toString(isDicePool)};
        }
    }

    // Courtesy of Boann from Stackoverflow @
    // https://stackoverflow.com/questions/3422673/how-to-evaluate-a-math-expression-given-in-string-form
    public static int math_eval(final String str) {
        return (int) new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

    public static TextComponent buildRollMessage( String[] dice_output, CommandSender sender, Boolean isSelfRoll) {
        TextComponent hoverResults = new TextComponent();
        String sendername = sender.getName(); 

        String rollTypeString = dice_output[4].split(" ")[0]; // type of roll (e.g. 20d20)

        if (!include_pool_text_in_roll) {
            rollTypeString = rollTypeString.split("@")[0];
        }

        if (isSelfRoll) { // if self roll, sub name for "You"
            sendername = "You";
        } else if (enable_placeholderapi_for_player_names) { // if using name placeholders, replace sendername with it
            try {
                sendername = PlaceholderAPI.setPlaceholders((Player) sender, player_name_placeholder);
            } catch (Exception e) {
                sender.sendMessage("Your player name placeholder is not set properly");
            }
        }

        if (message_style.equals("default")) { // vanilla style text
            hoverResults = new TextComponent(sendername + " rolled " + ChatColor.BOLD + rollTypeString + ChatColor.RESET + ": " + dice_output[2]); // chat message akin to "2 successes, 2 failures"
            hoverResults.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(dice_output[4]))); // hover text akin to (1, 2, 3, 4)

        } else if (message_style.equals("special")) { // gray text

            String prefaceText = "[" + ChatColor.DARK_AQUA + "Rolls" + ChatColor.RESET + "] ";

            String diceOutputString = dice_output[2]; 
            diceOutputString = diceOutputString.replace(ChatColor.RESET.toString(), ChatColor.GRAY.toString()); // replace white text with gray text 

            hoverResults = new TextComponent(prefaceText + 
                                                ChatColor.GOLD + sendername + 
                                                ChatColor.GRAY + " rolled a " + 
                                                ChatColor.GOLD + rollTypeString + 
                                                ChatColor.GRAY + ": " + 
                                                diceOutputString + "."); // chat message akin to "2 successes, 2 failures"

            hoverResults.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(dice_output[4]))); // hover text akin to (1, 2, 3, 4)
        }

        return hoverResults;
    }

    public static void sendToAll(CommandSender sender, String[] dice_output, Boolean isDicePool) {
        String sendername = sender.getName();
        String str1 = sendername + ", " + dice_output[3] + dice_output[4];
        String str2 = "";
        TextComponent hoverResultsToAll = new TextComponent();

        if (isDicePool) {
            str2 = dice_output[2];
            hoverResultsToAll = buildRollMessage(dice_output, sender, false);

        } else {        
            str2 = "Total: " + dice_output[2];
        }

        // send to all players
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (isDicePool) { // send hover text if dice pool
                online.spigot().sendMessage(hoverResultsToAll);
            } else { // send normal text if not
                online.sendMessage(str1);
                online.sendMessage(str2);
            }
        }

        // also send to console, if sender is console
        if (!(sender instanceof Player)) {
            if (isDicePool) { // send hover text if dice pool
                sender.spigot().sendMessage(hoverResultsToAll);
            } else { // send normal text if not
                sender.sendMessage(str1);
                sender.sendMessage(str2);
            }
        }
    }

}
