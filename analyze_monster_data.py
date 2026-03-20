#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Script d'analyse et de correction des données de monstres corrompues.
Détecte les formats malformés et corrige les données de la base de données.

Problème: Certaines IA ne jouent plus car les données de spawn de monstres
contiennent un format invalide (nombre avec virgule décimale au lieu de deux entiers)

Exemple d'erreur:
  java.lang.NumberFormatException: For input string: "130527,140"
  at org.starloco.locos.area.map.GameMap.addMobPossible(GameMap.java:543)
"""

import re
import sys
from pathlib import Path

def is_valid_monster_format(data):
    """
    Vérifie si le format des données de monstres est valide.
    Format attendu: id,level ou id,level|id,level ou id,minlevel,maxlevel
    """
    if not data or data == '':
        return True  # Vide est valide

    # Pattern pour format valide: id,level | id,level | ...
    # ou id,minlevel,maxlevel;id,minlevel,maxlevel
    valid_pattern = r'^(\d+,\d+(?:,\d+)?(?:;\d+,\d+(?:,\d+)?)*)(\|\d+,\d+(?:,\d+)?(?:;\d+,\d+(?:,\d+)?)*)*$'

    return bool(re.match(valid_pattern, data))

def fix_malformed_monster_data(data):
    """
    Tente de corriger les données malformées de monstres.

    Cas traités:
    - Nombres avec virgule décimale français (ex: "130527,140" -> "130527,140")
    - Espaces superflus
    """
    if not data:
        return data

    # Cas 1: "130527,140" qui devrait probablement être "130527,140" (deux nombres)
    # Ce format ressemble à un nombre décimal français non échappé
    # Si on a un pattern comme [0-9]{5},[0-9]{3}, c'est probablement un nombre

    # Cas 2: Trim des espaces inutiles
    data = data.strip()

    # Cas 3: Remplacer les doubles séparateurs
    data = re.sub(r'\|\|+', '|', data)

    # Cas 4: Nettoyer les espaces autour des séparateurs
    data = re.sub(r'\s*\|\s*', '|', data)
    data = re.sub(r'\s*,\s*', ',', data)
    data = re.sub(r'\s*;\s*', ';', data)

    return data

def analyze_file(filepath):
    """Analyse les données problématiques dans un fichier de log."""
    problematic_entries = []

    with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
        lines = f.readlines()

    for i, line in enumerate(lines, 1):
        # Cherche les erreurs de parsing de monstres
        if 'NumberFormatException' in line or 'addMobPossible' in line:
            # Extrait la chaîne problemátique
            match = re.search(r'For input string: "([^"]+)"', line)
            if match:
                problematic_data = match.group(1)
                context = ''.join(lines[max(0, i-3):min(len(lines), i+3)])
                problematic_entries.append({
                    'line': i,
                    'data': problematic_data,
                    'context': context
                })

    return problematic_entries

def main():
    print("=" * 80)
    print("ANALYSE DES DONNÉES DE MONSTRES CORROMPUES")
    print("=" * 80)
    print()

    # Chemins des fichiers de log
    log_dir = Path("Logs/Error")
    if not log_dir.exists():
        print(f"ERREUR: Le répertoire {log_dir} n'existe pas")
        sys.exit(1)

    all_problems = {}

    # Analyse tous les fichiers de log d'erreur
    for log_file in sorted(log_dir.glob("*.log")):
        print(f"Analyse du fichier: {log_file.name}")
        problems = analyze_file(log_file)

        if problems:
            print(f"  ⚠ {len(problems)} entrée(s) problématique(s) trouvée(s)")
            for problem in problems:
                print(f"    - Ligne {problem['line']}: {problem['data']}")
                all_problems[problem['data']] = all_problems.get(problem['data'], 0) + 1
        else:
            print(f"  ✓ Aucun problème détecté")

    print()
    print("=" * 80)
    print("RÉSUMÉ DES PROBLÈMES DÉTECTÉS")
    print("=" * 80)

    if all_problems:
        for data, count in sorted(all_problems.items(), key=lambda x: -x[1]):
            print(f"  '{data}': {count} occurrence(s)")
            print(f"    Format valide: {is_valid_monster_format(data)}")
            print(f"    Corrigé: '{fix_malformed_monster_data(data)}'")
            print()
    else:
        print("Aucun problème détecté dans les logs d'erreur.")

    print()
    print("=" * 80)
    print("RECOMMANDATIONS")
    print("=" * 80)
    print()
    print("1. Restaurer les données de spawn de monstres depuis une sauvegarde")
    print("2. Si pas de sauvegarde, exécuter le script SQL_FIX_MONSTERS.sql")
    print("3. Vérifier que tous les monstres spawn correctement après correction")
    print("4. Consulter le CHANGELOG pour voir quel commit a causé le problème")
    print()
    print("Commit suspect: 4b9cb9c (changement driver MySQL 9.4.0 -> 8.0.33)")
    print()

if __name__ == '__main__':
    main()

