package algorithms;

import utils.CSVUtils;

import java.io.FileWriter;
import java.util.*;

public class AStar implements WeightedGraphAlgorithm {

    @Override
    public String name() { return "AStar"; }

    @Override
    public List<Integer> runWeighted(Map<Integer, Map<Integer, Integer>> graph, int start, boolean verbose, String verbosePath) {
        // For benchmark, we don’t have a specific goal; we’ll pick goal = start+1 mod n.
        int n = graph.size();
        int goal = (start + 1) % n;

        Map<Integer, Integer> gScore = new HashMap<>();
        Map<Integer, Integer> fScore = new HashMap<>();
        for (Integer node : graph.keySet()) {
            gScore.put(node, Integer.MAX_VALUE);
            fScore.put(node, Integer.MAX_VALUE);
        }
        gScore.put(start, 0);
        fScore.put(start, heuristic(start, goal));

        PriorityQueue<int[]> open = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        open.offer(new int[]{start, fScore.get(start)});

        Set<Integer> closed = new HashSet<>();
        List<Integer> order = new ArrayList<>();

        FileWriter fw = null;
        try {
            if (verbose && verbosePath != null) {
                fw = new FileWriter(verbosePath);
                fw.append("Step,Action,Node,Neighbor,OldG,NewG,H,NewF,OpenSize\n");
            }
            int step = 0;

            while (!open.isEmpty()) {
                int[] cur = open.poll();
                int u = cur[0];
                if (closed.contains(u)) continue;
                closed.add(u);
                order.add(u);

                if (fw != null) CSVUtils.writeRow(fw, String.valueOf(step++), "Pop", String.valueOf(u),
                        "", "", "", "", "", String.valueOf(open.size()));

                if (u == goal) {
                    if (fw != null) CSVUtils.writeRow(fw, String.valueOf(step++), "ReachedGoal", String.valueOf(u),
                            "", "", "", "", "", String.valueOf(open.size()));
                    break;
                }

                for (Map.Entry<Integer, Integer> e : graph.getOrDefault(u, Collections.emptyMap()).entrySet()) {
                    int v = e.getKey();
                    int w = e.getValue();
                    int tentativeG = (gScore.get(u) == Integer.MAX_VALUE) ? Integer.MAX_VALUE : gScore.get(u) + w;

                    if (tentativeG < gScore.get(v)) {
                        int oldG = gScore.get(v);
                        gScore.put(v, tentativeG);
                        int h = heuristic(v, goal);
                        int nf = tentativeG + h;
                        fScore.put(v, nf);
                        open.offer(new int[]{v, nf});
                        if (fw != null) CSVUtils.writeRow(fw, String.valueOf(step++), "Relax",
                                String.valueOf(u), String.valueOf(v), String.valueOf(oldG),
                                String.valueOf(tentativeG), String.valueOf(h), String.valueOf(nf),
                                String.valueOf(open.size()));
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            CSVUtils.closeQuietly(fw);
        }

        return order;
    }

    // simple admissible heuristic over integer-labeled nodes
    private int heuristic(int a, int b) {
        return Math.abs(a - b);
    }
}
