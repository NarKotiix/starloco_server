#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
generate-maps-diff-inserts.py
Génère des fichiers SQL d'INSERT/REPLACE pour :
  1. Les nouvelles maps présentes dans maps_new.sql mais absentes de maps_ancien.sql
  2. Les maps dont des colonnes FONCTIONNELLES ont changé (monsters, capabilities,
     mappos, numgroup, minSize, fixSize, maxSize, forbidden, sniffed)
     → exclut mapData/places/key/date qui changent massivement à cause du format.
  3. (optionnel) Toutes les maps modifiées toutes colonnes confondues

Résultats générés :
  maps_diff_inserts.sql          → nouvelles maps (INSERT IGNORE) + modifiées fonct. (REPLACE)
  maps_new_only.sql              → INSERT IGNORE des 1051 nouvelles maps uniquement
  maps_functional_changes.sql    → REPLACE INTO des maps avec modif fonctionnelles

Usage :
    py generate-maps-diff-inserts.py
    py generate-maps-diff-inserts.py --old maps_ancien.sql --new maps_new.sql
"""

import argparse, os, re, sys, time

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))

INSERT_RE = re.compile(r'^INSERT INTO `maps` VALUES \((\d+),')

# Colonnes dans l'ordre du dump
COLS = ["id", "date", "width", "heigth", "places", "key", "mapData",
        "monsters", "capabilities", "mappos", "numgroup", "minSize",
        "fixSize", "maxSize", "forbidden", "sniffed"]

# Colonnes fonctionnelles "larges" : toutes sauf les données de terrain/format
FUNCTIONAL_COLS = {"monsters", "capabilities", "mappos", "numgroup",
                   "minSize", "fixSize", "maxSize", "forbidden", "sniffed"}

# Colonnes gameplay "strictes" : ce qui pilote vraiment les monstres et restrictions
# (exclut capabilities/maxSize qui changent massivement dans ce dump par valeur par défaut)
GAMEPLAY_COLS = {"monsters", "numgroup", "minSize", "fixSize", "forbidden",
                 "sniffed", "mappos"}

# Colonnes ignorées pour la détection de changement fonctionnel
# (changent massivement à cause d'un re-encodage du dump, pas d'une vraie modif)
FORMAT_COLS = {"mapData", "places", "key", "date", "width", "heigth"}


def parse_values(line: str):
    """Parse les valeurs d'une ligne INSERT SQL → liste de strings."""
    m = re.match(r"^INSERT INTO `maps` VALUES \((.+)\);?$", line)
    if not m:
        return None
    raw = m.group(1)
    vals, current, in_str = [], "", False
    i = 0
    while i < len(raw):
        c = raw[i]
        if c == "'" and not in_str:
            in_str = True
            current += c
        elif c == "'" and in_str:
            if i + 1 < len(raw) and raw[i + 1] == "'":
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


def load_maps(path: str) -> dict:
    """Charge {id -> ligne_complete} depuis un dump SQL."""
    maps = {}
    print(f"  Lecture de {os.path.basename(path)} ...", end="", flush=True)
    t0 = time.time()
    with open(path, "r", encoding="latin-1", errors="replace") as f:
        for line in f:
            m = INSERT_RE.match(line)
            if m:
                maps[int(m.group(1))] = line.rstrip("\n")
    print(f" {len(maps)} maps en {time.time() - t0:.1f}s")
    return maps


def changed_functional_cols(old_line: str, new_line: str) -> list:
    """Retourne la liste des colonnes fonctionnelles qui ont changé entre deux lignes."""
    ov = parse_values(old_line)
    nv = parse_values(new_line)
    if not ov or not nv or len(ov) != len(nv):
        return []
    changed = []
    for i, col in enumerate(COLS):
        if col in FUNCTIONAL_COLS and i < len(ov) and ov[i] != nv[i]:
            changed.append(col)
    return changed


def write_header(out, title, old_name, new_name, extra_lines=None):
    out.write("-- ============================================================\n")
    out.write(f"-- {title}\n")
    out.write("-- Généré par generate-maps-diff-inserts.py\n")
    out.write(f"-- Ancien : {old_name}\n")
    out.write(f"-- Nouveau: {new_name}\n")
    if extra_lines:
        for l in extra_lines:
            out.write(f"-- {l}\n")
    out.write("-- ============================================================\n\n")


def generate_diff(old_path: str, new_path: str, out_dir: str):
    print("=== generate-maps-diff-inserts ===")
    print(f"  Ancien : {old_path}")
    print(f"  Nouveau: {new_path}")
    print()

    old_maps = load_maps(old_path)
    new_maps = load_maps(new_path)

    old_ids = set(old_maps.keys())
    new_ids = set(new_maps.keys())
    common_ids = old_ids & new_ids

    added_ids   = sorted(new_ids - old_ids)
    removed_ids = sorted(old_ids - new_ids)
    all_modified = sorted(mid for mid in common_ids if old_maps[mid] != new_maps[mid])

    # Maps avec changement fonctionnel (large) uniquement
    functional_changes = {}   # {id -> [colonnes modifiées]}
    gameplay_changes = {}     # {id -> [colonnes modifiées gameplay strictes]}
    for mid in all_modified:
        cols_func = changed_functional_cols(old_maps[mid], new_maps[mid])
        if cols_func:
            functional_changes[mid] = cols_func
        # Gameplay strict
        ov = parse_values(old_maps[mid])
        nv = parse_values(new_maps[mid])
        if ov and nv and len(ov) == len(nv):
            cols_gp = [COLS[i] for i, col in enumerate(COLS)
                       if col in GAMEPLAY_COLS and i < len(ov) and ov[i] != nv[i]]
            if cols_gp:
                gameplay_changes[mid] = cols_gp
    func_ids = sorted(functional_changes.keys())
    gp_ids   = sorted(gameplay_changes.keys())

    print(f"  Maps dans ancien              : {len(old_ids)}")
    print(f"  Maps dans nouveau             : {len(new_ids)}")
    print(f"  Nouvelles maps                : {len(added_ids)}")
    print(f"  Maps supprimées               : {len(removed_ids)} {removed_ids if removed_ids else ''}")
    print(f"  Maps modifiées (all cols)     : {len(all_modified)}")
    print(f"  Maps modifiées (fonct. large) : {len(func_ids)}")
    print(f"  Maps modifiées (gameplay)     : {len(gp_ids)}")
    print()

    old_name = os.path.basename(old_path)
    new_name = os.path.basename(new_path)

    # ----------------------------------------------------------------
    # Fichier 1 : maps_new_only.sql  (INSERT IGNORE nouvelles maps)
    # ----------------------------------------------------------------
    path1 = os.path.join(out_dir, "maps_new_only.sql")
    with open(path1, "w", encoding="utf-8") as f:
        write_header(f, "maps_new_only.sql – Nouvelles maps uniquement",
                     old_name, new_name,
                     [f"Nouvelles maps : {len(added_ids)}",
                      "INSERT IGNORE : n'écrase pas les maps existantes",
                      f"IDs : {','.join(str(x) for x in added_ids[:20])}{'...' if len(added_ids)>20 else ''}"])
        for mid in added_ids:
            line = new_maps[mid].replace("INSERT INTO `maps`", "INSERT IGNORE INTO `maps`", 1)
            f.write(line + "\n")
    print(f"  [1] {path1}  ({os.path.getsize(path1)//1024} KB, {len(added_ids)} maps)")

    # ----------------------------------------------------------------
    # Fichier 2 : maps_gameplay_changes.sql  (REPLACE INTO gameplay strict)
    # ----------------------------------------------------------------
    path2 = os.path.join(out_dir, "maps_gameplay_changes.sql")
    with open(path2, "w", encoding="utf-8") as f:
        write_header(f, "maps_gameplay_changes.sql – Maps avec modifications gameplay",
                     old_name, new_name,
                     [f"Maps modifiées (gameplay strict) : {len(gp_ids)}",
                      "Colonnes : " + ", ".join(sorted(GAMEPLAY_COLS)),
                      "REPLACE INTO : remplace les données existantes"])
        for mid in gp_ids:
            cols = gameplay_changes[mid]
            f.write(f"-- ID={mid} | modifié : {', '.join(cols)}\n")
            line = new_maps[mid].replace("INSERT INTO `maps`", "REPLACE INTO `maps`", 1)
            f.write(line + "\n\n")
    print(f"  [2] {path2}  ({os.path.getsize(path2)//1024} KB, {len(gp_ids)} maps)")

    # ----------------------------------------------------------------
    # Fichier 3 : maps_functional_changes.sql  (REPLACE INTO fonct. large)
    # ----------------------------------------------------------------
    path3 = os.path.join(out_dir, "maps_functional_changes.sql")
    with open(path3, "w", encoding="utf-8") as f:
        write_header(f, "maps_functional_changes.sql – Maps avec modifications fonctionnelles (large)",
                     old_name, new_name,
                     [f"Maps modifiées (fonctionnel large) : {len(func_ids)}",
                      "Colonnes : " + ", ".join(sorted(FUNCTIONAL_COLS)),
                      "REPLACE INTO : remplace les données existantes – vérifiez avant d'appliquer !"])
        for mid in func_ids:
            cols = functional_changes[mid]
            f.write(f"-- ID={mid} | modifié : {', '.join(cols)}\n")
            line = new_maps[mid].replace("INSERT INTO `maps`", "REPLACE INTO `maps`", 1)
            f.write(line + "\n\n")
    print(f"  [3] {path3}  ({os.path.getsize(path3)//1024} KB, {len(func_ids)} maps)")

    # ----------------------------------------------------------------
    # Fichier 4 : maps_diff_inserts.sql  (fichier combiné principal)
    # ----------------------------------------------------------------
    path4 = os.path.join(out_dir, "maps_diff_inserts.sql")
    with open(path4, "w", encoding="utf-8") as f:
        write_header(f, "maps_diff_inserts.sql – Diff complet (nouvelles + modifiées gameplay)",
                     old_name, new_name,
                     [f"Nouvelles maps           : {len(added_ids)}",
                      f"Modifiées (gameplay)     : {len(gp_ids)}",
                      f"Supprimées (info)        : {len(removed_ids)} {removed_ids if removed_ids else ''}",
                      "",
                      "SECTION 1 → INSERT IGNORE nouvelles maps",
                      "SECTION 2 → REPLACE INTO maps modifiées (gameplay)",
                      "SECTION 3 → Info : IDs supprimés dans new vs ancien"])

        # Section 1
        f.write(f"\n-- ============================================================\n")
        f.write(f"-- SECTION 1 : Nouvelles maps ({len(added_ids)} entrées)\n")
        f.write(f"--   Présentes dans {new_name}, absentes de {old_name}\n")
        f.write(f"-- ============================================================\n\n")
        if added_ids:
            for mid in added_ids:
                line = new_maps[mid].replace("INSERT INTO `maps`", "INSERT IGNORE INTO `maps`", 1)
                f.write(line + "\n")
        else:
            f.write("-- (aucune nouvelle map)\n")

        # Section 2 – Gameplay strict
        f.write(f"\n-- ============================================================\n")
        f.write(f"-- SECTION 2 : Maps modifiées - changements gameplay ({len(gp_ids)} entrées)\n")
        f.write(f"--   Colonnes : {', '.join(sorted(GAMEPLAY_COLS))}\n")
        f.write(f"--   ATTENTION : REPLACE INTO écrase les données existantes !\n")
        f.write(f"-- ============================================================\n\n")
        if gp_ids:
            for mid in gp_ids:
                cols = gameplay_changes[mid]
                f.write(f"-- ID={mid} | modifié : {', '.join(cols)}\n")
                line = new_maps[mid].replace("INSERT INTO `maps`", "REPLACE INTO `maps`", 1)
                f.write(line + "\n\n")
        else:
            f.write("-- (aucune modification gameplay détectée)\n")

        # Section 3 : info supprimées
        f.write(f"\n-- ============================================================\n")
        f.write(f"-- SECTION 3 : Maps présentes dans {old_name} mais absentes de {new_name}\n")
        f.write(f"--   ({len(removed_ids)} maps) — action manuelle si nécessaire\n")
        f.write(f"-- ============================================================\n\n")
        if removed_ids:
            f.write(f"-- IDs : {','.join(str(x) for x in removed_ids)}\n")
            f.write("-- Pour supprimer (à décommenter si souhaité) :\n")
            f.write(f"-- DELETE FROM `maps` WHERE `id` IN ({','.join(str(x) for x in removed_ids)});\n")
        else:
            f.write("-- (aucune map supprimée)\n")

    print(f"  [4] {path4}  ({os.path.getsize(path4)//1024} KB)")
    print()
    print("Terminé.")


def main():
    parser = argparse.ArgumentParser(
        description="Génère des fichiers SQL diff entre maps_ancien.sql et maps_new.sql"
    )
    parser.add_argument("--old", default=os.path.join(SCRIPT_DIR, "maps_ancien.sql"))
    parser.add_argument("--new", default=os.path.join(SCRIPT_DIR, "maps_new.sql"))
    parser.add_argument("--outdir", default=SCRIPT_DIR,
                        help="Dossier de sortie (défaut: même dossier que le script)")
    args = parser.parse_args()

    for p in [args.old, args.new]:
        if not os.path.exists(p):
            print(f"ERREUR : fichier introuvable : {p}", file=sys.stderr)
            sys.exit(1)

    generate_diff(args.old, args.new, args.outdir)


if __name__ == "__main__":
    main()







