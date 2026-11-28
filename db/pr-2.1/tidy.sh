#!/bin/sh
# Скорочує вивід mysql -vvv: коментарі з файла друкуються як є, запит після "mysql>", без зайвих "Query OK" і порожніх рядків.
exec awk '
function out(s) { if (s == "") { if (last != "") print ""; last = ""; return } print s; last = s }
/^--------------$/ {
  if (!inq) { inq = 1; n = 0; next }
  inq = 0; allc = 1
  for (i = 1; i <= n; i++) if (q[i] !~ /^--/) allc = 0
  if (allc) { if (last !~ /^--/) out(""); for (i = 1; i <= n; i++) out(q[i]); skip = 1 }
  else { out(""); for (i = 1; i <= n; i++) out((i == 1 ? "mysql> " : "       ") q[i]); skip = 0; afterq = 1 }
  next
}
inq { q[++n] = $0; next }
skip && /^Query OK, 0 rows affected/ { skip = 0; next }
skip && /^$/ { next }
afterq && /^$/ { afterq = 0; next }
/^Bye$/ { next }
{ skip = 0; afterq = 0; out($0) }
'
