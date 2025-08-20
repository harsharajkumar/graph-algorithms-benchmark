package utils;

import java.util.*;

public class WeightedGraphGenerator {

    /**
     * Generate a weighted directed/undirected graph with positive integer weights.
     * @param n nodes (0..n-1)
     * @param density edge probability
     * @param directed if false, add symmetric edges with same weight
     * @param seed rng seed
     * @param maxDegreeCap cap per node (<=0 to disable)
     */
    public static Map<Integer, Map<Integer, Integer>> generateWeightedGraph(int n, double density,
                                                                            boolean directed, long seed,
                                                                            int maxDegreeCap) {
        Random rnd = new Random(seed);
        Map<Integer, Map<Integer, Integer>> g = new HashMap<>();
        for (int i = 0; i < n; i++) g.put(i, new HashMap<>());

        for (int u = 0; u < n; u++) {
            Map<Integer, Integer> nbrs = g.get(u);
            for (int v = 0; v < n; v++) {
                if (u == v) continue;
                if (rnd.nextDouble() < density) {
                    if (maxDegreeCap > 0 && nbrs.size() >= maxDegreeCap) continue;
                    int w = 1 + rnd.nextInt(20); // weights 1..20
                    nbrs.put(v, w);
                    if (!directed) {
                        Map<Integer, Integer> back = g.get(v);
                        if (maxDegreeCap <= 0 || back.size() < maxDegreeCap) {
                            back.put(u, w);
                        }
                    }
                }
            }
        }
        return g;
    }
}
