#!/usr/bin/env python3
"""Незалежний еталон для тестів: точні дроби замість double."""
from fractions import Fraction

ACRE = Fraction(43560) * Fraction("0.3048") ** 2
UNITS = {"CM2": Fraction(1, 10000), "M2": Fraction(1), "ARE": Fraction(100),
         "HECTARE": Fraction(10000), "ACRE": ACRE}

print("acre у m2:", float(ACRE), "точно:", ACRE == Fraction("4046.8564224"))
for src, a in UNITS.items():
    for dst, b in UNITS.items():
        if src != dst:
            print(f'"{src},{dst},{float(a / b)!r}",')
