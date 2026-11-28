-- Версія 3 з 3: Hibernate ORM (ПР 3.3). Звідки: pr-3.3/src/main/java/northwind/Queries.java, запити 3-6 на JPQL.
-- Це інша база (northwind_orm) і інші запити, ніж у 01 і 02: JPQL пишеться над сутностями, а SQL будує Hibernate.
-- Порівнюваний у звіті запит: запит 3 (JPQL без ON: з'єднання бере зі зв'язку p.categories) і запит 6 (рис. 2, SQL із підзапитом).
-- SQL знято командою SHOW_SQL=true ./run.sh < demo/04-show-sql.txt (у pr-3.3), псевдоніми c1_0, p1_0, o1_0, e1_0 дописує бібліотека.

-- Hibernate: запит 3. JPQL з src/main/java/northwind/Queries.java, метод avgPriceByCategory().
-- Параметрів немає. Сценарій: demo/02-queries-a.txt (меню пункт 3).
--
-- JPQL:
--   select c.categoryName, round(avg(p.unitPrice), 2)
--   from Products p join p.categories c
--   group by c.categoryId, c.categoryName
--   order by c.categoryName
--
-- SQL, який Hibernate надсилає в MySQL (знято командою  SHOW_SQL=true ./run.sh < demo/04-show-sql.txt  і подібними;
-- псевдоніми c1_0, p1_0 дописує бібліотека):
select
    c1_0.`CategoryName`,
    round(avg(p1_0.`UnitPrice`), 2)
from
    `northwind_orm`.`products` p1_0
join
    `northwind_orm`.`categories` c1_0
        on c1_0.`CategoryID`=p1_0.`CategoryID`
group by
    c1_0.`CategoryID`,
    c1_0.`CategoryName`
order by
    c1_0.`CategoryName`;

-- Hibernate: запит 4. JPQL з src/main/java/northwind/Queries.java, метод employeesAndCustomersByCity().
-- Параметрів немає. Сценарій: demo/02-queries-a.txt (меню пункт 4).
--
-- JPQL:
--   select e.city, count(distinct e.employeeId), count(distinct c.customerId)
--   from Employees e left join Customers c on c.city = e.city
--   group by e.city
--   order by e.city
--
-- SQL, який Hibernate надсилає в MySQL (знято командою  SHOW_SQL=true ./run.sh < demo/04-show-sql.txt  і подібними;
-- псевдоніми c1_0, p1_0 дописує бібліотека):
select
    e1_0.`City`,
    count(distinct e1_0.`EmployeeID`),
    count(distinct c1_0.`CustomerID`)
from
    `northwind_orm`.`employees` e1_0
left join
    `northwind_orm`.`customers` c1_0
        on c1_0.`City`=e1_0.`City`
group by
    e1_0.`City`
order by
    e1_0.`City`;

-- Hibernate: запит 5. JPQL з src/main/java/northwind/Queries.java, метод revenueByCustomer().
-- Параметрів немає; перші 10 рядків відбирає Java при друку, у SQL limit немає. Сценарій: demo/02-queries-b.txt (меню пункт 5).
--
-- JPQL:
--   select c.customerId, c.companyName, round(sum(od.unitPrice * od.quantity * (1 - od.discount)), 2) as revenue
--   from OrderDetails od join od.orders o join o.customers c
--   group by c.customerId, c.companyName
--   order by revenue desc
--
-- SQL, який Hibernate надсилає в MySQL (знято командою  SHOW_SQL=true ./run.sh < demo/04-show-sql.txt  і подібними;
-- псевдоніми c1_0, p1_0 дописує бібліотека):
select
    c1_0.`CustomerID`,
    c1_0.`CompanyName`,
    round(sum(((od1_0.`UnitPrice`*od1_0.`Quantity`)*(1-od1_0.`Discount`))), 2)
from
    `northwind_orm`.`order details` od1_0
join
    `northwind_orm`.`orders` o1_0
        on o1_0.`OrderID`=od1_0.`OrderID`
join
    `northwind_orm`.`customers` c1_0
        on c1_0.`CustomerID`=o1_0.`CustomerID`
group by
    c1_0.`CustomerID`,
    c1_0.`CompanyName`
order by
    3 desc;

-- Hibernate: запит 6. JPQL з src/main/java/northwind/Queries.java, метод ordersWithEmployee().
-- Параметрів немає; перші 10 рядків відбирає Java при друку. Сценарії: demo/02-queries-b.txt, demo/04-show-sql.txt (меню пункт 6).
--
-- JPQL:
--   select o.orderId, o.orderDate, o.customers.customerId,
--          (select concat(e.firstName, ' ', e.lastName) from Employees e where e.employeeId = o.employees.employeeId)
--   from Orders o
--   order by o.orderId
--
-- SQL, який Hibernate надсилає в MySQL (знято командою  SHOW_SQL=true ./run.sh < demo/04-show-sql.txt  і подібними;
-- псевдоніми c1_0, p1_0 дописує бібліотека):
select
    o1_0.`OrderID`,
    o1_0.`OrderDate`,
    o1_0.`CustomerID`,
    (select
        concat(e1_0.`FirstName`, ' ', e1_0.`LastName`)
    from
        `northwind_orm`.`employees` e1_0
    where
        e1_0.`EmployeeID`=o1_0.`EmployeeID`)
from
    `northwind_orm`.`orders` o1_0
order by
    o1_0.`OrderID`;
