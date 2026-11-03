#!/bin/sh
# План, вартість, лічильники й час одного запиту з part2_queries.sql.
# Використання: ./qstat.sh q3 before   (запит: q3..q7, q5_new, q6_new; стан: before або after)
set -eu
cd "$(dirname "$0")"
M=../_shared/mysql.sh
DB=northwind_opt
RUNS=500
TAB=$(printf '\t')

section() {
  awk -v h="-- $1." 'index($0, h) == 1 { on = 1; next } /^-- [0-9a-z_]+\./ { on = 0 } on' part2_queries.sql
}

Q=$(section "$1" | sed '/^$/d')
Q=${Q%;}
PLAN=plans/${1%_new}_$2.json

# Прогрів: перший запуск читає сторінки з диска
printf '%s;\n' "$Q" | $M -N -B "$DB" > /dev/null

echo "# EXPLAIN, основні стовпці"
printf 'EXPLAIN %s;\n' "$Q" | $M -B "$DB" | cut -f2,3,5,7,10,12 | column -t -s "$TAB"

echo "# EXPLAIN ANALYZE"
printf 'EXPLAIN ANALYZE %s;\n' "$Q" | $M -N -B -r "$DB" | sed '/^$/d'

printf 'EXPLAIN FORMAT=JSON %s;\n' "$Q" | $M -N -B -r "$DB" > "$PLAN"
python3 -c '
import json, sys
block = json.load(open(sys.argv[1]))["query_block"]
specs = block.get("union_result", {}).get("query_specifications", [])
if specs:  # UNION ALL: у плані окрема вартість кожного SELECT
    costs = [float(s["query_block"]["cost_info"]["query_cost"]) for s in specs]
    print("query_cost:", " + ".join(map(str, costs)), "=", round(sum(costs), 2))
else:
    print("query_cost:", block["cost_info"]["query_cost"])
' "$PLAN"

# Лічильники рахують звернення до рядків за один запуск
{ echo 'FLUSH STATUS;'; printf '%s;\n' "$Q"; section handler; } | $M -N -B "$DB" | tail -n 4 |
  awk '{ sub(/Handler_read_/, "", $1); s = s " " $1 "=" $2 } END { print "Handler_read_*:" s }'

$M "$DB" -e 'TRUNCATE TABLE performance_schema.events_statements_summary_by_digest'
i=0
while [ "$i" -lt "$RUNS" ]; do
  printf '%s;\n' "$Q"
  i=$((i + 1))
done | $M -N -B "$DB" > /dev/null

echo "# $RUNS запусків поспіль, час за performance_schema"
section digest | $M -B "$DB" | column -t -s "$TAB"
