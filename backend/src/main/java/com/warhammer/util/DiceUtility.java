package com.warhammer.util;

public class DiceUtility {
    /**
     * Shared logic for rerolling dice. 
     * @param face The die result (1-6)
     * @param target The value needed (BS or Wound Roll)
     * @param type The reroll strategy (ONES, FAIL, ALL)
     * @param fishing Whether the user is fishing for Criticals
     * @param critValue The value that counts as a Critical (usually 6)
     */
    public static boolean shouldReroll(int face, int target, String type, boolean fishing, int critValue) {
        return switch (type) {
            case "ONES" -> face == 1;
            case "FAIL" -> face < target || face == 1;
            case "ALL"  -> {
                if (fishing) yield face < critValue;
                yield face < target || face == 1;
            }
            default -> false;
        };
    }
}