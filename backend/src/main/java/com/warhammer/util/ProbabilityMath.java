package com.warhammer.util;

public class ProbabilityMath {
    
    public static double[] convolve(double[] A, double[] B) {
        double[] result = new double[A.length + B.length - 1];
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < B.length; j++) {
                result[i + j] += A[i] * B[j];
            }
        }
        return result;
    }

    public static double[] calculateBinomial(int n, double p) {
        double[] dist = new double[n + 1];
        for (int k = 0; k <= n; k++) {
            dist[k] = combinations(n, k) * Math.pow(p, k) * Math.pow(1 - p, n - k);
        }
        return dist;
    }

    private static double combinations(int n, int k) {
        if (k < 0 || k > n) return 0;
        if (k == 0 || k == n) return 1;
        if (k > n / 2) k = n - k;
        double res = 1;
        for (int i = 1; i <= k; i++) res = res * (n - i + 1) / i;
        return res;
    }
}