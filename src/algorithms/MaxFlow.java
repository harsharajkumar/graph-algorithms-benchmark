package algorithms;

import utils.CSVUtils;

import java.io.FileWriter;
import java.util.*;

public class MaxFlow implements WeightedGraphAlgorithm {

    @Override
    public String name() { return "MaxFlow_EdmondsKarp"; }

    @Override
    public List<Integer> runWeighted(Map<Integer, Map<Integer, Integer>> graph, int start, boolean verbose, String verbosePath) {
        // Interpret start as source; sink = (start+1) % n
        int n = graph.size();
        int sink = (start + 1) % n;

        int[][] cap = new int[n][n];
        for (int u : graph.keySet()) {
            for (Map.Entry<Integer, Integer> e : graph.get(u).entrySet()) {
                cap[u][e.getKey()] += Math.max(0, e.getValue());
            }
        }

        int flow = 0;
        int[] parent = new int[n];

        FileWriter fw = null;
        List<Integer> visitOrder = new ArrayList<>();
        try {
            if (verbose && verbosePath != null) {
                fw = new FileWriter(verbosePath);
                fw.append("Augment,Path,PathFlow,TotalFlow\n");
            }

            while (bfs(cap, start, sink, parent)) {
                int pathFlow = Integer.MAX_VALUE;
                List<Integer> path = new ArrayList<>();
                for (int v = sink; v != start; v = parent[v]) {
                    int u = parent[v];
                    path.add(v);
                    pathFlow = Math.min(pathFlow, cap[u][v]);
                }
                path.add(start);
                Collections.reverse(path);
                visitOrder.addAll(path);

                for (int v = sink; v != start; v = parent[v]) {
                    int u = parent[v];
                    cap[u][v] -= pathFlow;
                    cap[v][u] += pathFlow;
                }
                flow += pathFlow;

                if (fw != null) CSVUtils.writeRow(fw, String.valueOf(flow), path.toString(),
                        String.valueOf(pathFlow), String.valueOf(flow));
            }
        } catch (Exception e) {
        } finally {
            CSVUtils.closeQuietly(fw);
        }

        return visitOrder;
    }

    private boolean bfs(int[][] cap, int s, int t, int[] parent) {
        Arrays.fill(parent, -1);
        parent[s] = -2;
        Deque<int[]> q = new ArrayDeque<>();
        q.add(new int[]{s, Integer.MAX_VALUE});

        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int u = cur[0];
            for (int v = 0; v < cap.length; v++) {
                if (parent[v] == -1 && cap[u][v] > 0) {
                    parent[v] = u;
                    if (v == t) return true;
                    q.add(new int[]{v, Math.min(cur[1], cap[u][v])});
                }
            }
        }
        return false;
    }
}
