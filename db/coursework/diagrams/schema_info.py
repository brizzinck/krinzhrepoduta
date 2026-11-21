#!/usr/bin/env python3
"""Read the schema of a running MySQL container from information_schema.

The container and credentials come from the environment (set by make_diagrams.sh):
DB_CONTAINER, DB_USER, DB_PASSWORD, DB_NAME. Nothing is parsed from the DDL file.
"""
import os
import subprocess
from collections import OrderedDict

CONTAINER = os.environ.get("DB_CONTAINER", "cw-diag-mysql")
USER = os.environ.get("DB_USER", "root")
PASSWORD = os.environ.get("DB_PASSWORD", "diag")
SCHEMA = os.environ.get("DB_NAME", "micromobility")


def query(sql):
    """Run one statement in the container and return the rows as lists of strings (NULL becomes None)."""
    cmd = ["docker", "exec", "-i", "-e", f"MYSQL_PWD={PASSWORD}", CONTAINER,
           "mysql", f"-u{USER}", "-N", "-B", "--default-character-set=utf8mb4", "-e", sql]
    out = subprocess.run(cmd, capture_output=True, text=True, check=True).stdout
    return [[None if v == "NULL" else v for v in line.split("\t")] for line in out.rstrip("\n").split("\n") if line]


def load():
    """Return (tables, fks, uniques, indexes).

    tables:  OrderedDict name maps to list of dict(name, type, nullable, key, auto)
    fks:     list of dict(name, table, cols, ref_table, ref_cols, on_delete, on_update), one per constraint
    uniques: dict table maps to list of column lists of UNIQUE constraints (not the primary key)
    indexes: dict table maps to list of dict(name, unique, parts) without the primary key; parts are column names or expressions
    """
    s = SCHEMA
    tables = OrderedDict()
    for t, c, ty, nul, key, extra in query(
            "SELECT table_name, column_name, column_type, is_nullable, column_key, extra "
            f"FROM information_schema.columns WHERE table_schema='{s}' ORDER BY table_name, ordinal_position"):
        tables.setdefault(t, []).append(
            dict(name=c, type=ty, nullable=nul == "YES", key=key, auto="auto_increment" in (extra or "")))

    rules = {n: (d, u) for n, d, u in query(
        "SELECT constraint_name, delete_rule, update_rule FROM information_schema.referential_constraints "
        f"WHERE constraint_schema='{s}'")}
    fks = OrderedDict()
    for n, t, c, rt, rc in query(
            "SELECT constraint_name, table_name, column_name, referenced_table_name, referenced_column_name "
            "FROM information_schema.key_column_usage "
            f"WHERE table_schema='{s}' AND referenced_table_name IS NOT NULL "
            "ORDER BY table_name, constraint_name, ordinal_position"):
        f = fks.setdefault((t, n), dict(name=n, table=t, cols=[], ref_table=rt, ref_cols=[],
                                        on_delete=rules[n][0], on_update=rules[n][1]))
        f["cols"].append(c)
        f["ref_cols"].append(rc)

    uniques = {}
    ucols = OrderedDict()
    for n, t, c in query(
            "SELECT k.constraint_name, k.table_name, k.column_name FROM information_schema.key_column_usage k "
            "JOIN information_schema.table_constraints c ON c.constraint_schema=k.constraint_schema "
            "AND c.table_name=k.table_name AND c.constraint_name=k.constraint_name "
            f"WHERE k.table_schema='{s}' AND c.constraint_type='UNIQUE' "
            "ORDER BY k.table_name, k.constraint_name, k.ordinal_position"):
        ucols.setdefault((t, n), []).append(c)
    for (t, _n), cols in ucols.items():
        uniques.setdefault(t, []).append(cols)

    indexes = {}
    idx = OrderedDict()
    for t, n, nonu, seq, col, expr in query(
            "SELECT table_name, index_name, non_unique, seq_in_index, column_name, expression "
            f"FROM information_schema.statistics WHERE table_schema='{s}' AND index_name<>'PRIMARY' "
            "ORDER BY table_name, index_name, seq_in_index"):
        i = idx.setdefault((t, n), dict(name=n, unique=nonu == "0", parts=[]))
        i["parts"].append(col if col else clean_expr(expr))
    for (t, _n), i in idx.items():
        indexes.setdefault(t, []).append(i)
    return tables, list(fks.values()), uniques, indexes


def clean_expr(expr):
    """Make a functional index expression readable: drop backticks and the outer parentheses."""
    expr = (expr or "").replace("`", "")
    if expr.startswith("(") and expr.endswith(")"):
        expr = expr[1:-1]
    return expr




# Relation names of db-schema.md section 3: constraint name maps to (parent to child, child to parent, cardinality of the child side).
RELATIONS = {
    "fk_tariffs_operator": ("sets", "is_set_by", "0..N"),
    "fk_vehicles_operator": ("owns", "is_owned_by", "0..N"),
    "fk_vehicles_tariff": ("prices", "is_priced_by", "0..N"),
    "fk_vehicles_station": ("hosts", "is_parked_at", "0..N"),
    "fk_rides_customer": ("takes", "is_taken_by", "0..N"),
    "fk_rides_vehicle": ("is_used_in", "uses", "0..N"),
    "fk_payments_customer": ("holds", "belongs_to", "1..N"),
    "fk_payments_ride_customer": ("is_settled_by", "settles", "0..N"),
}

# Fixed lists stored as TINYINT codes (db-schema.md section 1.1). information_schema has no trace of them,
# so the legend text lives here: column key "table.column" maps to (title, list of "code NAME label").
CODES = {
    "vehicle_type": ("tariffs.vehicle_type, vehicles.vehicle_type", ["1 E_SCOOTER Електросамокат", "2 E_BIKE Електровелосипед"]),
    "vehicles.status": ("vehicles.status", ["1 AVAILABLE Чекає клієнта", "2 IN_RIDE У поїздці", "3 CHARGING На зарядці",
                                            "4 REPAIR У ремонті", "5 RETIRED Списано"]),
    "payments.kind": ("payments.kind", ["1 BONUS Бонус за реєстрацію", "2 TOPUP Поповнення",
                                        "3 RIDE Оплата поїздки", "4 REFUND Повернення за поїздку"]),
}


def code_key(table, column):
    """Return the CODES key when the column holds a code, else None."""
    if column == "vehicle_type":
        return "vehicle_type"
    key = f"{table}.{column}"
    return key if key in CODES else None


def legend_text():
    """Legend of the code columns as one DOT string (left-aligned lines)."""
    out = ["Коди стовпців (список зберігається в застосунку як enum, у БД діапазон перевіряє CHECK):\\l"]
    for title, items in CODES.values():
        out.append(f"{title}: " + "; ".join(items) + "\\l")
    return "".join(out)
