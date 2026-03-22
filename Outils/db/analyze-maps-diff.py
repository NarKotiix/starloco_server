#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
analyze-maps-diff.py
Analyse les différences champ par champ entre maps_ancien.sql et maps_new.sql
pour comprendre ce qui change réellement.
"""
import re, os

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
INSERT_RE = re.compile(r'^INSERT INTO `maps` VALUES \((\d+),')

# Colonnes dans l'ordre du dump
COLS = ["id", "date", "width", "heigth", "places", "key", "mapData",
        "monsters", "capabilities", "mappos", "numgroup", "minSize",
        "fixSize", "maxSize", "forbidden", "sniffed"]

def parse_values(line):
    """Extrait la liste brute de valeurs depuis une ligne INSERT."""
    m = re.match(r"^INSERT INTO `maps` VALUES \((.+)\);?$", line)
    if not m:
        return None
    raw = m.group(1)
    # Parse simple : split sur virgule hors strings SQL
    vals = []
    current = ""
    in_str = False
    i = 0
    while i < len(raw):
        c = raw[i]
        if c == "'" and not in_str:
            in_str = True
            current += c
        elif c == "'" and in_str:
            # Check escaped ''
            if i + 1 < len(raw) and raw[i+1] == "'":
                current += "''"
                i += 2
                continue
            in_str = False
            current += c
        elif c == "," and not in_str:
            vals.append(current.strip())
            current = ""
        else:
            current += c
        i += 1
    if current.strip():
        vals.append(current.strip())
    return vals

def load_maps(path):
    maps = {}
    with open(path, "r", encoding="latin-1", errors="replace") as f:
        for line in f:
            m = INSERT_RE.match(line)
            if m:
                maps[int(m.group(1))] = line.rstrip("\n")
    return maps

old_maps = load_maps(os.path.join(SCRIPT_DIR, "maps_ancien.sql"))
new_maps = load_maps(os.path.join(SCRIPT_DIR, "maps_new.sql"))

common = set(old_maps.keys()) & set(new_maps.keys())
modified_ids = sorted(mid for mid in common if old_maps[mid] != new_maps[mid])

print(f"Maps modifiees : {len(modified_ids)}")
print()

# Analyser quels champs changent (sur les 20 premiers)
col_change_count = {c: 0 for c in COLS}
sample_analyzed = 0
sample_errors = 0

for mid in modified_ids:
    ov = parse_values(old_maps[mid])
    nv = parse_values(new_maps[mid])
    if ov is None or nv is None:
        sample_errors += 1
        continue
    if len(ov) != len(nv):
        print(f"  ID={mid}: different nb cols old={len(ov)} new={len(nv)}")
        continue
    for i, col in enumerate(COLS):
        if i < len(ov) and ov[i] != nv[i]:
            col_change_count[col] += 1
    sample_analyzed += 1

print(f"Analyses: {sample_analyzed}, erreurs parse: {sample_errors}")
print()
print("Colonnes modifiees (nb maps affectees) :")
for col, cnt in sorted(col_change_count.items(), key=lambda x: -x[1]):
    if cnt > 0:
        print(f"  {col:15s} : {cnt}")

# Afficher 3 exemples pour la colonne la plus changée
most_changed = max(col_change_count.items(), key=lambda x: x[1])[0]
col_idx = COLS.index(most_changed)
print(f"\nExemples pour colonne '{most_changed}' (3 premiers) :")
count = 0
for mid in modified_ids:
    ov = parse_values(old_maps[mid])
    nv = parse_values(new_maps[mid])
    if ov and nv and len(ov) == len(nv) and col_idx < len(ov) and ov[col_idx] != nv[col_idx]:
        print(f"  ID={mid}: OLD={ov[col_idx][:60]} | NEW={nv[col_idx][:60]}")
        count += 1
        if count >= 3:
            break

