# ПР 3: запуск і показ

Потрібен лише Docker. Усі команди з теки `java/pr-3` репозиторію. Збірка пише лише в `target/`, хибні кейси `javac` нічого не створюють.

## Пайплайн

```sh
cd java/pr-3
make build   # зібрати проєкт (Maven у docker)
make exp1    # дослід 1: @Override і super
make exp2    # дослід 2: порядок конструкторів
make exp3    # дослід 3: поле за типом посилання, метод за фактичним типом
make cast    # приведення вниз, ClassCastException
make e1      # BadDog без super(name)
make e1b     # BadCat без конструктора
make e1c     # GoodDog з явним super(name)
make e2      # Dog d = new Animal(...)
make e3      # Dog d = a без приведення
```

`make errors` виконує `e1`..`e3` підряд, `make show` виконує все по черзі. `make help` показує всі цілі.

## Кроки

- `make build`: `../../tools/mvn.sh -q clean package`.
- `make exp1`, `exp2`, `exp3`: клас `ua.duikt.lavrishko.pr3.Main` з аргументом 1, 2 або 3; класи `Animal`, `Dog`, `Cat`.
- `make cast`: клас `CastDemo`.
- `make e1`..`make e3`: `javac` компілює файли з `compile-errors/E1_no_super`, `E1b_no_constructor`, `E1c_fixed`, `E2_downcast`, `E3_no_cast` проти `target/classes`.

## Що показати

1. `make exp1`: `Dog.describe():    Тварина Рекс. Собака Рекс гавкає`. Похідна версія починається з тексту базової через `super.describe()`.
2. `make exp2`: `[Animal] конструктор, ім'я: Рекс`, потім `[Dog] конструктор`. Спершу конструктор базового класу.
3. `make exp3`: `pet.label (тип посилання Animal): базове`, а `pet.describe()` дає версію `Dog` чи `Cat`. Поле береться за типом посилання, метод за фактичним типом.
4. `make cast`: для об'єкта `Dog` приведення вдале, для об'єкта `Animal` `class ua.duikt.lavrishko.pr3.Animal cannot be cast to class ua.duikt.lavrishko.pr3.Dog`.
5. `make e1`: `error: constructor Animal in class Animal cannot be applied to given types;` (`required: String`, `found:    no arguments`).
6. `make e1b`: та сама помилка для класу без власного конструктора.
7. `make e1c`: `Компіляція без помилок`. З явним `super(name)` клас компілюється.
8. `make e2`: `error: incompatible types: Animal cannot be converted to Dog`.
9. `make e3`: те саме повідомлення, бо без явного приведення компілятор не пропускає `Dog d = a`. Наприкінці `Готово.`

## Звідки що береться

- `src/main/java/ua/duikt/lavrishko/pr3`: `Animal` (єдиний конструктор з параметром, поле `label`, метод `describe()`), `Dog`, `Cat`, `Main`, `CastDemo`.
- `compile-errors/`: п'ять хибних кейсів. `run.sh` у цій теці й у `compile-errors/` записують вивід у `results/`, `make` лише друкує його.
- `diagrams/`: схеми наслідування й порядку конструкторів (Graphviz `.dot`, готові `.png` і `.svg`).
- `../../tools/mvn.sh`, `../../tools/java.sh`, `../../tools/javac.sh`: Maven, `java` і `javac` (JDK 21) у docker.
