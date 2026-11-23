# ПР 3: успадкування, перевизначення, порядок конструкторів (Тварина, Собака, Кіт). Повний показ: make show
.RECIPEPREFIX := >
.PHONY: help build exp1 exp2 exp3 cast e1 e1b e1c e2 e3 errors show
T := ../../tools
P := ua.duikt.lavrishko.pr3
CP := -encoding UTF-8 -cp target/classes -d /tmp/out

help: ## список команд
> @grep -E '^[a-z0-9-]+:.*## ' Makefile | sed 's/:.*## /\t/'

build: ## зібрати проєкт Maven у docker (target/classes)
> @$(T)/mvn.sh -q clean package

exp1: build ## дослід 1: @Override і super
> @echo; echo "Крок 1. Дослід 1: перевизначення й super. Очікувано: Dog.describe() і Cat.describe() починаються з тексту Animal"
> @$(T)/java.sh -cp target/classes $(P).Main 1

exp2: build ## дослід 2: порядок конструкторів
> @echo; echo "Крок 2. Дослід 2: порядок конструкторів. Очікувано: [Animal] перед [Dog] і перед [Cat]"
> @$(T)/java.sh -cp target/classes $(P).Main 2

exp3: build ## дослід 3: поле за типом посилання, метод за фактичним типом
> @echo; echo "Крок 3. Дослід 3: поле й метод через посилання Animal. Очікувано: label базове, describe() від Dog і Cat"
> @$(T)/java.sh -cp target/classes $(P).Main 3

cast: build ## приведення вниз: ClassCastException під час виконання
> @echo; echo "Крок 4. Приведення Animal до Dog. Очікувано: для Dog успішно, для Animal ClassCastException"
> @$(T)/java.sh -cp target/classes $(P).CastDemo

e1: build ## E1: похідний клас без super(name)
> @echo; echo "Крок 5. E1: BadDog без super(name). Очікувано: constructor Animal in class Animal cannot be applied to given types"
> @$(T)/javac.sh $(CP) compile-errors/E1_no_super/*.java || true

e1b: build ## E1b: похідний клас без власного конструктора
> @echo; echo "Крок 6. E1b: BadCat без конструктора (неявний super()). Очікувано: та сама помилка"
> @$(T)/javac.sh $(CP) compile-errors/E1b_no_constructor/*.java || true

e1c: build ## E1c: той самий клас з явним super(name)
> @echo; echo "Крок 7. E1c: GoodDog з явним super(name). Очікувано: без помилок"
> @$(T)/javac.sh $(CP) compile-errors/E1c_fixed/*.java && echo "Компіляція без помилок"

e2: build ## E2: Dog d = new Animal(...)
> @echo; echo "Крок 8. E2: Dog d = new Animal(...). Очікувано: incompatible types: Animal cannot be converted to Dog"
> @$(T)/javac.sh $(CP) compile-errors/E2_downcast/*.java || true

e3: build ## E3: Dog d = a без явного приведення
> @echo; echo "Крок 9. E3: Dog d = a без приведення. Очікувано: incompatible types: Animal cannot be converted to Dog"
> @$(T)/javac.sh $(CP) compile-errors/E3_no_cast/*.java || true

errors: e1 e1b e1c e2 e3 ## усі п'ять хибних кейсів

show: exp1 exp2 exp3 cast errors ## усе по черзі
> @echo; echo "Готово."
