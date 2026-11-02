"""Незалежна перевірка підсумків: перерахунок за вхідними файлами без Java, точна арифметика Decimal."""
from decimal import Decimal


def check(path):
    total_lines = bad = 0
    total = Decimal(0)
    with open(path, encoding="utf-8") as f:
        next(f)  # заголовок
        for line in f:
            total_lines += 1
            parts = line.rstrip("\n").split(";")
            try:
                if len(parts) != 3 or not parts[0].strip():
                    raise ValueError
                qty = int(parts[1])
                if qty <= 0 or "," in parts[2]:
                    raise ValueError
                price = Decimal(parts[2])
                if price < 0:
                    raise ValueError
                total += qty * price
            except (ValueError, ArithmeticError):
                bad += 1
    return total_lines, total_lines - bad, bad, total


for name in ("v2_correct", "v2_damaged", "v2_large"):
    n, ok, bad, total = check(f"data/{name}.txt")
    print(f"{name}.txt: рядків даних {n}, коректних {ok}, відхилено {bad}, загальна вартість {total}")
