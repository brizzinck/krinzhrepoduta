#!/bin/bash
# Повний запуск ПР 7: підготовка, збірка, Semgrep, Trivy, перевірки "що, якщо", підсумок, перевірка CVSS.
# Потрібен лише docker. Перший запуск Trivy завантажує бази (близько 3 ГБ у кеш work/security/pr-7/cache/trivy).
# Повторний запуск того ж дня: TRIVY_SKIP_DB=1 ./scripts/run_all.sh
set -eu
D=$(cd "$(dirname "$0")" && pwd)
. "$D/env.sh"
P=tomcat.version=10.1.59,jackson-bom.version=2.21.7,log4j2.version=2.25.5
export WHATIF="${WHATIF:-lr-3:3.5.16 lr-3:3.5.16:$P lr-6:3.5.16:$P lr-3:4.0.10 lr-6:4.0.10}"
bash "$D/00_prepare.sh"
bash "$D/01_build_deps.sh"
bash "$D/02_semgrep.sh"
bash "$D/03_trivy.sh"
bash "$D/versions.sh"
python3 -I "$D/04_summary.py"
python3 -I "$D/cvss31.py" --selftest "$RES/trivy-lr3-jar.json" "$RES/trivy-lr6-jar.json" | tee "$RES/cvss-selftest.txt"
