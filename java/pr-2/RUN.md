# ПР 2: запуск і показ

Потрібен лише Docker (JDK 21 і Maven в образі `maven:3.9-eclipse-temurin-21`). Усі команди з теки `java/pr-2` репозиторію. Збірка пишеться в `target/`, файли репозиторію не змінюються.

## Пайплайн

```sh
cd java/pr-2   # тека роботи відносно кореня репозиторію
make build        # mvn clean package
make access       # доступ до полів: помилки javac (E1, E2)
make compile-ok   # робочий код компілюється
make run          # Main: усі п'ять дослідів
make final        # перезапис final: помилки javac (E3, E4)
```

`make show` виконує все по черзі, `make help` показує цілі, `make clean` видаляє `target/`. Цілі після `build` самі запускають збірку, якщо `target/classes` ще немає.

## Кроки

- `build`: `../../tools/mvn.sh clean package`, п'ять класів у пакетах `model` і `app`.
- `access`: `javac` для двох хибних фрагментів. `compile-errors/E1_access_model` читає поля `Account` з іншого класу пакета `model`, `compile-errors/E2_access_app` читає їх з `Main` пакета `app`.
- `compile-ok`: `javac` для всіх файлів `src/main/java`.
- `run`: клас `ua.duikt.lavrishko.pr2.app.Main` запускає досліди 1-5, доступ перевіряють `model/AccessProbe` і `app/ProbeSub`, конструктори й `static` лежать у `model/Account`, `this` показує `model/AccountNoThis`.
- `final`: `compile-errors/E3_final_field/Account.java` (копія `Account` із методом `rename`, що пише в `final`-поле) і `compile-errors/E4_final_local/FinalLocal.java` (присвоєння `final`-змінній).

## Що показати

1. `make access`, E1: `AccessProbeAll.java:7: error: pin has private access in Account`. З іншого класу того самого пакета не компілюється лише `private`.
2. `make access`, E2: три помилки: `pin has private access in Account`, `balance is not public in Account; cannot be accessed from outside package`, `number has protected access in Account`. З іншого пакета (і не з підкласу) доступний лише `public`.
3. `make compile-ok`: без повідомлень і `код завершення: 0`.
4. `make run`:
   - дослід 1: `protected number з підкласу в app: UA-777777`, підклас з іншого пакета бачить `protected`;
   - дослід 2: три конструктори, `Account{id=ID-5, owner=Сидоренко С. С., number=UA-300003, balance=0.0}` (баланс 0.0 дає `this(owner, number, 0.0)`);
   - дослід 3: `з this: Account{id=ID-6, owner=Коваленко К. К., ...}` і `без this: AccountNoThis{owner=null, number=null, balance=0.0}`;
   - дослід 4: `лічильник до створення трьох об'єктів: 6`, потім `Account.getCount() після створення: 9`; `id (своє в кожного): ID-7, ID-8, ID-9`;
   - дослід 5: `final-поле id: ID-10`, `final-змінна limit у методі, dailyLimit(): 5000`.
5. `make final`: `Account.java:61: error: cannot assign a value to final variable id` і `FinalLocal.java:5: error: cannot assign a value to final variable limit`. `final` можна задати один раз.

## Звідки що береться

- `src/main/java/ua/duikt/lavrishko/pr2/model/` і `.../app/`: код роботи.
- `compile-errors/E1..E4`: хибні фрагменти, окремі від збірки.
- `../../tools/*.sh`: запуск `java`, `javac` і Maven у docker.
- `results/`: збережений вивід цих самих команд, на який посилається звіт.
