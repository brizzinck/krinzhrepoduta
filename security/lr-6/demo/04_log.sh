#!/bin/bash
# Журнал невдалих спроб: що записав застосунок за час демонстрації.
cd "$(dirname "$0")/../app" || exit 1
echo '$ cat logs/auth-failures.log'
cat logs/auth-failures.log
echo
echo '$ grep -c "eyJ" logs/auth-failures.log   # повних токенів у журналі немає'
grep -c "eyJ" logs/auth-failures.log
