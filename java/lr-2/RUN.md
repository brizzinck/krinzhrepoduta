# ЛР 2: запуск і показ

Потрібен лише Docker. Усі команди з теки `java/lr-2` репозиторію. Збірка пише лише в `target/`, спроби, що не компілюються, нічого не створюють.

## Пайплайн

```sh
cd java/lr-2
make build   # зібрати проєкт (Maven у docker)
make run     # Main: підсумок, перелік, спроби порушити стан
make e1      # student.id = 99
make e2      # student.attach(group)
make e3      # group.students.add(...)
make e4      # присвоєння final-полю
make e5      # class Sneaky extends Student
make ok      # робочий код компілюється без помилок
```

`make errors` виконує `e1`..`e5` підряд, `make show` виконує `run`, `errors` і `ok`. `make help` показує всі цілі.

## Кроки

- `make build`: `../../tools/mvn.sh -q clean package`, класи в `target/classes`.
- `make run`: клас `ua.duikt.lavrishko.group.app.Main`, працює з `Student` і `StudyGroup` із пакета `domain`.
- `make e1`..`make e5`: `javac` компілює файли з `compile-errors/E1_id_access`, `E2_service_op`, `E3_internal_list`, `E4_final_assign`, `E5_subclass` проти `target/classes`. Кожен файл містить хибний код і не компілюється.
- `make ok`: `javac` компілює весь `src/main/java`.

## Що показати

1. `make run`: `Група ТЦР-32, студентів: 4, середній бал: 62.50`, після зміни оцінки `середній бал: 82.50`, після видалення студента `середній бал: 86.67`. Спроби змінити список дають `UnsupportedOperationException`, оцінка 105 відхилена (`оцінка 105 поза межами 0..100`), `addStudent повернув false`. Останній рядок: `Останній Студент: 7 (відхилені студенти номерів не витратили)`.
2. `make e1`: `error: id has private access in Student`. Номер змінити не можна.
3. `make e2`: `error: attach(StudyGroup) is not public in Student; cannot be accessed from outside package`. Службова операція закрита для пакета `app`.
4. `make e3`: `error: students has private access in StudyGroup` (два рядки, разом `2 errors`). Внутрішній список недоступний.
5. `make e4`: `error: cannot assign a value to final variable fullName`.
6. `make e5`: `error: cannot inherit from final Student`. Клас `final`, тому нащадок не може перевизначити `getGrade()` чи `isInGroup()`.
7. `make ok`: `Компіляція без помилок`. Наприкінці `make show` друкує `Готово.`

## Звідки що береться

- `src/main/java/ua/duikt/lavrishko/group/domain`: `Student` (номер зі `static`, `final`-поля, два конструктори, `attach`/`detach` без модифікатора) і `StudyGroup` (приватний список, `List.copyOf`, середній бал за елементами).
- `src/main/java/ua/duikt/lavrishko/group/app/Main.java`: сценарій показу.
- `compile-errors/`: п'ять хибних фрагментів. `run.sh` у цій теці записує їхній вивід у `results/`, `make` лише друкує його.
- `../../tools/mvn.sh`, `../../tools/java.sh`, `../../tools/javac.sh`: Maven, `java` і `javac` (JDK 21) у docker.
