package com.warhammer.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Warhammer dice notation (e.g., "2D6+2", "D3") into statistical components.
 * Based on the properties of a Discrete Uniform Distribution.
 */
public class DiceParser {
    private static final String REGEX = "(?<count>\\d*)D(?<faces>\\d+)(?:\\+(?<mod>\\d+))?";
    private static final Pattern DICE_PATTERN = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

    // Statistical constants for a Discrete Uniform Distribution
    private static final double MEAN_DIVISOR = 2.0;   
    private static final double VARIANCE_DIVISOR = 12.0; 

    public record DiceStat(double mean, double variance) {}

    public static DiceStat parse(String input) {
        if (isInvalid(input)) {
            return new DiceStat(0.0, 0.0);
        }

        String cleanedInput = input.trim().toUpperCase();
        Matcher matcher = DICE_PATTERN.matcher(cleanedInput);

        if (matcher.matches()) {
            return calculateDiceProbability(matcher);
        }

        return parseAsStaticValue(cleanedInput);
    }

    private static DiceStat calculateDiceProbability(Matcher matcher) {
        double count = parseGroup(matcher, "count", 1.0); 
        double faces = parseGroup(matcher, "faces", 0.0);
        double modifier = parseGroup(matcher, "mod", 0.0);

        // Mean of a single die: (n + 1) / 2
        double singleDieMean = (faces + 1.0) / MEAN_DIVISOR;
        double totalMean = (count * singleDieMean) + modifier;

        // Variance of a single die: (n^2 - 1) / 12
        double singleDieVariance = (Math.pow(faces, 2) - 1.0) / VARIANCE_DIVISOR;
        double totalVariance = count * singleDieVariance;

        return new DiceStat(totalMean, totalVariance);
    }

    private static DiceStat parseAsStaticValue(String input) {
        try {
            double val = Double.parseDouble(input.replaceAll("[^\\d.]", ""));
            return new DiceStat(val, 0.0);
        } catch (NumberFormatException e) {
            return new DiceStat(0.0, 0.0);
        }
    }

    private static double parseGroup(Matcher matcher, String groupName, double defaultValue) {
        String val = matcher.group(groupName);
        return (val == null || val.isEmpty()) ? defaultValue : Double.parseDouble(val);
    }

    private static boolean isInvalid(String input) {
        return input == null || input.isBlank();
    }
}