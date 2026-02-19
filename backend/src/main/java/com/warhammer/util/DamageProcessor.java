package com.warhammer.util;

/**
 * The logic engine for the Damage phase.
 * It transforms a wound distribution into a damage distribution by convolving 
 * individual weapon damage profiles for every possible wound count.
 */
public class DamageProcessor {

    /**
     * Calculates the total damage distribution based on the number of successful wounds 
     * and the damage characteristic of the weapon.
     * @param woundDist The probability array where index 'w' is the chance of 'w' wounds.
     * @param damageExpression The weapon damage (e.g., "2", "D3", "D6+1").
     * @return A probability array for total damage.
     */
    public static double[] calculateDamageDistribution(double[] woundDist, String damageExpression) {
        if (woundDist == null || woundDist.length == 0) {
            return new double[]{1.0};
        }

        // 1. Build the distribution for exactly ONE successful wound (e.g., D3+1)
        double[] singleWoundDamageDist = buildSingleWoundDist(damageExpression);
        
        // 2. Determine maximum possible damage to initialize the array
        int maxPossibleDamage = (woundDist.length - 1) * (singleWoundDamageDist.length - 1);
        double[] totalDamageDist = new double[maxPossibleDamage + 1];

        // 3. Optimization: Iterative Convolution
        double[] currentScenarioDist = {1.0}; 

        for (int w = 0; w < woundDist.length; w++) {
            double probOfWounds = woundDist[w];

            if (probOfWounds > 0.0000001) {
                for (int d = 0; d < currentScenarioDist.length; d++) {
                    totalDamageDist[d] += currentScenarioDist[d] * probOfWounds;
                }
            }

            if (w < woundDist.length - 1) {
                currentScenarioDist = ProbabilityMath.convolve(currentScenarioDist, singleWoundDamageDist);
            }
        }
        
        return totalDamageDist;
    }

    /**
     * Parses the damage string and creates a distribution for a single die/value.
     */
    private static double[] buildSingleWoundDist(String expr) {
        if (expr == null || expr.trim().isEmpty()) {
            return new double[]{0, 1.0}; 
        }
        
        expr = expr.toUpperCase().replace(" ", "");
        
        if (expr.contains("D6")) {
            return buildDiceDist(6, parseBonus(expr, "D6"));
        } 
        
        if (expr.contains("D3")) {
            return buildDiceDist(3, parseBonus(expr, "D3"));
        }

        try {
            int flat = Integer.parseInt(expr);
            double[] dist = new double[flat + 1];
            dist[flat] = 1.0;
            return dist;
        } catch (NumberFormatException e) {
            return new double[]{0, 1.0}; 
        }
    }

    private static double[] buildDiceDist(int sides, int bonus) {
        int maxVal = Math.max(0, sides + bonus);
        double[] dist = new double[maxVal + 1];
        double prob = 1.0 / sides;
        
        for (int i = 1; i <= sides; i++) {
            int result = i + bonus;
            if (result >= 0) {
                dist[result] += prob;
            } else {
                dist[0] += prob; 
            }
        }
        return dist;
    }

    private static int parseBonus(String expr, String diceType) {
        try {
            int opIndex = expr.indexOf(diceType) + diceType.length();
            if (opIndex >= expr.length()) return 0;
            
            String suffix = expr.substring(opIndex);
            if (suffix.startsWith("+")) {
                return Integer.parseInt(suffix.substring(1));
            } else if (suffix.startsWith("-")) {
                return -Integer.parseInt(suffix.substring(1));
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }
}