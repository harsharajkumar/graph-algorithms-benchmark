package utils;

import java.util.*;

public class GraphGenerator {

    /**
     * Generate an unweighted directed/undirected random graph.
     * @param n nodes (0..n-1)
     * @param density probability of edge existence (0..1)
     * @param directed if false, add symmetric edges
     * @param seed rng seed
     * @param maxDegreeCap optional cap per node to bound edges (use <=0 to disable)
     */
    public static Map<Integer, List<Integer>> generateDenseGraph(int n, double density,
                                                                 boolean directed, long seed,
                                                                 int maxDegreeCap) {
        Random rnd = new Random(seed);
        Map<Integer, List<Integer>> g = new HashMap<>();
        for (int i = 0; i < n; i++) g.put(i, new ArrayList<>());

        for (int u = 0; u < n; u++) {
            List<Integer> neighbors = g.get(u);
            for (int v = 0; v < n; v++) {
                if (u == v) continue;
                if (rnd.nextDouble() < density) {
                    if (maxDegreeCap > 0 && neighbors.size() >= maxDegreeCap) continue;
                    neighbors.add(v);
                    if (!directed) {
                        List<Integer> back = g.get(v);
                        if (maxDegreeCap <= 0 || back.size() < maxDegreeCap) {
                            back.add(u);
                        }
                    }
                }
            }
        }
        return g;
    }
}
