package algorithms;

import utils.CSVUtils;

import java.io.FileWriter;
import java.util.*;

public class DFSAlgorithm implements UnweightedGraphAlgorithm {

    @Override
    public String name() { return "DFS"; }

    @Override
    public List<Integer> runUnweighted(Map<Integer, List<Integer>> graph, int start, boolean verbose, String verbosePath) {
        List<Integer> visitOrder = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Deque<Integer> stack = new ArrayDeque<>();

        FileWriter fw = null;
        try {
            if (verbose && verbosePath != null) {
                fw = new FileWriter(verbosePath);
                fw.append("Step,Action,Node,StackState,VisitedSet\n");
            }

            stack.push(start);
            int step = 0;
            if (fw != null) CSVUtils.writeRow(fw, String.valueOf(step++), "PushStart", String.valueOf(start),
                    stack.toString(), visited.toString());

            while (!stack.isEmpty()) {
                int u = stack.pop();
                if (!visited.contains(u)) {
                    visited.add(u);
                    visitOrder.add(u);
                    if (fw != null) CSVUtils.writeRow(fw, String.valueOf(step++), "Visit", String.valueOf(u),
                            stack.toString(), visited.toString());
                    // push neighbors in reverse to mimic recursive order
                    List<Integer> nbrs = new ArrayList<>(graph.getOrDefault(u, Collections.emptyList()));
                    Collections.reverse(nbrs);
                    for (int v : nbrs) {
                        if (!visited.contains(v)) {
                            stack.push(v);
                            if (fw != null) CSVUtils.writeRow(fw, String.valueOf(step++), "Push", String.valueOf(v),
                                    stack.toString(), visited.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            CSVUtils.closeQuietly(fw);
        }
        return visitOrder;
    }
}
