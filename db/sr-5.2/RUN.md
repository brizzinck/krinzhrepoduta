# СР 5.2: запуск і показ

Потрібен лише Docker. Усі команди з теки `homework/db/sr-5.2`. Другий термінал не потрібен: сеанси блокування скрипт запускає сам.

## Пайплайн

```sh
cd /home/skalse/job/duikt/course_3/students/lavrishko_vladyslav/homework/db/sr-5.2
make db         # база car_rental у вихідному стані
make slow       # повільний журнал і digest
make lock       # блокування рядка, близько 10 секунд
make binlog     # двійковий журнал
make errlog     # журнал помилок
```

`make show` виконує все підряд (близько 15 секунд). `make help` показує всі цілі. Скрипт міняє лише змінні сервера через `SET GLOBAL` і повертає їх назад, дані `car_rental` лишаються без змін.

## SQL по кроках

- `make slow`: `sql/01-slow-enable.sql`, `sql/02-slow-workload.sql`, `sql/03-slow-read-slow-log.sql`, `sql/04-slow-read-digest.sql`, `sql/05-slow-restore.sql`, `sql/06-slow-check-variables.sql`, поріг 0, запит для спостереження, читання `slow_log` і digest, повернення налаштувань.
- `make lock`: `sql/07-lock-session-a.sql`, `sql/08-lock-session-b.sql`, `sql/09-lock-waits.sql`, `sql/10-lock-data-locks.sql`, два сеанси на одному рядку й два запити про очікування.
- `make binlog`: `sql/11-binlog-status.sql`, `sql/12-binlog-update.sql`, `sql/13-binlog-events.sql`, `sql/14-binlog-variables.sql`, `sql/15-binlog-revert.sql`, позиція журналу, зміна, події `Update_rows`, зворотна зміна.
- `make errlog`: `sql/16-errlog-start-time.sql` ... `sql/22-errlog-verbosity-2.sql`, верболізність 2 і 3, невдалий вхід, запис у `error_log`.
- `make sql`: друкує весь вміст `sql/`. `demo.sh` друкує кожну команду з файлом SQL, а `-vvv` показує сам запит; файли `02`, `07`, `08`, `19` скрипт друкує через `cat`.

## Що показати

1. `make slow`. Таблиця `slow_log`: три рядки `rows_sent 5`, `rows_examined 50`, час менше 1 мс. Digest: `cnt 3`, `rows_examined 150`. В кінці `slow_query_log OFF`, `long_query_time 10.000000`, `log_output FILE`. Поріг 0 фіксує кожен запит, налаштування повернуто.
2. `make lock`. Не чіпай термінал 10 секунд. У `sys.innodb_lock_waits`: `locked_table: car_rental.cars`, `wait_age_secs: 2`, `waiting_query: UPDATE cars SET status = status WHERE car_id = 1`, `blocking_query: DO SLEEP(10)`. У `data_locks` рядки `X,REC_NOT_GAP GRANTED` і `X,REC_NOT_GAP WAITING`. Так видно, хто кого блокує. Остання лінія: `Сеанс A завершився ROLLBACK, сеанс B виконався`.
3. `make binlog`. Події `Anonymous_Gtid`, `Query` (`BEGIN`), `Table_map` (`car_rental.cars`), `Update_rows`, `Xid` (`COMMIT`). Нижче `binlog_format ROW` і `log_bin ON`. Двійковий журнал фіксує зміни даних. Ім'я файла й позиції щоразу інші.
4. `make errlog`. Спершу `2`, `ERROR 1045` і `login_errors 0`: при verbosity 2 невдалий вхід не пишеться. Після `SET GLOBAL log_error_verbosity = 3` той самий вхід дає рядок `prio: Note`, `error_code: MY-010926`, `Access denied for user 'root'@'localhost'`. Верболізність скрипт повертає на 2.

Код: `demo.sh` (функції `slow`, `lock`, `binlog`, `errlog`), `sql/` (SQL кожного кроку, запит для спостереження в `sql/02-slow-workload.sql`).

## Звідки що береться

`make` друкує кожну команду завантаження бази, наприклад `./mysql.sh < schema.sql`. `mysql.sh` запускає клієнт `mysql` у контейнері `duikt-mysql`, `mysql-up.sh` піднімає цей контейнер.

- `schema.sql`, `seed.sql`: база `car_rental`, копія з ПР 1.1.
