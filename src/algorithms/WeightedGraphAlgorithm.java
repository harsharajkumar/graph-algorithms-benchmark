package algorithms;

import java.util.List;
import java.util.Map;

public interface WeightedGraphAlgorithm {
    String name();

    /**
     * @param graph weighted adjacency map: u -> (v -> weight)
     * @param start start node (0..n-1). For MaxFlow we’ll assume start is the source and sink=start+1 (unless you change it)
     * @param verbose if true, write step-by-step CSV to verbosePath
     * @param verbosePath file path for per-run CSV (may be null when verbose=false)
     * @return list of processed/visited nodes in order (algorithm-specific)
     */
    List<Integer> runWeighted(Map<Integer, Map<Integer, Integer>> graph, int start, boolean verbose, String verbosePath);
}
