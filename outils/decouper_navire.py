#!/usr/bin/env python3
"""
Découpe l'image d'un navire vu du ciel en cases carrées, prêtes pour la grille du jeu.

    python3 outils/decouper_navire.py mon_navire.png 4
    python3 outils/decouper_navire.py mon_navire.png 4 --sortie src/Images/navires --nom croiseur --taille 64
    python3 outils/decouper_navire.py trois_navires.png 4 --navire 2     (image avec plusieurs navires : prend le 2e)

Ce que fait le script :
  1. enlève le fond (couleur unie prise dans les coins de l'image : blanc, vert, etc.)
     et corrige le reflet vert sur les bords du navire quand le fond est vert
  2. sépare les navires s'il y en a plusieurs sur l'image, et recadre au plus près
  3. met le navire à l'horizontale si besoin (proue à droite : à vérifier sur l'aperçu)
  4. le met à exactement N cases de long sur 1 case de large (le navire occupe ~80 % de la largeur).
     Si le navire est trop long pour N cases (ex. un cuirassé sur 2 cases), il est RACCOURCI au lieu
     d'être écrasé : on garde la poupe, la proue et une tranche du milieu (tourelles, cheminées),
     comme on raccourcirait une maquette. Un même cuirassé peut ainsi servir pour toutes les tailles.
  5. enregistre une image par case (nom_1.png ... nom_N.png, fond transparent) et un aperçu

Nécessite Pillow : pip install pillow
"""
import argparse
import os
from PIL import Image, ImageChops, ImageFilter


def couleur_du_fond(im):
    """Couleur moyenne des 4 coins (on suppose un fond uni)."""
    w, h = im.size
    coins = [im.getpixel((x, y)) for x, y in ((2, 2), (w - 3, 2), (2, h - 3), (w - 3, h - 3))]
    return tuple(sum(c[i] for c in coins) // 4 for i in range(3))


def enlever_fond(im, tolerance):
    """Rend transparent tout ce qui est proche de la couleur du fond."""
    im = im.convert("RGBA")
    if im.getextrema()[3][0] < 255:
        return im  # l'image a déjà de la transparence : on la garde telle quelle
    fond = Image.new("RGB", im.size, couleur_du_fond(im))
    diff = ImageChops.difference(im.convert("RGB"), fond).convert("L")
    masque = diff.point(lambda v: 255 if v > tolerance else 0)
    # adoucit le bord du navire (pas d'escalier) et bouche les petits trous
    masque = masque.filter(ImageFilter.MaxFilter(3)).filter(ImageFilter.MinFilter(3)).filter(ImageFilter.GaussianBlur(0.7))
    im.putalpha(masque)
    r, v, b = couleur_du_fond(im)
    if v > 150 and v > r + 60 and v > b + 60:
        im = enlever_reflet_vert(im)
    return im


def enlever_reflet_vert(im):
    """Fond vert : les bords du navire en gardent une teinte verte. On ramène le vert au niveau du rouge/bleu."""
    r, v, b, a = im.split()
    # vert = min(vert, max(rouge, bleu)) : un gris reste gris, une teinte verte redevient grise
    v = ImageChops.darker(v, ImageChops.lighter(r, b))
    return Image.merge("RGBA", (r, v, b, a))


def separer_navires(alpha):
    """Boîtes (x0, y0, x1, y1) de chaque navire : on coupe sur les bandes vides, en lignes puis en colonnes."""
    w, h = alpha.size
    px = alpha.load()
    def bandes(n, plein):
        res, debut = [], None
        for i in range(n):
            if plein(i) and debut is None:
                debut = i
            elif not plein(i) and debut is not None:
                res.append((debut, i))
                debut = None
        if debut is not None:
            res.append((debut, n))
        # on ignore les petites taches (bouts de fond mal enlevés)
        return [bd for bd in res if bd[1] - bd[0] > 8]
    seuil = 3  # pixels opaques mini pour qu'une ligne/colonne compte
    lignes = bandes(h, lambda y: sum(1 for x in range(0, w, 2) if px[x, y] > 128) >= seuil)
    boites = []
    for (y0, y1) in lignes:
        colonnes = bandes(w, lambda x: any(px[x, y] > 128 for y in range(y0, y1, 2)))
        for (x0, x1) in colonnes:
            boites.append((x0, y0, x1, y1))
    return boites


def main():
    p = argparse.ArgumentParser(description="Découpe un navire vu du ciel en N cases.")
    p.add_argument("image", help="image du navire entier, vu de dessus, sur un fond uni")
    p.add_argument("cases", type=int, help="longueur du navire en cases (1 à 5)")
    p.add_argument("--sortie", default="navires_decoupes", help="dossier de sortie")
    p.add_argument("--nom", default=None, help="préfixe des fichiers (par défaut : navire<N>)")
    p.add_argument("--taille", type=int, default=64, help="taille d'une case en pixels (défaut 64)")
    p.add_argument("--remplissage", type=float, default=0.8, help="largeur du navire / largeur de case (défaut 0.8)")
    p.add_argument("--tolerance", type=int, default=40, help="tolérance pour enlever le fond (défaut 40)")
    p.add_argument("--inverser", action="store_true", help="retourner le navire (si la proue est à gauche)")
    p.add_argument("--etirer", action="store_true", help="ne jamais raccourcir le navire, seulement l'étirer")
    p.add_argument("--navire", type=int, default=0,
                   help="s'il y a plusieurs navires sur l'image : numéro de celui à garder (sinon tous, un dossier chacun)")
    a = p.parse_args()

    nom = a.nom or f"navire{a.cases}"
    im = enlever_fond(Image.open(a.image), a.tolerance)
    boites = separer_navires(im.getchannel("A"))
    if not boites:
        raise SystemExit("Aucun navire trouvé : le fond n'est pas uni ? Essayez --tolerance 60")
    if a.navire:
        if a.navire > len(boites):
            raise SystemExit(f"Il n'y a que {len(boites)} navire(s) sur l'image")
        decouper(im.crop(boites[a.navire - 1]), a, a.sortie, nom)
    elif len(boites) == 1:
        decouper(im.crop(boites[0]), a, a.sortie, nom)
    else:
        print(f"{len(boites)} navires trouvés : un dossier par navire (choisissez-en un, puis utilisez --navire)")
        for k, boite in enumerate(boites, 1):
            decouper(im.crop(boite), a, os.path.join(a.sortie, f"navire_{k}"), nom)


def raccourcir(im, proportion_voulue):
    """
    Raccourcit le navire (vu de dessus, horizontal) pour qu'il ait la proportion longueur/largeur voulue,
    en retirant des morceaux au milieu de la coque plutôt qu'en l'écrasant.
    On garde : la poupe, une tranche centrale (superstructure) et la proue.
    """
    l, h = im.size
    voulu = round(proportion_voulue * h)
    if l <= voulu * 1.35:
        return im  # pas beaucoup trop long : un léger tassement passe inaperçu
    # largeur de la coque colonne par colonne : on coupe là où elle atteint presque sa largeur maximale,
    # pour que les morceaux se raccordent sans marche
    alpha = im.getchannel("A").point(lambda v: 255 if v > 128 else 0)
    largeurs = [alpha.crop((x, 0, x + 1, h)).histogram()[255] for x in range(l)]
    pleine = [x for x in range(l) if largeurs[x] >= 0.97 * max(largeurs)]
    poupe_fin, proue_debut = pleine[0], pleine[-1]
    bout = max(poupe_fin, l - proue_debut)   # longueur des bouts effilés gardés
    if bout * 2 >= l:
        return im
    if voulu >= 2 * bout + 0.15 * voulu:
        centre = voulu - 2 * bout
        bout_voulu = bout
    else:
        # très court : les bouts sont tassés pour garder une tranche du milieu (~30 %)
        centre = round(0.3 * voulu)
        bout_voulu = (voulu - centre) // 2
        centre = voulu - 2 * bout_voulu
    poupe = im.crop((0, 0, bout, h))
    proue = im.crop((l - bout, 0, l, h))
    if bout_voulu != bout:
        poupe = poupe.resize((bout_voulu, h), Image.LANCZOS)
        proue = proue.resize((bout_voulu, h), Image.LANCZOS)
    milieu = im.crop((l // 2 - centre // 2, 0, l // 2 - centre // 2 + centre, h))
    resultat = Image.new("RGBA", (voulu, h), (0, 0, 0, 0))
    resultat.paste(poupe, (0, 0))
    resultat.paste(milieu, (bout_voulu, 0))
    resultat.paste(proue, (bout_voulu + centre, 0))
    return resultat


def decouper(im, a, sortie, nom):
    n, t = a.cases, a.taille
    os.makedirs(sortie, exist_ok=True)
    # recadrage au plus près (sans les pixels presque transparents)
    im = im.crop(im.getchannel("A").point(lambda v: 255 if v > 40 else 0).getbbox())
    if im.height > im.width:          # navire vertical : on le couche
        im = im.rotate(-90, expand=True)
    if a.inverser:
        im = im.transpose(Image.FLIP_LEFT_RIGHT)

    # N cases de long, ~80 % d'une case de large, centré verticalement dans la bande
    longueur = n * t - max(2, t // 16) * 2   # petite marge aux deux bouts
    largeur = max(1, round(t * a.remplissage))
    if not a.etirer:
        im = raccourcir(im, longueur / largeur)
    im = im.resize((longueur, largeur), Image.LANCZOS)
    bande = Image.new("RGBA", (n * t, t), (0, 0, 0, 0))
    bande.paste(im, ((n * t - longueur) // 2, (t - largeur) // 2), im)

    for i in range(n):
        case = bande.crop((i * t, 0, (i + 1) * t, t))
        case.save(os.path.join(sortie, f"{nom}_{i + 1}.png"))

    # aperçu : le navire sur des cases, pour vérifier le découpage
    apercu = Image.new("RGBA", (n * t, t), (0, 0, 0, 0))
    for i in range(n):
        couleur = (245, 222, 179, 255) if i % 2 == 0 else (0, 255, 255, 255)  # couleurs de la grille du jeu
        apercu.paste(Image.new("RGBA", (t, t), couleur), (i * t, 0))
    apercu.alpha_composite(bande)
    apercu.save(os.path.join(sortie, f"{nom}_apercu.png"))
    print(f"{n} cases enregistrées dans {sortie}/ ({nom}_1.png ... {nom}_{n}.png) + {nom}_apercu.png")


if __name__ == "__main__":
    main()
