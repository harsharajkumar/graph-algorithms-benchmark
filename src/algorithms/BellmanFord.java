package algorithms;

import utils.CSVUtils;

import java.io.FileWriter;
import java.util.*;

public class BellmanFord implements WeightedGraphAlgorithm {

    @Override
    public String name() { return "BellmanFord"; }

    @Override
    public List<Integer> runWeighted(Map<Integer, Map<Integer, Integer>> graph, int start, boolean verbose, String verbosePath) {
        int n = graph.size();
        Map<Integer, Integer> dist = new HashMap<>();
        for (Integer node : graph.keySet()) dist.put(node, Integer.MAX_VALUE);
        dist.put(start, 0);

        FileWriter fw = null;
        List<Integer> sweepOrder = new ArrayList<>();
        try {
            if (verbose && verbosePath != null) {
                fw = new FileWriter(verbosePath);
                fw.append("Iteration,Action,U,V,OldDist,NewDist\n");
            }

            for (int i = 1; i <= n - 1; i++) {
                boolean changed = false;
                for (int u : graph.keySet()) {
                    sweepOrder.add(u);
                    for (Map.Entry<Integer, Integer> e : graph.get(u).entrySet()) {
                        int v = e.getKey(), w = e.getValue();
                        if (dist.get(u) != Integer.MAX_VALUE && dist.get(u) + w < dist.get(v)) {
                            int old = dist.get(v);
                            int nd = dist.get(u) + w;
                            dist.put(v, nd);
                            changed = true;
                            if (fw != null) CSVUtils.writeRow(fw, String.valueOf(i), "Relax",
                                    String.valueOf(u), String.valueOf(v), String.valueOf(old), String.valueOf(nd));
                        }
                    }
                }
                if (!changed) break;
            }

            // Optional negative cycle check (logged)
            for (int u : graph.keySet()) {
                for (Map.Entry<Integer, Integer> e : graph.get(u).entrySet()) {
                    int v = e.getKey(), w = e.getValue();
                    if (dist.get(u) != Integer.MAX_VALUE && dist.get(u) + w < dist.get(v)) {
                        if (fw != null) CSVUtils.writeRow(fw, "NC", "NegativeCycle", String.valueOf(u),
                                String.valueOf(v), String.valueOf(dist.get(v)), String.valueOf(dist.get(u) + w));
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            CSVUtils.closeQuietly(fw);
        }
        return sweepOrder;
    }
}
