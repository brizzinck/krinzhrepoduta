#!/usr/bin/env python3
"""Генерує northwind_discount_fix.sql зі знижками з T-SQL-дампа LabWork2.sql.

У MySQL-дампі стовпець Discount має тип float(24,0), тому всі знижки (0,05; 0,1; 0,15 ...)
округлилися до 0. У T-SQL-дампі значення збережені правильно. Скрипт читає рядки
INSERT "Order Details" VALUES(order, product, price, qty, discount) і пише SQL, який:
  1) змінює тип стовпця на DECIMAL(4,2);
  2) виставляє знижки через тимчасову таблицю.
Використання: python3 -I gen_discount_fix.py LabWork2.sql > northwind_discount_fix.sql
"""
import re
import sys
from decimal import Decimal

src = open(sys.argv[1], encoding="utf-8", errors="replace").read()
rx = re.compile(r'INSERT\s+"Order Details"\s+VALUES\(\s*(\d+)\s*,\s*(\d+)\s*,\s*([\d.]+)\s*,\s*(\d+)\s*,\s*([\d.]+)\s*\)', re.I)
rows = [(int(o), int(p), Decimal(pr), int(q), Decimal(d)) for o, p, pr, q, d in rx.findall(src)]
nonzero = [(o, p, d) for o, p, pr, q, d in rows if d != 0]
print(f"-- Знижки з T-SQL-дампа: усього рядків {len(rows)}, зі знижкою {len(nonzero)}")
print("ALTER TABLE `order details` MODIFY `Discount` DECIMAL(4,2) NOT NULL DEFAULT 0;")
print("CREATE TEMPORARY TABLE tmp_discount (OrderID INT, ProductID INT, Discount DECIMAL(4,2), PRIMARY KEY (OrderID, ProductID));")
for i in range(0, len(nonzero), 200):
    chunk = nonzero[i:i + 200]
    print("INSERT INTO tmp_discount VALUES " + ",".join(f"({o},{p},{d})" for o, p, d in chunk) + ";")
print("UPDATE `order details` od JOIN tmp_discount t ON t.OrderID = od.OrderID AND t.ProductID = od.ProductID SET od.Discount = t.Discount;")
print("DROP TEMPORARY TABLE tmp_discount;")
