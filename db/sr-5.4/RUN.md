# СР 5.4: запуск і показ

Потрібен лише Docker. Усі команди з теки `homework/db/sr-5.4`. Звіт порівнює ПР 3.1 (JDBC), ПР 3.2 (Jdbi) і ПР 3.3 (Hibernate); цифри беруться з їхніх тек, база `northwind_orm`.

## Пайплайн

```sh
cd /home/skalse/job/duikt/course_3/students/lavrishko_vladyslav/homework/db/sr-5.4
make queries   # порівнювані запити: JDBC, Jdbi, Hibernate
make loc       # рядки ручного коду
make nplus1    # Hibernate: N+1 (готує northwind_orm на версії 2)
make sql       # SQL, який генерує Hibernate
make apps      # усі три додатки по черзі (довго)
```

`make show` виконує `queries`, `loc`, `nplus1` і `sql`. `make help` показує всі цілі. `make apps` викликає `make show` з `pr-3.1`, `pr-3.2` і `pr-3.3`.

## SQL по кроках

- `make queries`: `sql/01-jdbc.sql`, `sql/02-jdbi.sql`, `sql/03-hibernate-jpql-and-generated.sql`, порівнювані запити в трьох версіях (JDBC і Jdbi з одним текстом SQL, Hibernate: JPQL і згенерований SQL).
- `make nplus1`: `sql/04-n-plus-1.sql`, SQL ліниво завантаження (1 + 5 запитів) і `join fetch` (1 запит).
- `make sql`: той самий запит 6 у Hibernate, див. `sql/03-hibernate-jpql-and-generated.sql`.
- `make showsql`: друкує весь вміст `sql/`.

## Що показати

1. `make loc`. Чотири підсумки: `218 total` (JDBC), `217 total` (Jdbi), `181 total` (Hibernate вручну), `1761 total` (згенеровані сутності). Це ручний код доступу до даних. Прямо порівнювати не можна: у ПР 3.1 CRUD для 10 таблиць, у 3.2 для 3, у 3.3 лише читання.
2. `make nplus1`. `Ліниве завантаження: SQL-запитів 6` і `join fetch: SQL-запитів 1`. Ліниве завантаження Hibernate дає N+1: один запит списку й по одному на кожного з 5 співробітників.
3. `make sql`. Рядок `Hibernate:`, під ним відформатований `select` у кілька рядків з вкладеним підзапитом `concat(e1_0...)` і `from` таблиці `orders` бази `northwind_orm` із псевдонімом `o1_0`. Структуру запиту задав я, текст пише бібліотека, і в ньому видно її псевдоніми `o1_0` і `e1_0`.
4. Таблиця порівняння (зручність, контроль над SQL, зв'язки, міграції, ризик N+1) лежить у `report.pdf`, таблиця 1.

Код: порівнювані запити `pr-3.1/src/main/java/rental/QueryDao.java`, `pr-3.2/src/main/java/rental/ReportDao.java`, `pr-3.3/src/main/java/northwind/Queries.java`.

## Звідки що береться

`make` друкує кожну команду завантаження бази, наприклад `./mysql.sh < schema.sql`. `mysql.sh` запускає клієнт `mysql` у контейнері `duikt-mysql`, `mysql-up.sh` піднімає цей контейнер.

- `pr-3.1/`: копія ПР 3.1 із зібраним jar.
- `pr-3.2/`: копія ПР 3.2 із зібраним jar.
- `pr-3.3/`: копія ПР 3.3 із зібраним jar.
