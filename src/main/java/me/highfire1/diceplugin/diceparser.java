package me.highfire1.diceplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static me.highfire1.diceplugin.Diceplugin.default_mode;
import static me.highfire1.diceplugin.Diceplugin.default_mode_types;

public class diceparser {
    public static void parsedice(CommandSender sender, String[] args) {


        // check if a mode was selected
        String mode = default_mode;
        boolean param_exists = false;
        boolean temp = false;
        String str1 = "";

        int startint;
        if (args.length > 0 && args[0].contains("d")) {
            startint = 1;
        } else {
            startint = 0;
        }

        // iterate through args while also building str1
        for (int i = startint; i<args.length; i++) {

            // iterate through all default modes
            for (String default_mode : default_mode_types) {
                if (args[i].equals(("-" + default_mode))) {
                    mode = default_mode;
                    temp = true;
                    param_exists = true;
                    break;
                }
            }
            // build to str1 if not parameter
            if (!temp) {
                temp = false;
                str1 += args[i] + " ";
            }
        }

        if (str1.length() > 0) {
            str1 = str1.substring(0, str1.length() - 1) + ":";
        } else {
            str1 = "Rolling: ";
        }

        // default to 1d20 if "no" args provided
        if (    (args.length == 0 || args[0].length() == 0) ||
                (args.length == 1 && param_exists)
            ) {
            args = new String[] {"1d20"};
        }

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

        // Convert dice into numbers by iterating through dicereader
        for (int i=0; i<dicereader.size(); i++) {
            String param = dicereader.get(i); // will be either a roll/num or operator e.g. 1d20, +, -, 15, etc.

            // if parameter looks like dice then try to roll it
            if (param.contains("d")) {

                // make d20 -> 1d20
                if (param.charAt(0) == 'd') {
                    param = "1".concat(param);
                }

                String[] dice_parts = param.split("d", 2);

                if (dice_parts[0].equals("") || dice_parts[1].equals("")) {
                    sender.sendMessage("Malformed input.");
                    return;
                }
                // Catch bad args
                try {
                    Integer.parseInt(dice_parts[0]);
                    Integer.parseInt(dice_parts[1]);

                } catch (Exception e) {
                    sender.sendMessage("Malformed input.");
                    return;
                }

                // Finally roll dice now that dice parts are clean
                str1 = str1.concat(param + " (");
                int dice_num = Integer.parseInt(dice_parts[0]);
                int dice_val = Integer.parseInt(dice_parts[1]);

                int total = 0;
                for (int j = 0; j < dice_num; j++) {
                    int roll = ThreadLocalRandom.current().nextInt(dice_val) + 1;
                    str1 = str1.concat(roll + ", ");
                    total += roll;
                }

                // remove extra comma from last roll
                str1 = str1.substring(0, str1.length()-2) + ")";
                // replace "1d20" with dice output to use in evaluation
                dicereader.set(i, Integer.toString(total));

            // if parameter not like dice then just add it to str1
            } else if (param.contains("(") || param.contains(")")) {
                str1 = str1.concat(param);

            } else {
                str1 = str1.concat(" " + param);
            }
        }

        // evaluate the expression to output a total, taking into account parentheses/math
        int total_ = 0;
        try {
            total_ = math_eval(String.join("", dicereader));
        } catch (Exception e) {
            sender.sendMessage("Math failed :/ Error: " + e.getMessage());
            return;
        }

        String str2 = "Total: " + total_;
        // mode 1, aka self
        if (mode.equals(default_mode_types.get(0))) {
            sender.sendMessage(str1);
            sender.sendMessage(str2);
        // mode 2, aka to everyone
        } else if (mode.equals(default_mode_types.get(1))) {
            str1 = sender.getName() + ", " + str1;
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.sendMessage(str1);
                online.sendMessage(str2);
            }
            // also send to console, if sender is console
            if(!(sender instanceof Player)) {
                sender.sendMessage(str1);
                sender.sendMessage(str2);
            }
        } else {
            sender.sendMessage("ERROR!");
            for (String str : default_mode_types) {
                sender.sendMessage(str);
            }
            sender.sendMessage(mode);
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
}
