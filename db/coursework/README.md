# Micromobility rental (coursework)

Database coursework «Прокат транспорту мікромобільності»: a small admin application for renting e-scooters and
e-bikes of several operators, built with an ORM.

> Навчальний проєкт. Дані операторів імітовані, проєкт не пов'язаний з Bolt, Jet і Zelektra.

All operator names, phones, e-mails, customers and rides are simulated; there is no real personal data.

## Stack

Java 21, Spring Boot 3.5, Spring Data JPA (Hibernate 6.6), Thymeleaf with Bootstrap (webjar, no CDN),
MySQL 8.4, Flyway. Everything runs in Docker; nothing is installed on the host.

## Run

```
make up         # build and start MySQL + the application, wait for the admin, print the URL
make help       # all targets
```

Targets: `up`, `down` (stop, keep data), `reset` (drop data, fresh demo data), `logs`, `ps`, `db` (MySQL shell with the
compose credentials), `check`, `test`, `diagrams`, `open` (browser), `clean` (drop data, remove `app/target`).
They work from anywhere: `make -C <this directory> <target>`.

The same with Docker Compose directly:

```
docker compose up -d --build        # MySQL + the application, compose project "micromobility"
docker compose down -v              # stop and drop the data (a clean start gives fresh active rides)
```

The admin opens at http://127.0.0.1:18480/ (the host port can be changed: `APP_PORT=8081 make up`).
The database is not published on the host. The schema (`V1__schema.sql`) and the demo data (`V2__seed.sql`) are
applied by Flyway at the start; Hibernate runs with `ddl-auto=validate` and only checks the mapping.

## Check

```
make check      # fresh volume, then all checks; exit code 0 if everything passed (same as ./check.sh)
make test       # unit tests of the billing formula (Maven in Docker)
```

Parts: stack and migrations; `sql/checks.sql` (constraint tests on a scratch database); CRUD, ride rules, payments
and refused cases through the HTTP forms; every report page and the vehicle battery filter (whole ordered result:
values and order, with and without date filters) against its SQL. Outputs are written to `results/`.
Without make: `./check.sh` and `cd app && ../../../tools/mvn.sh -q test`.

## What is where

| Path | Content |
| --- | --- |
| `app/src/main/resources/db/migration/` | `V1__schema.sql` (7 tables, constraints, indexes), `V2__seed.sql` (demo data) |
| `app/src/main/java/micromobility/domain/` | entities, enums with codes and Ukrainian labels, attribute converters |
| `app/src/main/java/micromobility/repository/` | Spring Data repositories: derived methods, JPQL/HQL, one native query |
| `app/src/main/java/micromobility/service/` | `Billing`, transactional services with the business rules |
| `app/src/main/java/micromobility/web/`, `templates/` | controllers, forms, Thymeleaf pages |
| `Makefile` | the targets above |
| `sql/` | `queries.sql` (reports R1-R7 as SQL), `checks.sql` (constraint tests) |
| `results/`, `screenshots/` | outputs of `check.sh`, page screenshots |

## Features

- Full CRUD: operators, tariffs, stations, vehicles, customers.
- Rides: start, search (R2), receipt, finish at a chosen station, cancel. Payments: wallet history, top-up, refund;
  the ledger is append-only. Identity check through Дія is simulated.
- Reports R1-R7 with filters and aggregation (`/reports`; R2 is the rides list).
- Constraint errors are shown on the forms as Ukrainian messages, chosen by the constraint name.
