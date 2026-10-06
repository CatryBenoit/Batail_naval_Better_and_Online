#!/usr/bin/env bash
# Régénère toutes les images du plateau à partir des images générées rangées dans outils/sources :
#   - les navires découpés en cases (src/Images/navires)
#   - la texture de mer et les effets : bombe, explosion, plouf (src/Images/effets)
# Chaque navire est découpé en cases de 100 px (affichées en 50 px : reste net en plein écran).
#   cuirasses.webp : 3 cuirassés     croiseurs.webp : 3 croiseurs
# Nom des fichiers : navire<taille><variante>_<n° de case>.png  (case 1 = poupe, dernière case = proue)
set -e
cd "$(dirname "$0")/.."
SORTIE=src/Images/navires
rm -rf "$SORTIE"
mkdir -p "$SORTIE"

decoupe() { # image taille n°navire nom
    python3 outils/decouper_navire.py "outils/sources/$1" "$2" --navire "$3" --nom "$4" --taille 100 --sortie "$SORTIE" > /dev/null
}

decoupe cuirasses.webp 5 1 navire5a
decoupe croiseurs.webp 4 1 navire4a
decoupe croiseurs.webp 3 2 navire3a
decoupe croiseurs.webp 3 3 navire3b
decoupe cuirasses.webp 2 2 navire2a
decoupe croiseurs.webp 2 2 navire2b
decoupe cuirasses.webp 2 3 navire2c
decoupe croiseurs.webp 1 3 navire1a
decoupe croiseurs.webp 1 1 navire1b

rm -f "$SORTIE"/*_apercu.png   # les aperçus ne servent qu'à vérifier
echo "Navires : $(ls "$SORTIE" | wc -l) images dans $SORTIE"

EFFETS=src/Images/effets
rm -rf "$EFFETS"
mkdir -p "$EFFETS"
python3 outils/preparer_effets.py mer    outils/sources/mer.webp       "$EFFETS/mer.jpg"       > /dev/null
python3 outils/preparer_effets.py sprite outils/sources/bombe.png      "$EFFETS/bombe.png"     > /dev/null
python3 outils/preparer_effets.py sprite outils/sources/explosion.webp "$EFFETS/explosion.png" > /dev/null
python3 outils/preparer_effets.py sprite outils/sources/plouf.webp     "$EFFETS/plouf.png"     > /dev/null
echo "Effets : $(ls "$EFFETS" | tr '\n' ' ')"
