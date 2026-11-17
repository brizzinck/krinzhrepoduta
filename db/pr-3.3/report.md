---
pagetitle: "ПР 3.3 — Створення додатку із застосуванням ORM технології"
university: "Державний університет інформаційно-комунікаційних технологій"
institute: "Навчально-науковий інститут інформаційних технологій"
department: "Кафедра технологій цифрового розвитку"
report: "з практичної роботи № 3.3 (7)"
discipline: "Технології проектування та адміністрування баз даних та сховищ даних"
topic: "Створення додатку із застосуванням ORM технології"
variant: "Варіант 2 (запити ПР 1.2), база Northwind"
student: ["студент групи ТЦР-32", "Лаврішко Владислав Сергійович"]
teacher-label: "Перевірила:"
teacher: ["Гордієнко Катерина Олександрівна"]
city-year: "Київ – 2026"
---

## Мета і завдання

Мета: навчитися працювати з БД через ORM. Завдання: підключити базу Northwind із другої роботи, згенерувати сутності з готової БД (Database First), створити дві міграції зі зміною моделі та реалізувати запити 3-6 з ПР 1.2 мовою ORM. Вивід у консоль.

## Хід виконання

**Технологія.** Я взяв Hibernate ORM 6.6.58 на Java 21, без Spring. EF Core в умові дано як приклад, тому відповідники такі:

Таблиця 1 — Відповідність EF Core і Hibernate

| EF Core (.NET) | Hibernate (Java) |
|:---------------------|:----------------------------------------|
| `DbContext` | `Session` (її створює `SessionFactory`) |
| `DbSet<T>`, клас сутності | клас з `@Entity`, запит `from T` у `Session` |
| навігаційна властивість `ICollection<T>` | поле `Set<T>` з `@OneToMany` |
| `Scaffold-DbContext` | `hbm2java` (плагін `hibernate-tools-maven`) |
| `Add-Migration`, `Update-Database` | Flyway: файли `V1__…sql`, команда `migrate` |
| LINQ | JPQL |

Копію бази я зробив окремо (`northwind_orm`), бо міграції змінюють схему. Підключення беруться зі змінних середовища. Меню показує всі 13 таблиць Northwind (рис. 1), їх повний вивід лежить у `results/all-tables.txt`.

![Рисунок 1 — Підключення, перегляд таблиці `order details` (з 13)](screenshots/01-connect.png){width=75%}

**Database First.** Генератор `hbm2java` читає схему MySQL і створює класи. У `hibernate.reveng.xml` я залишив усі таблиці, крім службової історії Flyway. З 13 таблиць вийшло 11 сутностей: таблиці-зв'язки `employeeterritories` і `customercustomerdemo` стали `@ManyToMany`, меню читає їх через ці зв'язки. Стовпці стали полями, зовнішні ключі стали зв'язками `@ManyToOne` та `@OneToMany`, складений ключ `order details` став `@EmbeddedId` (рис. 2). Назва `order details` має пробіл, тому в налаштуваннях увімкнено `globally_quoted_identifiers`. Режим `hbm2ddl.auto=validate`: Hibernate лише перевіряє, що схема збігається з сутностями. Повторна генерація в окрему теку відрізняється лише п'ятьма класами з ручними правками (рис. 2). Для стовпців `CHAR` (`CustomerID`, `CustomerTypeID`, `RegionDescription`, `TerritoryDescription`) генератор вказав `VARCHAR`, тому додано `@JdbcTypeCode(SqlTypes.CHAR)`. У двох `@JoinTable` прибрано `updatable=false`, бо Hibernate вимагає однакові `insertable` і `updatable`. Code First працює навпаки: схема створюється з класів міграціями, а Model First починає з візуальної моделі.

```{.java .small}
@Entity
@Table(name="products", catalog="northwind_orm")
public class Products implements java.io.Serializable {
    @Id @GeneratedValue(strategy=IDENTITY)
    @Column(name="ProductID", unique=true, nullable=false)
    public Integer getProductId() { return this.productId; }

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CategoryID")
    public Categories getCategories() { return this.categories; }
    // ... решта полів і set-методи
}
```

![Рисунок 2 — Генерація сутностей](screenshots/02-scaffold.png){width=75%}

**Міграції.** Hibernate не веде версій міграцій, на відміну від `Add-Migration`, тому я взяв Flyway 11.20 у docker. `V1__northwind_schema.sql` це схема 13 таблиць, знята `mysqldump --no-data`: на існуючій базі вона стає базовою версією (`baseline`), а на порожній створює всі 13 таблиць, тобто це повноцінна перша міграція. Зміна моделі: клієнт отримує поле `email`. Спершу сутність `Customers` без `email`, потім із ним: Hibernate при запуску не знаходить стовпець `Email` і зупиняється, `validate` це доводить. Друга міграція `V2__add_customer_email.sql` виконується командою `migrate`, після неї додаток запускається. На рис. 3 зверху вниз: базова версія V1 і запуск зі старою моделлю, помилка `validate` після зміни моделі, `migrate`, запуск зі стовпцем `Email`.

```{.sql .small}
-- V2__add_customer_email.sql
ALTER TABLE customers ADD COLUMN Email VARCHAR(100) NULL;
```

![Рисунок 3 — Міграції: база V1, помилка `validate`, `migrate`, запуск (банер Flyway і меню додатка скорочено до «…»)](screenshots/03-migrations.png){width=82%}

**Запити 3-6** з ПР 1.2 (меню 3-6, рис. 4) записані в JPQL над сутностями, а не над таблицями:

```{.java .small}
// 3. Середня ціна продуктів по категоріях
"select c.categoryName, round(avg(p.unitPrice), 2) from Products p join p.categories c group by c.categoryId, c.categoryName order by c.categoryName"
// 4. Співробітники й клієнти по містах (з'єднання сутностей за містом)
"select e.city, count(distinct e.employeeId), count(distinct c.customerId) from Employees e left join Customers c on c.city = e.city group by e.city order by e.city"
// 5. Дохід від кожного клієнта зі знижкою
"select c.customerId, c.companyName, round(sum(od.unitPrice * od.quantity * (1 - od.discount)), 2) as revenue from OrderDetails od join od.orders o join o.customers c group by c.customerId, c.companyName order by revenue desc"
// 6. Замовлення й співробітник підзапитом у select
"select o.orderId, o.orderDate, o.customers.customerId, (select concat(e.firstName, ' ', e.lastName) from Employees e where e.employeeId = o.employees.employeeId) from Orders o order by o.orderId"
```

| Рисунок 4 — Запити 3 і 4 | Рисунок 4 (продовження) — Запити 5 і 6, таблиці скорочено («…») |
|:-----------|:-----------|
| ![](screenshots/04-queries-a.png) | ![](screenshots/04-queries-b.png) |

Зв'язки `@ManyToOne` ліниві: пункт 7 меню показує проблему N+1 (10 замовлень дають 6 SQL-запитів, а `join fetch` один).

## Результати

Сценарії лежать у `demo/`. Запити дали ті самі дані, що SQL у ПР 1.2: 8 категорій із цінами від 20.25 до 54.01, 5 міст, у запиті 5 клієнти QUICK, ERNSH, SAVEA і 89 рядків, у запиті 6 замовлення від 10248 і 830 рядків.

Код: <https://github.com/brizzinck/krinzhrepoduta/tree/main/db/pr-3.3>.

## Висновки

Я створив консольний додаток на Hibernate ORM для бази Northwind. Сутності для всіх 13 таблиць згенеровано з готової БД інструментом `hbm2java`, типи `CHAR` і два зв'язки довелося виправити вручну. Запити 3-6 записані в JPQL майже без `ON`, бо зв'язки стали полями класів, а результати збігаються з ПР 1.2. Міграції зроблено у Flyway: базова версія V1 і V2 з новим стовпцем `Email`, а `validate` показав розбіжність моделі й схеми. Я опанував `SessionFactory`, `Session`, анотації `@Entity`, `@ManyToOne`, `@ManyToMany`, `@EmbeddedId`, JPQL і відмінності Hibernate від EF Core. Hibernate генерує SQL сам, тому контроль над запитом менший, ніж у Jdbi.
