#!/usr/bin/env python3
"""
Interactive dashboard for the benchmark.

Run:
  cd analysis
  streamlit run streamlit_app.py
"""
import os
import glob
import csv
from pathlib import Path

import numpy as np
import pandas as pd
import plotly.express as px
import streamlit as st

ROOT = Path(__file__).resolve().parent.parent
MASTER_DEFAULT = ROOT / "data" / "all_graph_algorithms_verbose.csv"
RESULTS_DIR = ROOT / "results"


@st.cache_data
def load_master(path: Path) -> pd.DataFrame:
    df = pd.read_csv(path)
    df["Directed"] = df["Directed"].astype(str).map({"true": True, "false": False, "True": True, "False": False})
    for col in ["Nodes", "Edges", "Seed", "Run", "StartNode", "RuntimeMs", "MemoryBeforeKB", "MemoryAfterKB", "MemoryDeltaKB", "Visited"]:
        if col in df.columns:
            df[col] = pd.to_numeric(df[col], errors="coerce")
    V = df["Nodes"].astype(float)
    E = df["Edges"].astype(float)
    denom = V * (V - 1.0)
    df["Density"] = np.where(df["Directed"], E / denom, (2.0 * E) / denom)
    df["AvgDegree"] = np.where(df["Directed"], E / V, (2.0 * E) / V)
    return df


def find_verbose_file(algo: str, n: int, seed: int, run: int, weighted: bool) -> str | None:
    kind = "weighted" if weighted else "unweighted"
    pattern = str(RESULTS_DIR / f"{algo}_{kind}_n{n}_d*_seed{seed}_run{run}.csv")
    matches = glob.glob(pattern)
    return matches[0] if matches else None


def extract_maxflow(verbose_file: str) -> float | None:
    try:
        total = None
        with open(verbose_file, newline="") as f:
            for row in csv.DictReader(f):
                if "TotalFlow" in row and row["TotalFlow"] != "":
                    total = float(row["TotalFlow"])
        return total
    except Exception:
        return None


st.set_page_config(page_title="Graph Bench Dashboard", layout="wide")
st.title("Graph Algorithms – FAANG-style Benchmark Dashboard")

st.sidebar.header("Load Data")
uploaded = st.sidebar.file_uploader("Upload master CSV (optional)", type=["csv"])
if uploaded:
    df = load_master(Path(uploaded.name))  # name only for cache key
    df = pd.read_csv(uploaded)
    # redo normalization
    df["Directed"] = df["Directed"].astype(str).map({"true": True, "false": False, "True": True, "False": False})
    for col in ["Nodes", "Edges", "Seed", "Run", "StartNode", "RuntimeMs", "MemoryBeforeKB", "MemoryAfterKB", "MemoryDeltaKB", "Visited"]:
        if col in df.columns:
            df[col] = pd.to_numeric(df[col], errors="coerce")
    V = df["Nodes"].astype(float)
    E = df["Edges"].astype(float)
    denom = V * (V - 1.0)
    df["Density"] = np.where(df["Directed"], E / denom, (2.0 * E) / denom)
    df["AvgDegree"] = np.where(df["Directed"], E / V, (2.0 * E) / V)
else:
    if not MASTER_DEFAULT.exists():
        st.warning("No default master CSV found. Upload a CSV in the sidebar.")
        st.stop()
    df = load_master(MASTER_DEFAULT)

st.markdown("#### Filters")
algos = sorted(df["Algorithm"].unique().tolist())
selected_algos = st.multiselect("Algorithms", algos, default=algos)
df = df[df["Algorithm"].isin(selected_algos)]

col1, col2, col3 = st.columns(3)
with col1:
    nodes = st.slider("Nodes", int(df["Nodes"].min()), int(df["Nodes"].max()), (int(df["Nodes"].min()), int(df["Nodes"].max())))
with col2:
    dens = st.slider("Density", float(df["Density"].min()), float(df["Density"].max()), (float(df["Density"].min()), float(df["Density"].max())))
with col3:
    dir_choice = st.selectbox("Directed filter", ["All", "Directed", "Undirected"], index=0)

mask = (df["Nodes"].between(nodes[0], nodes[1])) & (df["Density"].between(dens[0], dens[1]))
if dir_choice != "All":
    mask &= (df["Directed"] == (dir_choice == "Directed"))
dfv = df[mask].copy()

st.markdown("### Runtime vs Nodes")
fig1 = px.line(dfv.groupby(["Algorithm", "Nodes"])["RuntimeMs"].median().reset_index(),
               x="Nodes", y="RuntimeMs", color="Algorithm", markers=True)
st.plotly_chart(fig1, use_container_width=True)

st.markdown("### Runtime vs Density")
fig2 = px.line(dfv.groupby(["Algorithm", "Density"])["RuntimeMs"].median().reset_index(),
               x="Density", y="RuntimeMs", color="Algorithm", markers=True)
st.plotly_chart(fig2, use_container_width=True)

st.markdown("### Memory vs Runtime")
fig3 = px.scatter(dfv, x="RuntimeMs", y="MemoryDeltaKB", color="Algorithm", opacity=0.6)
st.plotly_chart(fig3, use_container_width=True)

# MaxFlow: enrich with final flow from per-run logs (if files present)
if any(dfv["Algorithm"].str.contains("MaxFlow", case=False, na=False)):
    st.markdown("### MaxFlow – Flow Value vs Runtime")
    dfm = dfv[dfv["Algorithm"].str.contains("MaxFlow", case=False, na=False)].copy()
    flows = []
    for _, r in dfm.iterrows():
        vf = find_verbose_file(r["Algorithm"], int(r["Nodes"]), int(r["Seed"]), int(r["Run"]), weighted=True)
        if vf:
            val = extract_maxflow(vf)
        else:
            val = None
        flows.append(val)
    dfm["FinalFlow"] = flows
    dfm = dfm.dropna(subset=["FinalFlow"])
    if dfm.empty:
        st.info("No MaxFlow verbose files found, or flow could not be parsed.")
    else:
        fig4 = px.scatter(dfm, x="FinalFlow", y="RuntimeMs", color="Nodes", title="MaxFlow: Flow vs Runtime")
        st.plotly_chart(fig4, use_container_width=True)

st.caption("Tip: Use the filters to isolate specific regimes and compare empirical curves to expected complexities.")
