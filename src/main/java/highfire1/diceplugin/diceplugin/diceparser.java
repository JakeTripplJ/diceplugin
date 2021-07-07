package highfire1.diceplugin.diceplugin;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;

import java.util.ArrayList;
import java.util.Random;

public class diceparser {
    public static String[] parsedice(String[] input, Random random_generator) {


        // default to 1d20 if no input provided
        if (input.length == 0 || input[0].length() == 0) {
            input = new String[] {"1d20"};
        }

        // build output string
        String str1 = (input.length > 1) ?
                String.join(" ", input).substring(1).substring(input[0].length()) + " : " :
                "Rolling: ";

        // Preprocess input
        // generates string list in the form of {"1d20", "+", "35"}
        ArrayList<String> dicereader = new ArrayList<>();

        String tempholder = "";
        for (char s : input[0].toCharArray()) {
            if (s == '-' || s == '+' || s == '(' || s == ')' || s == '*' || s == '/' || s == '^') {
                if (tempholder.length() > 0) {
                    dicereader.add(tempholder);
                    tempholder = "";
                }
                dicereader.add(String.valueOf(s));
            } else if (s != ' ') {
                tempholder += s;
            }
        }
        dicereader.add(tempholder);

        // Convert dice into numbers
        //ArrayList<Integer> dicerolls = new ArrayList<>();

        for (int i=0; i<dicereader.size(); i++) {
            String param = dicereader.get(i);

            if (param.contains("d")) {

                if (param.charAt(0) == 'd') {
                    param = "1".concat(param);
                }

                String[] dice_parts = param.split("d");
                int total = 0;

                str1 = str1.concat(param + " (");

                try {
                    int dice_num = Integer.parseInt(dice_parts[0]);
                    int dice_val = Integer.parseInt(dice_parts[1]);

                    for (int j = 0; j < dice_num; j++) {
                        int roll = random_generator.nextInt(dice_val) + 1;
                        str1 = str1.concat(roll + ", ");
                        total += roll;
                    }

                } catch (Exception e) {
                    return new String[]{ChatColor.of(String.valueOf(Color.RED)) + "Error: Malformed input"};
                }

                str1 = str1.substring(0, str1.length()-2) + ")";
                dicereader.set(i, Integer.toString(total));

                // Fancy spacing for parentheses and operators
            } else if (param.contains("(") || param.contains(")")) {
                str1 = str1.concat(param);


            } else {
                str1 = str1.concat(" " + param + " ");
            }
        }

        double total_ = math_eval(String.join("", dicereader));
        String str2 = "Total: " + (int)total_;

        return new String[] {str1, str2};
    }


    // Courtesy of Boann from Stackoverflow @
    // https://stackoverflow.com/questions/3422673/how-to-evaluate-a-math-expression-given-in-string-form
    public static double math_eval(final String str) {
        return new Object() {
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
