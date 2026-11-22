# Підсумок запусків ПР 7

Усі числа взяті зі збережених файлів у results/ скриптом scripts/04_summary.py.

## Semgrep

| ЛР | файлів скановано | правил | знахідок | ERROR | WARNING | INFO | помилок сканера |
|---|---|---|---|---|---|---|---|
| lr3 | 36 | 239 | 5 | 3 | 2 | 0 | 0 |
| lr6 | 19 | 238 | 2 | 2 | 0 | 0 | 0 |

Знахідки:

| ЛР | правило | файл:рядок | серйозність |
|---|---|---|---|
| lr3 | detected-bcrypt-hash | sql/init.sql:38 | ERROR |
| lr3 | tainted-sql-string | src/main/java/ua/duikt/lavrishko/rental/demo/VulnerableDemoController.java:43 | ERROR |
| lr3 | spring-sqli | src/main/java/ua/duikt/lavrishko/rental/demo/VulnerableDemoController.java:46 | WARNING |
| lr3 | tainted-sql-string | src/main/java/ua/duikt/lavrishko/rental/demo/VulnerableDemoController.java:64 | ERROR |
| lr3 | spring-sqli | src/main/java/ua/duikt/lavrishko/rental/demo/VulnerableDemoController.java:67 | WARNING |
| lr6 | detected-bcrypt-hash | src/main/java/ua/duikt/lavrishko/rental/auth/UserStore.java:12 | ERROR |
| lr6 | detected-bcrypt-hash | src/main/java/ua/duikt/lavrishko/rental/auth/UserStore.java:13 | ERROR |

## Trivy: кількість знахідок (рядків звіту, один рядок на пакет і CVE)

| ЛР | ціль | всього | CRITICAL | HIGH | MEDIUM | LOW | унікальних ідентифікаторів |
|---|---|---|---|---|---|---|---|
| lr3 | jar | 89 | 15 | 31 | 29 | 14 | 86 |
| lr3 | pom | 89 | 15 | 31 | 29 | 14 | 86 |
| lr6 | jar | 21 | 7 | 6 | 8 | 0 | 21 |
| lr6 | pom | 21 | 7 | 6 | 8 | 0 | 21 |

## Пакети з вразливостями, lr3 (jar)

| пакет | встановлено | CRIT | HIGH | MED | LOW | найвища виправлена версія в тій самій лінії |
|---|---|---|---|---|---|---|
| org.apache.tomcat.embed:tomcat-embed-core | 10.1.31 | 8 | 16 | 7 | 5 | 10.1.58 |
| org.thymeleaf:thymeleaf | 3.1.2.RELEASE | 3 | 0 | 0 | 0 | 3.1.5.RELEASE |
| org.thymeleaf:thymeleaf-spring6 | 3.1.2.RELEASE | 3 | 0 | 0 | 0 | 3.1.5.RELEASE |
| org.springframework:spring-webmvc | 6.1.14 | 1 | 2 | 8 | 2 | у цій лінії немає |
| com.fasterxml.jackson.core:jackson-databind | 2.17.2 | 0 | 5 | 7 | 0 | у цій лінії немає |
| com.fasterxml.jackson.core:jackson-core | 2.17.2 | 0 | 3 | 1 | 0 | у цій лінії немає |
| org.springframework.boot:spring-boot | 3.3.5 | 0 | 2 | 0 | 0 | 3.3.11 |
| org.springframework.security:spring-security-crypto | 6.3.4 | 0 | 1 | 0 | 0 | 6.3.8 |
| org.springframework:spring-core | 6.1.14 | 0 | 1 | 0 | 1 | у цій лінії немає |
| org.springframework:spring-expression | 6.1.14 | 0 | 1 | 1 | 1 | у цій лінії немає |
| ch.qos.logback:logback-core | 1.5.11 | 0 | 0 | 2 | 4 | 1.5.34 |
| org.apache.logging.log4j:log4j-api | 2.23.1 | 0 | 0 | 1 | 0 | у цій лінії немає |
| org.springframework.boot:spring-boot-autoconfigure | 3.3.5 | 0 | 0 | 1 | 0 | у цій лінії немає |
| org.springframework:spring-context | 6.1.14 | 0 | 0 | 0 | 1 | 6.1.20 |
| org.springframework:spring-web | 6.1.14 | 0 | 0 | 1 | 0 | 6.1.21 |

## Пакети з вразливостями, lr6 (jar)

| пакет | встановлено | CRIT | HIGH | MED | LOW | найвища виправлена версія в тій самій лінії |
|---|---|---|---|---|---|---|
| org.apache.tomcat.embed:tomcat-embed-core | 10.1.55 | 5 | 1 | 1 | 0 | 10.1.59 |
| org.springframework:spring-webmvc | 6.2.19 | 2 | 0 | 0 | 0 | у цій лінії немає |
| com.fasterxml.jackson.core:jackson-databind | 2.21.4 | 0 | 3 | 6 | 0 | 2.21.7 |
| com.fasterxml.jackson.core:jackson-core | 2.21.4 | 0 | 2 | 0 | 0 | 2.21.7 |
| org.apache.logging.log4j:log4j-api | 2.24.3 | 0 | 0 | 1 | 0 | у цій лінії немає |

## Перевірки "що, якщо" (збірка копії з іншим parent)

| копія | всього | CRITICAL | HIGH | MEDIUM | LOW |
|---|---|---|---|---|---|
| lr3-whatif-3.5.16-props | 2 | 2 | 0 | 0 | 0 |
| lr3-whatif-3.5.16 | 21 | 7 | 6 | 8 | 0 |
| lr6-whatif-3.5.16-props | 2 | 2 | 0 | 0 | 0 |
| збірка lr3-whatif-3.5.16-props | BUILD SUCCESS | | | | |
| збірка lr3-whatif-3.5.16 | BUILD SUCCESS | | | | |
| збірка lr3-whatif-4.0.10 | BUILD FAILURE | | | | |
| збірка lr6-whatif-3.5.16-props | BUILD SUCCESS | | | | |
| збірка lr6-whatif-4.0.10 | BUILD FAILURE | | | | |

