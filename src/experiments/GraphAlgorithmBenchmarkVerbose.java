package experiments;

import algorithms.*;
import utils.GraphGenerator;
import utils.WeightedGraphGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GraphAlgorithmBenchmarkVerbose {

    public static void main(String[] args) throws IOException {
        // ----- Benchmark settings (tune here) -----
        boolean[] directedOptions = {true, false};
        int[] nodeSizes = {100, 500};           // keep reasonable; increase after testing
        double[] densities = {0.01, 0.05, 0.1}; // sparse -> denser
        long[] seeds = {42, 1337, 2024};
        int runsPerGraph = 2;
        boolean measureMemory = true;
        boolean verbose = true;

        // ----- folders -----
        new File("../data").mkdirs();    // from src/ working dir, write to ../data
        new File("../results").mkdirs();

        // ----- combined CSV -----
        FileWriter csv = new FileWriter("../data/all_graph_algorithms_verbose.csv");
        csv.append("Algorithm,Directed,Nodes,Edges,Seed,Run,StartNode,RuntimeMs,MemoryBeforeKB,MemoryAfterKB,MemoryDeltaKB,Visited\n");

        // unweighted algos
        List<UnweightedGraphAlgorithm> unweightedAlgos = Arrays.<UnweightedGraphAlgorithm>asList(
                new BFSAlgorithm(),
                new DFSAlgorithm(),
                new BipartiteCheck()
        );

        // weighted algos
        List<WeightedGraphAlgorithm> weightedAlgos = Arrays.<WeightedGraphAlgorithm>asList(
                new Dijkstra(),
                new BellmanFord(),
                new AStar(),
                new MaxFlow()
        );

        Random startPicker = new Random(1234);

        for (boolean directed : directedOptions) {
            for (int n : nodeSizes) {
                for (double density : densities) {
                    for (long seed : seeds) {
                        // generate graphs
                        Map<Integer, List<Integer>> unweightedGraph =
                                GraphGenerator.generateDenseGraph(n, density, directed, seed, 1000);
                        Map<Integer, Map<Integer, Integer>> weightedGraph =
                                WeightedGraphGenerator.generateWeightedGraph(n, density, directed, seed, 1000);

                        long edgesUnweighted = unweightedGraph.values().stream().mapToLong(List::size).sum();
                        long edgesWeighted = weightedGraph.values().stream().mapToLong(m -> m.values().size()).sum();

                        // ----- unweighted -----
                        for (UnweightedGraphAlgorithm algo : unweightedAlgos) {
                            for (int r = 1; r <= runsPerGraph; r++) {
                                int start = startPicker.nextInt(n);
                                if (measureMemory) System.gc();
                                Runtime rt = Runtime.getRuntime();
                                long memBefore = measureMemory ? (rt.totalMemory() - rt.freeMemory()) / 1024 : 0;
                                String verbosePath = verbose
                                        ? String.format("../results/%s_unweighted_n%d_d%.3f_seed%d_run%d.csv",
                                        algo.name(), n, density, seed, r)
                                        : null;

                                long t0 = System.nanoTime();
                                List<Integer> result = algo.runUnweighted(unweightedGraph, start, verbose, verbosePath);
                                long t1 = System.nanoTime();

                                long memAfter = measureMemory ? (rt.totalMemory() - rt.freeMemory()) / 1024 : 0;
                                double runtimeMs = (t1 - t0) / 1e6;

                                csv.append(String.join(",",
                                        algo.name(),
                                        String.valueOf(directed),
                                        String.valueOf(n),
                                        String.valueOf(edgesUnweighted),
                                        String.valueOf(seed),
                                        String.valueOf(r),
                                        String.valueOf(start),
                                        String.valueOf(runtimeMs),
                                        String.valueOf(memBefore),
                                        String.valueOf(memAfter),
                                        String.valueOf(memAfter - memBefore),
                                        String.valueOf(result.size())
                                )).append("\n");
                            }
                        }

                        // ----- weighted -----
                        for (WeightedGraphAlgorithm algo : weightedAlgos) {
                            for (int r = 1; r <= runsPerGraph; r++) {
                                int start = startPicker.nextInt(n);
                                if (measureMemory) System.gc();
                                Runtime rt = Runtime.getRuntime();
                                long memBefore = measureMemory ? (rt.totalMemory() - rt.freeMemory()) / 1024 : 0;
                                String verbosePath = verbose
                                        ? String.format("../results/%s_weighted_n%d_d%.3f_seed%d_run%d.csv",
                                        algo.name(), n, density, seed, r)
                                        : null;

                                long t0 = System.nanoTime();
                                List<Integer> result = algo.runWeighted(weightedGraph, start, verbose, verbosePath);
                                long t1 = System.nanoTime();

                                long memAfter = measureMemory ? (rt.totalMemory() - rt.freeMemory()) / 1024 : 0;
                                double runtimeMs = (t1 - t0) / 1e6;

                                csv.append(String.join(",",
                                        algo.name(),
                                        String.valueOf(directed),
                                        String.valueOf(n),
                                        String.valueOf(edgesWeighted),
                                        String.valueOf(seed),
                                        String.valueOf(r),
                                        String.valueOf(start),
                                        String.valueOf(runtimeMs),
                                        String.valueOf(memBefore),
                                        String.valueOf(memAfter),
                                        String.valueOf(memAfter - memBefore),
                                        String.valueOf(result.size())
                                )).append("\n");
                            }
                        }
                    }
                }
            }
        }

        csv.close();
        System.out.println("✅ Verbose FAANG-style benchmark complete. CSV saved in data/ and per-run logs in results/");
    }
}
