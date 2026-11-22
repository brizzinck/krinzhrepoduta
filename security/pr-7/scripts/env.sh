# Спільні змінні ПР 7. Підключається через `. scripts/env.sh`.
# Усі контейнери мають префікс cw-pr7-; чужі контейнери й томи не чіпаємо.
HW=$(cd "$(dirname "${BASH_SOURCE[0]:-$0}")/../../.." && pwd)
S=$(dirname "$HW")
PR7=$HW/security/pr-7
WORK=$S/work/security/pr-7
RES=$PR7/results
LR3=$HW/security/lr-3/app
LR6=$HW/security/lr-6/app
BUILD=$WORK/build
CACHE=$WORK/cache/trivy
RUNLOG=$HW/tools/runlog.sh

SEMGREP_IMG=semgrep/semgrep:1.186.0
TRIVY_IMG=aquasec/trivy:0.76.2
OSV_IMG=ghcr.io/google/osv-scanner:v2.3.8
MVN_IMG=maven:3.9-eclipse-temurin-21
M2_VOL=cw-pr7-m2
UIDGID="$(id -u):$(id -g)"

# Правила Semgrep з публічного реєстру, без входу в акаунт
SEMGREP_CONFIGS="--config p/java --config p/security-audit --config p/secrets --config p/owasp-top-ten"

# Maven у власному контейнері й власному томі кешу
mvn_run() { # ім'я тека аргументи...
  local name=$1 dir=$2; shift 2
  docker run --rm --name "$name" -u "$UIDGID" -e LANG=C.UTF-8 \
    -v "$M2_VOL":/var/maven/.m2 -e MAVEN_CONFIG=/var/maven/.m2 \
    -v "$dir":/w -w /w "$MVN_IMG" mvn -B -Duser.home=/var/maven "$@"
}
