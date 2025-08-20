package algorithms;

import utils.CSVUtils;

import java.io.FileWriter;
import java.util.*;

public class BipartiteCheck implements UnweightedGraphAlgorithm {

    @Override
    public String name() { return "BipartiteCheck"; }

    @Override
    public List<Integer> runUnweighted(Map<Integer, List<Integer>> graph, int start, boolean verbose, String verbosePath) {
        int n = graph.size();
        int[] color = new int[n];
        Arrays.fill(color, -1);
        List<Integer> visit = new ArrayList<>();
        FileWriter fw = null;

        try {
            if (verbose && verbosePath != null) {
                fw = new FileWriter(verbosePath);
                fw.append("Step,Action,Node,Neighbor,ColorNode,ColorNeighbor\n");
            }
            int step = 0;
            for (int s = 0; s < n; s++) {
                if (color[s] != -1) continue;
                Deque<Integer> q = new ArrayDeque<>();
                q.add(s);
                color[s] = 0;
                if (fw != null) CSVUtils.writeRow(fw, String.valueOf(step++), "ColorStart", String.valueOf(s),
                        "", String.valueOf(color[s]), "");

                while (!q.isEmpty()) {
                    int u = q.poll();
                    visit.add(u);
                    for (int v : graph.getOrDefault(u, Collections.emptyList())) {
                        if (color[v] == -1) {
                            color[v] = 1 - color[u];
                            q.add(v);
                            if (fw != null) CSVUtils.writeRow(fw, String.valueOf(step++), "ColorAssign",
                                    String.valueOf(u), String.valueOf(v), String.valueOf(color[u]), String.valueOf(color[v]));
                        } else if (color[v] == color[u]) {
                            if (fw != null) CSVUtils.writeRow(fw, String.valueOf(step++), "Conflict",
                                    String.valueOf(u), String.valueOf(v), String.valueOf(color[u]), String.valueOf(color[v]));
                            // not bipartite, but we still return collected visit order
                            return visit;
                        }
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            CSVUtils.closeQuietly(fw);
        }
        return visit;
    }
}
