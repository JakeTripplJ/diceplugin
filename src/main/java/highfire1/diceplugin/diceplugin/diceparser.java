package highfire1.diceplugin.diceplugin;

import java.util.ArrayList;
import java.util.Random;

public class diceparser {
    public static final String[] parsedice(String[] input, Random random_generator) {



        if (input.length == 0) {
            return new String[]{"At least one parameter required!"};
        }

        String roll = input[0];

        String str1 = (input.length > 1) ?
                String.join(",", input).substring(roll.length()) + ":" :
                "Rolling: ";


        // separate parameters to make dice easier to parse
        // generates string list in the form of {"1", "d", "20", "+", "35"}
        ArrayList<String> dicereader = new ArrayList<String>();

        String tempholder = "";
        for (char s : roll.toCharArray()) {
            if (s == 'd' || s == '+') {
                //TODO check is integer here
                dicereader.add(tempholder);
                tempholder = "";
                dicereader.add(String.valueOf(s));
            } else {
                tempholder += s;
            }
        }
        dicereader.add(tempholder);

        // THIS CODE IS HORRIBLE PAIN
        int counter = 0;
        for (String ohno : dicereader) {
            if (ohno.equals("d")) {
                counter++;
            }
        }
        if (counter > 1) {
            return new String[]{"only one dice allowed PLEASE HOLD WHILE I REWRITE THIS CODE"};
        }


        // QOL things
        // allow input like "d20" -> "1d20"
        // TODO REWORK
        if (dicereader.get(0).equals("d")) {
            dicereader.add(0, "1");
        }

        // parse input
        ArrayList<Integer> rolls = new ArrayList<Integer>();
        Integer total = 0;


        for(int i = 0; i < dicereader.size(); i++ ) {
            String cur_param = dicereader.get(i);

            // get last and next element to roll dice
            if (cur_param.equals("d")) {
                if (i == 0 || i >= dicereader.size()) {
                    return new String[]{"Malformed input boooo"};
                }
                int dice_amt = Integer.parseInt(dicereader.get(i-1));
                int dice_val = Integer.parseInt(dicereader.get(i+1));
                str1 += (dicereader.get(i-1) + "d" + dicereader.get(i+1) + " (");

                // actually roll dice
                for(int j=0; j<dice_amt; j++) {
                    int single_roll = random_generator.nextInt(dice_val) + 1;
                    str1 += (single_roll + ", ");
                    total += single_roll;
                }
                // prettyness
                str1 = str1.substring(0, str1.length()-2) + ")";
            }

            // ADDITION!
            if (cur_param.equals("+")) {
                if (i == dicereader.size()) {
                    return new String[]{"Malformed input boooo"};
                }
                str1 += " + " + dicereader.get(i+1);
                total += Integer.parseInt(dicereader.get(i+1));


            }
            // hooray for edge cases
            if (dicereader.size() == 1) {
                str1 += dicereader.get(i+1);
                total += Integer.parseInt(dicereader.get(i));
            }
        }

        String str2 = "Total: " + Integer.toString(total);

        String[] out = {str1, str2};
        return out;
    }
}
