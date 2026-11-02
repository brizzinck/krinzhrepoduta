# DUIKT coursework, year 3, semester 5 (2026/2027)

Vladyslav Lavrishko, group ТЦР-32, State University of Information and Communication Technologies (ДУІКТ).

Each assignment has its own directory `<course>/<code>/`, e.g. `java/lr-5` (lab 5) or `db/pr-1.1` (practical 1.1): source code, the report `report.md`, input data and run results. Codes: `pr` practical work, `lr` lab, `pz` practical session, `sr` self-study.

| Directory | Course | Teacher |
|---|---|---|
| `java/` | Конструювання програмного забезпечення JAVA | Ніщеменко Д. О. |
| `db/` | Технології проектування та адміністрування БД та сховищ даних | Гордієнко К. О. |
| `arch/` | Архітектура ПЗ | Гавор А. С. |
| `security/` | Безпека розробки програмного забезпечення | Ананченко О. Є. |
| `digital_tech/` | Цифрові технології | Поперешняк С. В. |
| `yatpz/` | ЯТПЗ | Читулян В. О. |
| `iot/` | Технології Інтернету речей | Завацький В. О. |
| `mobile_dev/` | Програмування мобільних пристроїв | Олейніков І. А. |

## Assignments

- **Java:** [Lab 2](java/lr-2/), [Lab 3](java/lr-3/), [Lab 4](java/lr-4/), [Lab 5](java/lr-5/), [Practical 1](java/pr-1/), [Practical 2](java/pr-2/), [Practical 3](java/pr-3/), [Practical 5](java/pr-5/), [Session 4](java/pz-4/), [Self-study 1](java/sr-1/)
- **Databases:** [Practical 1.1](db/pr-1.1/), [Practical 1.2](db/pr-1.2/), [Self-study 5.1](db/sr-5.1/)
- **Software architecture:** [Session 1](arch/pz-1/), [Session 2](arch/pz-2/), [Session 3](arch/pz-3/), [Session 4](arch/pz-4/)
- **Secure development:** [Lab 1](security/lr-1/), [Lab 3](security/lr-3/)
- **Digital technologies:** [Practical 2](digital_tech/pr-2/), [Practical 2.1](digital_tech/pr-2.1/), [Practical 2.2](digital_tech/pr-2.2/), [Practical 2.3](digital_tech/pr-2.3/), [Practical 2.4](digital_tech/pr-2.4/)
- **Software testing (ЯТПЗ):** [Lab 2](yatpz/lr-2/), [Lab 3](yatpz/lr-3/), [Practical 1](yatpz/pr-1/)
- **Mobile development:** [Lab 1](mobile_dev/lr-1/)
- **IoT:** [Session 1](iot/pz-1/)

## Build

Report PDFs/DOCX and archives are not committed; build them with:

```sh
./build.sh              # all reports
./build.sh java/lr-5    # one report
./build.sh archives     # arch project archives
```

Requires pandoc and Google Chrome (for PDF). Java, .NET and MySQL run in docker: `tools/java.sh`, `tools/mvn.sh`, `tools/dotnet.sh`.
