# ПР 7: запуск і показ

Потрібен Docker і інтернет: правила Semgrep беруться з публічного реєстру, Maven завантажує залежності, Trivy завантажує бази вразливостей. Усі команди з теки `security/pr-7`. Вихідні коди ЛР 3 і ЛР 6 (`../lr-3/app`, `../lr-6/app`) підключаються лише для читання; збірки й копії лежать у `target/`, файли репозиторію (`results/` та інші) `make` не змінює.

Перший запуск довший: Maven завантажує залежності (2-3 хвилини), Trivy завантажує бази (близько 1,1 ГБ стиснуто, у `target/cache/trivy` разом 3,2 ГБ, кілька хвилин). Далі бази й кеш Maven (том Docker `cw-pr7-m2`) використовуються повторно.

## Пайплайн

```sh
cd security/pr-7   # шлях відносно кореня репозиторію
make results      # збережені результати, на яких складено звіт (без мережі)
make cvss         # оцінки CVSS з розділу 6 і самоперевірка скрипта (без мережі)
make semgrep-lr3  # SAST: Semgrep над ЛР 3
make semgrep-lr6  # SAST: Semgrep над ЛР 6
make db           # бази Trivy (потрібні для trivy-* і whatif-*, завантажуються самі)
make trivy-lr3    # SCA: збірка app.jar, Trivy над app.jar і pom.xml
make trivy-lr6
make whatif-lr3   # копія ЛР 3 з parent 3.5.16, потім ще з новими Tomcat, Jackson, log4j
make whatif-lr6   # копія ЛР 6 з новими Tomcat, Jackson, log4j
make open         # відкрити report.pdf і слайди
```

`make show` виконує все по черзі й у кінці викликає `clean`, `make help` показує цілі. `make clean` видаляє `target/` (крім кешу баз) і контейнери; `make clean-all` прибирає ще кеш баз і том Maven.

## Кроки

- `make results`: читає `results/versions.txt` і `results/summary.md`.
- `make cvss`: `scripts/cvss31.py` (калькулятор CVSS v3.1 за специфікацією FIRST) рахує вектори таблиць 4 і 5 звіту; потім `--selftest` звіряє його з оцінками, які дав Trivy у `results/trivy-lr3-jar.json` і `trivy-lr6-jar.json`.
- `make semgrep-lr3`, `semgrep-lr6`: `semgrep/semgrep:1.186.0`, `semgrep scan --metrics=off --no-git-ignore` з наборами `p/java`, `p/security-audit`, `p/secrets`, `p/owasp-top-ten` над `src/main`, `src/test` і файлами в корені застосунку; `scripts/show.py semgrep` порівнює знахідки зі збереженими `results/semgrep-lr3.json` і `semgrep-lr6.json`.
- `make trivy-lr3`, `trivy-lr6`: копія застосунку в `target/build/`, `mvn -B -q -DskipTests package` у `maven:3.9-eclipse-temurin-21`, далі `aquasec/trivy:0.76.2` `rootfs` над `app.jar` і `fs` над `pom.xml` (`--offline-scan --scanners vuln`); `scripts/show.py trivy` порівнює з `results/trivy-lr*-jar.json`.
- `make whatif-lr3`, `whatif-lr6`: `scripts/whatif.sh` копіює застосунок, міняє версію parent і додає властивості `tomcat.version=10.1.59`, `jackson-bom.version=2.21.7`, `log4j2.version=2.25.5`, збирає з тестами й сканує новий `app.jar`; порівнює зі збереженими `results/trivy-*-whatif-*.json`.

## Що показати

0. `make db`: `Trivy 0.76.2` і дати оновлення бази вразливостей та Java DB (при першому запуску спершу завантаження баз).
1. `make results`: версії й дати баз зі звіту (база Trivy від `2026-11-22 07:05:02`, Java DB від `2026-11-22 00:57:27`), таблиця Semgrep (5 знахідок у ЛР 3, 2 у ЛР 6) і таблиця Trivy (89 і 21 знахідка).
2. `make cvss`: `базова оцінка 8.2 (High)`, `середовищна оцінка 5.4 (Medium)` для SQL-ін'єкції `search`; `9.4` і `7.4 (High)` для `INSERT`; `7.5` і `5.1 (Medium)` для CVE-2026-24880; `9.8` і `0.0 (None)` для CVE-2025-24813; `7.4` і `0.0 (None)` для CVE-2025-22228; наприкінці `перевірено різних пар вектор-оцінка зі звітів: 56; розбіжностей: 2` (дві бази GHSA на 0,1 вищі, у звіті це зазначено; будь-яка інша кількість розбіжностей зупиняє `make` з повідомленням).
3. `make semgrep-lr3`: `Ran 239 rules on 36 files: 5 findings.`, таблиця знахідок (`tainted-sql-string` на `VulnerableDemoController.java:43` і `:64`, `spring-sqli` на `:46` і `:67`, `detected-bcrypt-hash` у `sql/init.sql:38`) і `Збігається зі збереженим results/semgrep-lr3.json: знахідок 5, файлів 36.`
4. `make semgrep-lr6`: `Ran 238 rules on 19 files: 2 findings.`, дві `detected-bcrypt-hash` в `UserStore.java:12` і `:13`, `Збігається зі збереженим results/semgrep-lr6.json: знахідок 2, файлів 19.`
5. `make trivy-lr3`: `всього 89, C/H/M/L 15/31/29/14, унікальних CVE 86`, головні `tomcat-embed-core 10.1.31: 8/16/7/5`, `thymeleaf 3.1.2.RELEASE: 3/0/0/0`, `Числа збігаються зі звітом.`, `jar і pom.xml збігаються.`
6. `make trivy-lr6`: `всього 21, C/H/M/L 7/6/8/0, унікальних CVE 21`, головні `tomcat-embed-core 10.1.55: 5/1/1/0`, `spring-webmvc 6.2.19: 2/0/0/0`, `jar і pom.xml збігаються.`
7. `make whatif-lr3`: з parent 3.5.16 `Тести: Tests run: 76, Failures: 0, Errors: 0, Skipped: 0`, `всього 21, C/H/M/L 7/6/8/0` і `Числа збігаються зі звітом.`; з трьома властивостями `всього 2, C/H/M/L 2/0/0/0`.
8. `make whatif-lr6`: `Tests run: 18, Failures: 0, Errors: 0, Skipped: 0` і `всього 2, C/H/M/L 2/0/0/0`.

Числа Trivy залежать від бази: якщо вона новіша за звітну (2026-11-22), знахідок може бути більше, тоді скрипт скаже `Числа відрізняються від звіту`. На базі від 2026-11-23 числа збіглися зі звітом.

## Звідки що береться

- `results/`: сирі виводи Semgrep (json, sarif, текст) і Trivy (json, текст), дерева залежностей, перевірки «що, якщо», `summary.md`, `versions.txt` (їх створили `scripts/run_all.sh` і сусідні скрипти).
- `scripts/cvss31.py`: калькулятор CVSS v3.1; `scripts/show.py`: зведення для `make`; `scripts/whatif.sh`: перевірка «що, якщо».
- `slides/`: слайди для короткої презентації; `report.md`, `report.pdf`: звіт.
- Boot 4.0.10 не збирається (пакети Jackson 3), тому в `make` його немає, журнали лежать у `results/build-lr*-whatif-4.0.10.txt`.
