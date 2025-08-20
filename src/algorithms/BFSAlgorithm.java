package algorithms;

import utils.CSVUtils;

import java.io.FileWriter;
import java.util.*;

public class BFSAlgorithm implements UnweightedGraphAlgorithm {

    @Override
    public String name() { return "BFS"; }

    @Override
    public List<Integer> runUnweighted(Map<Integer, List<Integer>> graph, int start, boolean verbose, String verbosePath) {
        List<Integer> visitOrder = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Deque<Integer> q = new ArrayDeque<>();

        FileWriter fw = null;
        try {
            if (verbose && verbosePath != null) {
                fw = new FileWriter(verbosePath);
                fw.append("Step,Action,Node,QueueState,VisitedSet\n");
            }

            q.add(start);
            visited.add(start);
            if (fw != null) CSVUtils.writeRow(fw, "0", "EnqueueStart", String.valueOf(start),
                    q.toString(), visited.toString());

            int step = 1;
            while (!q.isEmpty()) {
                int u = q.poll();
                visitOrder.add(u);
                if (fw != null) CSVUtils.writeRow(fw, String.valueOf(step++), "Dequeue", String.valueOf(u),
                        q.toString(), visited.toString());

                for (int v : graph.getOrDefault(u, Collections.emptyList())) {
                    if (!visited.contains(v)) {
                        visited.add(v);
                        q.add(v);
                        if (fw != null) CSVUtils.writeRow(fw, String.valueOf(step++), "Enqueue", String.valueOf(v),
                                q.toString(), visited.toString());
                    }
                }
            }
        } catch (Exception e) {
            // swallow to keep benchmark running
        } finally {
            CSVUtils.closeQuietly(fw);
        }
        return visitOrder;
    }
}
