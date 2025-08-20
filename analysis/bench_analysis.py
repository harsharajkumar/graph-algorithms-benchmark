#!/usr/bin/env python3
"""
FAANG-style analysis for graph benchmark CSVs.

Inputs:
  - ../data/all_graph_algorithms_verbose.csv  (master CSV from Java)
  - ../results/*.csv                          (per-run verbose logs; used to extract MaxFlow's flow)

Outputs (saved in analysis/figures/):
  - runtime_vs_nodes.png
  - runtime_vs_density.png
  - memory_vs_runtime.png
  - maxflow_flow_vs_runtime.png (if MaxFlow runs exist)

Usage:
  cd analysis
  python bench_analysis.py
"""
import os
import math
import glob
import csv
from pathlib import Path

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt


# ---------- configuration ----------
MASTER_CSV = Path(__file__).resolve().parent.parent / "data" / "all_graph_algorithms_verbose.csv"
RESULTS_DIR = Path(__file__).resolve().parent.parent / "results"
FIG_DIR = Path(__file__).resolve().parent / "figures"
FIG_DIR.mkdir(parents=True, exist_ok=True)


def load_master(master_csv: Path) -> pd.DataFrame:
    df = pd.read_csv(master_csv)
    # normalize dtypes
    df["Algorithm"] = df["Algorithm"].astype(str)
    df["Directed"] = df["Directed"].astype(str).map({"true": True, "false": False, "True": True, "False": False})
    for col in ["Nodes", "Edges", "Seed", "Run", "StartNode"]:
        df[col] = pd.to_numeric(df[col], errors="coerce")
    for col in ["RuntimeMs", "MemoryBeforeKB", "MemoryAfterKB", "MemoryDeltaKB", "Visited"]:
        df[col] = pd.to_numeric(df[col], errors="coerce")
    # derive density ≈ E / (V*(V-1)) for directed else 2E / (V*(V-1))
    V = df["Nodes"].astype(float)
    E = df["Edges"].astype(float)
    denom = V * (V - 1.0)
    df["Density"] = np.where(df["Directed"], E / denom, (2.0 * E) / denom)
    df["AvgDegree"] = np.where(df["Directed"], E / V, (2.0 * E) / V)
    return df


def _find_verbose_file(algo: str, n: int, seed: int, run: int, weighted: bool) -> str | None:
    """We don’t have density in the master, so match with wildcard on d*."""
    kind = "weighted" if weighted else "unweighted"
    # file naming in the Java code:
    #   ../results/{Algo}_{kind}_n{n}_d{density}_seed{seed}_run{r}.csv
    # We’ll glob for density.
    pattern = str(RESULTS_DIR / f"{algo}_{kind}_n{n}_d*_seed{seed}_run{run}.csv")
    matches = glob.glob(pattern)
    return matches[0] if matches else None


def extract_maxflow_total_flow(verbose_file: str) -> float | None:
    """
    MaxFlow verbose has header: Augment,Path,PathFlow,TotalFlow
    Return the last TotalFlow value.
    """
    try:
        last_total = None
        with open(verbose_file, newline="") as f:
            reader = csv.DictReader(f)
            for row in reader:
                if "TotalFlow" in row and row["TotalFlow"] != "":
                    last_total = float(row["TotalFlow"])
        return last_total
    except Exception:
        return None


def add_maxflow_flow(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()
    df["MaxFlowValue"] = np.nan
    mask = df["Algorithm"].str.contains("MaxFlow", case=False, na=False)
    if not mask.any():
        return df

    # Try to locate per-run verbose files and extract final flow
    for idx, row in df[mask].iterrows():
        algo = row["Algorithm"]
        n = int(row["Nodes"])
        seed = int(row["Seed"])
        run = int(row["Run"])
        weighted = True  # MaxFlow is in weighted list

        verbose = _find_verbose_file(algo, n, seed, run, weighted=weighted)
        if verbose:
            flow = extract_maxflow_total_flow(verbose)
            if flow is not None:
                df.at[idx, "MaxFlowValue"] = flow
    return df


def plot_runtime_vs_nodes(df: pd.DataFrame):
    # aggregate by (Algorithm, Nodes): median runtime with IQR for robustness
    grouped = df.groupby(["Algorithm", "Nodes"])["RuntimeMs"].agg(
        median="median", q1=lambda s: s.quantile(0.25), q3=lambda s: s.quantile(0.75)
    ).reset_index()

    plt.figure(figsize=(9, 6))
    for algo, sub in grouped.groupby("Algorithm"):
        sub = sub.sort_values("Nodes")
        plt.plot(sub["Nodes"], sub["median"], marker="o", label=algo)
        # error bars as vertical lines (IQR)
        for _, r in sub.iterrows():
            plt.vlines(r["Nodes"], r["q1"], r["q3"], alpha=0.25)
    plt.xlabel("Nodes (V)")
    plt.ylabel("Runtime (ms) — median with IQR")
    plt.title("Runtime vs. Number of Nodes")
    plt.legend()
    out = FIG_DIR / "runtime_vs_nodes.png"
    plt.tight_layout()
    plt.savefig(out, dpi=160)
    plt.close()


def plot_runtime_vs_density(df: pd.DataFrame):
    grouped = df.groupby(["Algorithm", "Density"])["RuntimeMs"].agg(
        median="median"
    ).reset_index()
    plt.figure(figsize=(9, 6))
    for algo, sub in grouped.groupby("Algorithm"):
        sub = sub.sort_values("Density")
        plt.plot(sub["Density"], sub["median"], marker="o", label=algo)
    plt.xlabel("Density (≈ E / (V*(V-1)) or 2E / (V*(V-1)))")
    plt.ylabel("Runtime (ms) — median")
    plt.title("Runtime vs. Density")
    plt.legend()
    out = FIG_DIR / "runtime_vs_density.png"
    plt.tight_layout()
    plt.savefig(out, dpi=160)
    plt.close()


def plot_memory_vs_runtime(df: pd.DataFrame):
    plt.figure(figsize=(9, 6))
    for algo, sub in df.groupby("Algorithm"):
        plt.scatter(sub["RuntimeMs"], sub["MemoryDeltaKB"], alpha=0.5, label=algo)
    plt.xlabel("Runtime (ms)")
    plt.ylabel("Memory Delta (KB)")
    plt.title("Memory vs. Runtime")
    plt.legend()
    out = FIG_DIR / "memory_vs_runtime.png"
    plt.tight_layout()
    plt.savefig(out, dpi=160)
    plt.close()


def plot_maxflow_flow_vs_runtime(df: pd.DataFrame):
    sub = df[df["Algorithm"].str.contains("MaxFlow", case=False, na=False)].dropna(subset=["MaxFlowValue"])
    if sub.empty:
        return
    plt.figure(figsize=(9, 6))
    plt.scatter(sub["MaxFlowValue"], sub["RuntimeMs"], alpha=0.6)
    plt.xlabel("Final Max Flow Value")
    plt.ylabel("Runtime (ms)")
    plt.title("MaxFlow: Flow Value vs Runtime")
    out = FIG_DIR / "maxflow_flow_vs_runtime.png"
    plt.tight_layout()
    plt.savefig(out, dpi=160)
    plt.close()


def main():
    if not MASTER_CSV.exists():
        raise SystemExit(f"Master CSV not found: {MASTER_CSV}\nRun the Java benchmark first.")
    df = load_master(MASTER_CSV)
    df = add_maxflow_flow(df)

    plot_runtime_vs_nodes(df)
    plot_runtime_vs_density(df)
    plot_memory_vs_runtime(df)
    plot_maxflow_flow_vs_runtime(df)

    print(f"✅ Figures saved to: {FIG_DIR}")


if __name__ == "__main__":
    main()
