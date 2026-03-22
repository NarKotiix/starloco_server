#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
analyze-mapdata-format.py
Analyse le format exact des mapData dans les deux dumps
pour déterminer si le serveur Java peut lire les deux formats.
"""
import re, os

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
INSERT_RE = re.compile(r'^INSERT INTO `maps` VALUES \((\d+),')

COLS = ["id", "date", "width", "heigth", "places", "key", "mapData",
        "monsters", "capabilities", "mappos", "numgroup", "minSize",
        "fixSize", "maxSize", "forbidden", "sniffed"]

def parse_values(line):
    m = re.match(r"^INSERT INTO `maps` VALUES \((.+)\);?$", line)
    if not m:
        return None
    raw = m.group(1)
    vals, current, in_str = [], "", False
    i = 0
    while i < len(raw):
        c = raw[i]
        if c == "'" and not in_str:
            in_str = True; current += c
        elif c == "'" and in_str:
            if i + 1 < len(raw) and raw[i+1] == "'":
                current += "''"; i += 2; continue
            in_str = False; current += c
        elif c == "," and not in_str:
            vals.append(current.strip()); current = ""
        else:
            current += c
        i += 1
    if current.strip():
        vals.append(current.strip())
    return vals

def get_field(line, field):
    vals = parse_values(line)
    if not vals:
        return None
    idx = COLS.index(field)
    return vals[idx].strip("'") if idx < len(vals) else None

def count_digits(s):
    return sum(1 for c in s if c.isdigit())

def is_hex(s):
    return all(c in '0123456789abcdefABCDEF' for c in s) if s else False

# Charger quelques maps de chaque fichier pour analyse
sample_ids = [5, 6, 7, 100, 500, 1000, 2000, 5000]

print("=== Analyse format mapData ===\n")

def load_sample(path, ids):
    result = {}
    with open(path, "r", encoding="latin-1", errors="replace") as f:
        for line in f:
            m = INSERT_RE.match(line)
            if m:
                mid = int(m.group(1))
                if mid in ids:
                    result[mid] = line.rstrip("\n")
    return result

old_sample = load_sample(os.path.join(SCRIPT_DIR, "maps_ancien.sql"), set(sample_ids))
new_sample = load_sample(os.path.join(SCRIPT_DIR, "maps_new.sql"), set(sample_ids))

for mid in sample_ids:
    if mid not in old_sample or mid not in new_sample:
        continue

    old_md = get_field(old_sample[mid], "mapData")
    new_md = get_field(new_sample[mid], "mapData")
    old_key = get_field(old_sample[mid], "key")
    new_key = get_field(new_sample[mid], "key")
    w = int(get_field(old_sample[mid], "width"))
    h = int(get_field(old_sample[mid], "heigth"))

    # Nb cellules théoriques Dofus 1.29
    nb_cells = h * (2 * w - 1) - (w - 1)

    print(f"--- Map ID={mid} (w={w}, h={h}, cells_théoriques={nb_cells}) ---")
    print(f"  OLD mapData: len={len(old_md)}, digits={count_digits(old_md)}, "
          f"hex_only={is_hex(old_md)}, len%10={len(old_md)%10}, len/cells={len(old_md)/nb_cells:.1f}")
    print(f"  NEW mapData: len={len(new_md)}, digits={count_digits(new_md)}, "
          f"hex_only={is_hex(new_md)}, len%10={len(new_md)%10}, len/cells={len(new_md)/nb_cells:.1f}")
    print(f"  OLD key: len={len(old_key)}, empty={old_key==''}")
    print(f"  NEW key: len={len(new_key)}, empty={new_key==''}")
    print(f"  key changed: {old_key != new_key}")
    # Premier et dernier chars
    print(f"  OLD mapData[:20]={old_md[:20]!r}")
    print(f"  NEW mapData[:20]={new_md[:20]!r}")
    print()

# Statistiques globales sur les formats
print("=== Stats globales (1000 maps) ===\n")
old_crypted = 0; old_plain = 0; old_other = 0
new_crypted = 0; new_plain = 0; new_other = 0
count = 0

with open(os.path.join(SCRIPT_DIR, "maps_ancien.sql"), "r", encoding="latin-1", errors="replace") as f:
    for line in f:
        m = INSERT_RE.match(line)
        if m:
            md = get_field(line.rstrip(), "mapData")
            if md:
                nd = count_digits(md)
                if nd > 1000:
                    old_crypted += 1
                elif len(md) % 10 == 0:
                    old_plain += 1
                else:
                    old_other += 1
            count += 1
            if count >= 1000: break

count = 0
with open(os.path.join(SCRIPT_DIR, "maps_new.sql"), "r", encoding="latin-1", errors="replace") as f:
    for line in f:
        m = INSERT_RE.match(line)
        if m:
            md = get_field(line.rstrip(), "mapData")
            if md:
                nd = count_digits(md)
                if nd > 1000:
                    new_crypted += 1
                elif len(md) % 10 == 0:
                    new_plain += 1
                else:
                    new_other += 1
            count += 1
            if count >= 1000: break

print(f"  OLD (1000 maps) : crypted={old_crypted}, plain(len%10==0)={old_plain}, other={old_other}")
print(f"  NEW (1000 maps) : crypted={new_crypted}, plain(len%10==0)={new_plain}, other={new_other}")

