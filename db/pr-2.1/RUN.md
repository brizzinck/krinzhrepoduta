# ПР 2.1: запуск і показ

Потрібен лише Docker. Усі команди з теки `homework/db/pr-2.1`. База `northwind` має бути свіжа, її створює `make db`.

## Пайплайн

```sh
cd /home/skalse/job/duikt/course_3/students/lavrishko_vladyslav/homework/db/pr-2.1
make db           # свіжа база northwind
make tx           # транзакція і ROLLBACK
make dirty        # дві сесії, READ UNCOMMITTED (близько 8 с)
make committed    # дві сесії, READ COMMITTED (близько 8 с)
make proc         # процедура AddProduct
make trigger      # журнал і тригер
make view         # view із запиту 6
make user         # користувач, REVOKE, DROP USER
```

`make show` виконує все підряд (близько 20 с). `make help` показує всі цілі. Цілі `proc`, `trigger` і `view` йдуть у цьому порядку, бо тригер потребує процедури.

## SQL по кроках

Кожен крок виконує файл із `sql/`. `make` друкує команду, клієнт `mysql -vvv` перед кожним результатом друкує сам запит.

- `make db`: `northwind.sql`, свіжа база.
- `make tx`: `sql/01-transaction.sql`, UPDATE у транзакції і ROLLBACK.
- `make dirty`: `sql/02a-dirty-session-A.sql` і `sql/02b-dirty-session-B.sql`, дві сесії на READ UNCOMMITTED, B бачить незафіксоване.
- `make committed`: `sql/03a-committed-session-A.sql` і `sql/03b-committed-session-B.sql`, ті самі сесії на READ COMMITTED, B не бачить.
- `make proc`: `sql/04-procedure.sql`, процедура `AddProduct` з OUT і її виклик.
- `make trigger`: `sql/05-log-trigger.sql`, `productslog`, тригер AFTER INSERT і перевірка.
- `make view`: `sql/06-view.sql`, view `v_orders_with_employees` і вибірка з неї.
- `make user`: `sql/07a` ... `sql/07g`, прибрати старого користувача, створити з грантами, запити під ним, REVOKE, заборонений CREATE, DROP USER, вхід заборонено.

## Що показати

1. `make tx`. Три таблиці: `Obere Str. 57`, потім `вул. Тестова, 1` (UPDATE у транзакції), потім знову `Obere Str. 57` (ROLLBACK). Зміну відкотили, дані повернулись.
2. `make dirty`. Сесія A міняє адресу й тримає транзакцію 6 с, сесія B читає. `A: після UPDATE | вул. Тестова, 1`, через 2 с `B: поки A не завершила | вул. Тестова, 1`, потім `Obere Str. 57` в обох. Брудне читання: на `READ UNCOMMITTED` B бачить незафіксоване.
3. `make committed`. Той самий дослід: `B: поки A не завершила | Obere Str. 57`. На `READ COMMITTED` незафіксоване не видно.
4. `make proc`. `new_product_id 78` і зверху списку `78 | Test Product A | 25.5000 | 100`. Процедура вставила рядок і повернула ключ через `OUT`.
5. `make trigger`. `trg_products_ai | AFTER | INSERT | products`, після виклику процедури в `productslog` один рядок (продукт 79), після INSERT на два рядки ще 80 і 81, усього три. Тригер `FOR EACH ROW` спрацював на кожен рядок.
6. `make view`. `10248 ... Steven Buchanan`, `10249 ... Michael Suyama`, `10250 ... Margaret Peacock` і `rows_in_view 830`. View це збережений запит без власних даних.
7. `make user`. Спершу `GRANT SELECT, INSERT, UPDATE, DELETE, CREATE ON northwind.*`, потім таблиця `2 | changed` (усі п'ять привілеїв працюють). Після `REVOKE` списку `CREATE` немає, `ERROR 1142 CREATE command denied`, але INSERT працює (рядки 2 і 3). Далі `users_left 0` і `ERROR 1045 Access denied`. Права видаються й забираються, користувач видалений.

Дві сесії вручну, у двох терміналах (A запускай першим):

```sh
./mysql.sh -t -vvv -n northwind < sql/02a-dirty-session-A.sql
./mysql.sh -t -vvv -n northwind < sql/02b-dirty-session-B.sql
```

Код у `sql/` (по кроках) і в поданих файлах: `01_transaction.sql`, `01a_session_A.sql`, `02_procedure.sql`, `03_log_trigger.sql`, `04_view.sql`, `05a_create_user.sql` ... `05f_login_denied.sql`.

## Звідки що береться

`make` друкує кожну команду завантаження бази, наприклад `./mysql.sh < schema.sql`. `mysql.sh` запускає клієнт `mysql` у контейнері `duikt-mysql`, `mysql-up.sh` піднімає цей контейнер.

- `northwind.sql`: база `northwind`, Northwind викладачки (дамп `MY_SQL Labwork2.sql` з Classroom, ПР 1.2) і два виправлення в кінці файла: знижки з `LabWork2.sql` і `AUTO_INCREMENT` для `products`.

`tidy.sh` лише скорочує вивід `mysql -vvv`: коментар із файла друкується одним рядком, запит після `mysql>`.
