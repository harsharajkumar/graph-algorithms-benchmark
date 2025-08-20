package algorithms;

import utils.CSVUtils;

import java.io.FileWriter;
import java.util.*;

public class Dijkstra implements WeightedGraphAlgorithm {

    @Override
    public String name() { return "Dijkstra"; }

    @Override
    public List<Integer> runWeighted(Map<Integer, Map<Integer, Integer>> graph, int start, boolean verbose, String verbosePath) {
        int n = graph.size();
        Map<Integer, Integer> dist = new HashMap<>();
        for (Integer node : graph.keySet()) dist.put(node, Integer.MAX_VALUE);
        dist.put(start, 0);

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        pq.offer(new int[]{start, 0});

        Set<Integer> settled = new HashSet<>();
        List<Integer> order = new ArrayList<>();

        FileWriter fw = null;
        try {
            if (verbose && verbosePath != null) {
                fw = new FileWriter(verbosePath);
                fw.append("Step,Action,Node,Neighbor,OldDist,NewDist,QueueSize\n");
            }
            int step = 0;

            while (!pq.isEmpty()) {
                int[] cur = pq.poll();
                int u = cur[0];
                if (settled.contains(u)) continue;
                settled.add(u);
                order.add(u);
                if (fw != null) CSVUtils.writeRow(fw, String.valueOf(step++), "Settle", String.valueOf(u),
                        "", "", "", String.valueOf(pq.size()));

                for (Map.Entry<Integer, Integer> e : graph.getOrDefault(u, Collections.emptyMap()).entrySet()) {
                    int v = e.getKey();
                    int w = e.getValue();
                    if (dist.get(u) != Integer.MAX_VALUE && dist.get(u) + w < dist.get(v)) {
                        int old = dist.get(v);
                        int nd = dist.get(u) + w;
                        dist.put(v, nd);
                        pq.offer(new int[]{v, nd});
                        if (fw != null) CSVUtils.writeRow(fw, String.valueOf(step++), "Relax", String.valueOf(u),
                                String.valueOf(v), String.valueOf(old), String.valueOf(nd), String.valueOf(pq.size()));
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            CSVUtils.closeQuietly(fw);
        }
        return order;
    }
}
