package algorithms;

import java.util.List;
import java.util.Map;

public interface UnweightedGraphAlgorithm {
    String name();

    /**
     * @param graph adjacency list: node -> list of neighbors
     * @param start start node (0..n-1)
     * @param verbose if true, write step-by-step CSV to verbosePath
     * @param verbosePath file path for per-run CSV (may be null when verbose=false)
     * @return list of visited nodes in order
     */
    List<Integer> runUnweighted(Map<Integer, List<Integer>> graph, int start, boolean verbose, String verbosePath);
}
