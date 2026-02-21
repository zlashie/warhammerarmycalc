package com.warhammer.util;

import com.warhammer.dto.CalculationRequestDTO;

/**
 * Utility responsible for orchestrating the hitting phase of the combat math pipeline.
 * <p>
 * This processor handles the complex interactions between Ballistic Skill (BS), 
 * hit modifiers, reroll strategies (including "fishing" for criticals), and 
 * critical hit effects such as Lethal Hits and Sustained Hits. It uses iterative 
 * convolution to transform an attack distribution into a final hit distribution.
 */
public class HitProcessor {

    private static final int D6_SIDES = 6;
    private static final double PROB_PER_FACE = 1.0 / 6.0;
    
    // Array size 7 supports up to Sustained 6 (1 base + 5 bonus). 
    private static final int STANDARD_HITS_ARRAY_SIZE = 7;
    private static final int LETHAL_HITS_ARRAY_SIZE = 2;

    /**
     * Calculates the hit distribution for an entire unit by scaling model-level attacks.
     * <p>
     * This method first determines the total distribution of attacks for the unit (considering 
     * model count and variable attack characteristics like "D6"), then applies single-die 
     * outcome logic to transform those attacks into resulting hits.
     *
     * @param request The unit profile and active hit modifiers.
     * @return A HitResult containing separate probability distributions for standard hits 
     * and lethal auto-wounds.
     */
    public static HitResult calculateUnitDistribution(CalculationRequestDTO request) {
        int numModels = request.getNumberOfModels();
        if (numModels <= 0) {
            return new HitResult(new double[]{1.0}, new double[]{1.0}, new double[]{1.0});
        }

        // 1. Determine the unit-wide distribution of total attacks
        double[] singleModelAttackDist = buildExpressionDist(request.getAttacksPerModel());
        double[] unitAttackDist = {1.0};

        for (int i = 0; i < numModels; i++) {
            unitAttackDist = ProbabilityMath.convolve(unitAttackDist, singleModelAttackDist);
        }

        // 2. Determine the probability map for exactly one attack roll
        double[] singleStandard = new double[STANDARD_HITS_ARRAY_SIZE];
        double[] singleLethal = new double[LETHAL_HITS_ARRAY_SIZE];
        double[] singleTotal = new double[STANDARD_HITS_ARRAY_SIZE]; 

        calculateSingleDieOutcomes(singleStandard, singleLethal, singleTotal, request);

        // 3. Project the unit attack distribution onto the hit outcomes via convolution
        return transformAttacksToHits(unitAttackDist, singleStandard, singleLethal, singleTotal);
    }

    /**
     * Projects a distribution of attacks into resulting hit streams using iterative convolution.
     * <p>
     * This method acts as the transformation engine, mapping the probability of rolling 
     * 'N' attacks to the corresponding probability of achieving 'X' hits based on the 
     * single-die success rates.
     *
     * @param attackDist Probability array where index 'a' represents the chance of having 'a' attacks.
     * @param sStd Single-die probability map for standard hits.
     * @param sLethal Single-die probability map for lethal auto-wounds.
     * @param sTotal Single-die probability map for the combined hit pool.
     * @return A unified HitResult distribution for the unit.
     */
    private static HitResult transformAttacksToHits(double[] attackDist, double[] sStd, double[] sLethal, double[] sTotal) {
        int maxStd = (attackDist.length - 1) * (sStd.length - 1);
        int maxLethal = (attackDist.length - 1) * (sLethal.length - 1);
        int maxTotal = (attackDist.length - 1) * (sTotal.length - 1);

        double[] totalStd = new double[maxStd + 1];
        double[] totalLethal = new double[maxLethal + 1];
        double[] totalTrueHits = new double[maxTotal + 1];

        double[] currentStd = {1.0};
        double[] currentLethal = {1.0};
        double[] currentTotal = {1.0};

        for (int a = 0; a < attackDist.length; a++) {
            double probOfA = attackDist[a];
            if (probOfA > 0.0000001) {
                for (int i = 0; i < currentStd.length; i++) totalStd[i] += currentStd[i] * probOfA;
                for (int i = 0; i < currentLethal.length; i++) totalLethal[i] += currentLethal[i] * probOfA;
                for (int i = 0; i < currentTotal.length; i++) totalTrueHits[i] += currentTotal[i] * probOfA;
            }

            if (a < attackDist.length - 1) {
                currentStd = ProbabilityMath.convolve(currentStd, sStd);
                currentLethal = ProbabilityMath.convolve(currentLethal, sLethal);
                currentTotal = ProbabilityMath.convolve(currentTotal, sTotal);
            }
        }

        return new HitResult(totalStd, totalLethal, totalTrueHits);
    }

    /**
     * Parses a string attack characteristic (e.g., "D6", "D3+1", "2") into a probability distribution.
     *
     * @param expr The raw string input representing model attacks.
     * @return A probability array for a single model's attack output.
     */
    private static double[] buildExpressionDist(String expr) {
        if (expr == null || expr.isBlank()) return new double[]{0, 1.0};
        expr = expr.toUpperCase().replace(" ", "");
        
        if (expr.contains("D6")) return buildDiceDist(6, parseBonus(expr, "D6"));
        if (expr.contains("D3")) return buildDiceDist(3, parseBonus(expr, "D3"));

        try {
            int flat = Integer.parseInt(expr);
            double[] dist = new double[flat + 1];
            dist[flat] = 1.0;
            return dist;
        } catch (Exception e) {
            return new double[]{0, 1.0}; 
        }
    }

    /**
     * Generates a uniform probability distribution for a die roll with a flat bonus.
     *
     * @param sides Number of sides on the die (e.g., 3 or 6).
     * @param bonus The flat additive or subtractive modifier.
     * @return A probability array representing the modified die roll results.
     */
    private static double[] buildDiceDist(int sides, int bonus) {
        int maxVal = Math.max(0, sides + bonus);
        double[] dist = new double[maxVal + 1];
        double probPerSide = 1.0 / sides; 
        
        for (int i = 1; i <= sides; i++) {
            int result = Math.max(0, i + bonus);
            dist[result] += probPerSide;
        }
        return dist;
    }

    /**
     * Extracts a numerical bonus from a dice expression string (e.g., extracting 2 from "D6+2").
     *
     * @param expr The expression string.
     * @param type The dice type identifier to locate the bonus suffix.
     * @return The parsed integer bonus, or 0 if none is found.
     */
    private static int parseBonus(String expr, String type) {
        try {
            int index = expr.indexOf(type) + type.length();
            if (index >= expr.length()) return 0;
            String suffix = expr.substring(index);
            if (suffix.startsWith("+")) return Integer.parseInt(suffix.substring(1));
            if (suffix.startsWith("-")) return -Integer.parseInt(suffix.substring(1));
        } catch (Exception e) {}
        return 0;
    }

    /**
     * Simulates the outcome of a single D6 roll, accounting for modifiers and rerolls.
     * <p>
     * Results are categorized and added to the standard, lethal, and total hit probability maps.
     *
     * @param std Array to populate with standard hit probabilities.
     * @param lethal Array to populate with lethal hit probabilities.
     * @param total Array to populate with combined hit probabilities.
     * @param req The request containing BS and reroll settings.
     */
    private static void calculateSingleDieOutcomes(double[] std, double[] lethal, double[] total, CalculationRequestDTO req) {
        int bs = req.getBsValue();
        for (int face = 1; face <= D6_SIDES; face++) {
            if (shouldReroll(face, bs, req)) {
                for (int rerollFace = 1; rerollFace <= D6_SIDES; rerollFace++) {
                    processDiceFace(rerollFace, std, lethal, total, req, PROB_PER_FACE * PROB_PER_FACE);
                }
            } else {
                processDiceFace(face, std, lethal, total, req, PROB_PER_FACE);
            }
        }
    }

    /**
     * Evaluates a single D6 face against game rules to categorize the outcome.
     * <p>
     * Handles 40k mechanics where natural 1s always fail and natural 6s usually 
     * trigger critical effects like Lethal or Sustained Hits.
     *
     * @param face The die face value (1-6).
     * @param std Standard hit probability array.
     * @param lethal Lethal hit probability array.
     * @param total Total hit probability array.
     * @param req Active combat modifiers.
     * @param prob The weighted probability of this specific face occurring.
     */
    private static void processDiceFace(int face, double[] std, double[] lethal, double[] total, CalculationRequestDTO req, double prob) {
        boolean isCrit = face >= req.getCritHitValue();
        int effectiveRoll = req.isPlusOneToHit() ? face + 1 : face;
        boolean isHit = req.isTorrent() || (effectiveRoll >= req.getBsValue() && face != 1);

        if (isCrit && req.isLethalHits()) {
            lethal[1] += prob;
            if (req.isSustainedHits()) {
                applySustainedExplosionOnly(std, req.getSustainedValue(), prob);
                applySustainedWithBase(total, req.getSustainedValue(), prob); 
            } else {
                std[0] += prob; 
                total[1] += prob;
            }
        } else if (isCrit && req.isSustainedHits()) {
            lethal[0] += prob;
            applySustainedWithBase(std, req.getSustainedValue(), prob);
            applySustainedWithBase(total, req.getSustainedValue(), prob);
        } else if (isHit) {
            std[1] += prob;
            lethal[0] += prob;
            total[1] += prob;
        } else {
            std[0] += prob;
            lethal[0] += prob;
            total[0] += prob;
        }
    }

    /**
     * Handles specialized Sustained Hit logic where the base hit is siphoned into the Lethal pool.
     * <p>
     * This ensures that only the "exploded" extra hits are added to the standard hit distribution 
     * while the critical hit bypasses wounding.
     *
     * @param dist Target probability array.
     * @param value The sustained value string (e.g., "1", "D3").
     * @param prob The event probability.
     */
    private static void applySustainedExplosionOnly(double[] dist, String value, double prob) {
        if ("D3".equalsIgnoreCase(value)) {
            double split = prob / 3.0;
            dist[1] += split; dist[2] += split; dist[3] += split; 
        } else {
            int bonus = parseBonusValue(value);
            if (bonus > 0 && bonus < dist.length) dist[bonus] += prob;
            else dist[0] += prob; 
        }
    }

    /**
     * Applies standard Sustained Hit logic, adding both the original hit and any bonus explosions.
     *
     * @param dist Target probability array.
     * @param value The sustained characteristic.
     * @param prob The event probability.
     */
    private static void applySustainedWithBase(double[] dist, String value, double prob) {
        if ("D3".equalsIgnoreCase(value)) {
            double split = prob / 3.0;
            dist[2] += split; dist[3] += split; dist[4] += split; 
        } else {
            int bonus = parseBonusValue(value);
            int finalIndex = Math.min(1 + bonus, dist.length - 1);
            dist[finalIndex] += prob;
        }
    }

    /**
     * Determines if a specific die face should trigger a reroll based on user settings 
     * and current strategy (such as "fishing" for critical results).
     */
    private static boolean shouldReroll(int face, int bs, CalculationRequestDTO req) {
        boolean fishing = req.isSustainedHits() || req.isLethalHits();
        return DiceUtility.shouldReroll(face, bs, req.getRerollType(), fishing, req.getCritHitValue());
    }

    /**
     * Safely parses a bonus string into an integer.
     *
     * @param value The numerical string.
     * @return The parsed integer, or 0 if parsing fails or input is null.
     */
    private static int parseBonusValue(String value) {
        try { return (value != null) ? Integer.parseInt(value) : 0; }
        catch (NumberFormatException e) { return 0; }
    }
}