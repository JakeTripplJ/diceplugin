package highfire1.diceplugin.diceplugin;

import java.util.Random;

public class diceparser {
    public static final String[] parsedice(String dice, Random random) {

        String[] diceparts = dice.split("d");
        int dicenum = Integer.parseInt(diceparts[0]);
        int dicetype = Integer.parseInt(diceparts[1]);

        int[] dicerolls = new int[dicenum];


        for (int i = 0; i < dicenum; i++) {
            dicerolls[i] = random.nextInt(dicetype) + 1;
        }

        String str1 = "Rolling " + dice + " (";
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

        String[] out = {str1, str2};
        return out;
    }
}
