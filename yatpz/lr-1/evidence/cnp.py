#!/usr/bin/env python3
"""Генерує 5 коректних CNP (Challenge #4) і показує розрахунок контрольної цифри."""
WEIGHTS = "279146358279"


def control(first12: str) -> tuple[int, int, int]:
    """Повертає суму добутків, остачу від ділення на 11 і контрольну цифру."""
    total = sum(int(d) * int(w) for d, w in zip(first12, WEIGHTS))
    rest = total % 11
    return total, rest, 1 if rest == 10 else rest


# стать і століття, рік, місяць, день, регіон, порядковий номер, що перевіряє цей набір
CASES = [
    ("1", "80", "05", "15", "01", "001", "чоловік, XX ст., регіон 01 (нижня межа)"),
    ("2", "99", "12", "31", "52", "002", "жінка, остання дата XX ст., регіон 52 (верхня межа)"),
    ("5", "00", "02", "29", "12", "003", "чоловік, XXI ст., 29 лютого високосного року"),
    ("6", "04", "04", "30", "40", "004", "жінка, XXI ст., останній день 30-денного місяця"),
    ("1", "75", "01", "31", "23", None, "остача 10, контрольна цифра 1"),
]


def main() -> None:
    print("CNP            сума остача контрольна  що перевіряє")
    for s, yy, mm, dd, area, order, note in CASES:
        if order is None:
            # шукаємо порядковий номер, для якого остача дорівнює 10
            order = next(f"{n:03d}" for n in range(1, 1000)
                         if control(s + yy + mm + dd + area + f"{n:03d}")[1] == 10)
        first12 = s + yy + mm + dd + area + order
        total, rest, digit = control(first12)
        print(f"{first12}{digit}  {total:4d} {rest:6d} {digit:10d}  {note}")


if __name__ == "__main__":
    main()
