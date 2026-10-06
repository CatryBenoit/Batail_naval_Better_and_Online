#!/usr/bin/env python3
"""
Prépare les images du plateau à partir des images générées (outils/sources) :

    python3 outils/preparer_effets.py mer   source.webp  destination.jpg   texture de mer qui se répète sans raccord
    python3 outils/preparer_effets.py sprite source.png destination.png    sprite détouré (fond uni), carré, centré

Nécessite Pillow : pip install pillow
"""
import os
import sys
from PIL import Image, ImageChops

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from decouper_navire import enlever_fond  # noqa: E402  (même détourage que pour les navires)


def lisse(t):
    """0 -> 0, 1 -> 1, avec un départ et une arrivée en douceur."""
    t = max(0.0, min(1.0, t))
    return t * t * (3 - 2 * t)


def rendre_raccordable(im):
    """
    Rend une texture répétable sans raccord visible : on la mélange avec une copie décalée d'une demi-image.
    Au centre on garde l'original, vers les bords on passe progressivement à la copie décalée, dont les bords
    (qui viennent du milieu de l'original) se raccordent parfaitement quand on répète l'image.
    """
    w, h = im.size
    decale = ImageChops.offset(im, w // 2, h // 2)  # décalage d'une demi-image, en faisant le tour
    masque = Image.new("L", (w, h))
    px = masque.load()
    marge_x, marge_y = w * 0.3, h * 0.3
    for y in range(h):
        my = lisse(min(y, h - 1 - y) / marge_y)
        for x in range(w):
            px[x, y] = round(255 * my * lisse(min(x, w - 1 - x) / marge_x))
    return Image.composite(im, decale, masque)


def mer(source, destination, taille=512):
    im = Image.open(source).convert("RGB")
    im = rendre_raccordable(im).resize((taille, taille), Image.LANCZOS)
    im.save(destination, quality=90)


def sprite(source, destination, taille=128):
    im = enlever_fond(Image.open(source), 40)
    boite = im.getchannel("A").point(lambda v: 255 if v > 40 else 0).getbbox()
    if boite is None:
        raise SystemExit("rien trouvé sur " + source)
    im = im.crop(boite)
    cote = max(im.size)
    carre = Image.new("RGBA", (cote, cote), (0, 0, 0, 0))
    carre.paste(im, ((cote - im.width) // 2, (cote - im.height) // 2), im)
    carre.resize((taille, taille), Image.LANCZOS).save(destination)


if __name__ == "__main__":
    if len(sys.argv) != 4 or sys.argv[1] not in ("mer", "sprite"):
        raise SystemExit(__doc__)
    {"mer": mer, "sprite": sprite}[sys.argv[1]](sys.argv[2], sys.argv[3])
    print("ok :", sys.argv[3])
