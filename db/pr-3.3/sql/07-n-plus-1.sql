-- Демонстрація N+1: меню пункт 7. Звідки: src/main/java/northwind/Queries.java, метод nPlusOne(). Сценарій: demo/03-n-plus-1.txt.
-- Підрахунок запитів дає factory.getStatistics().getPrepareStatementCount(). Параметр ? у limit = 10 (setMaxResults).

-- Частина 1. Ліниве завантаження (LAZY): 1 запит списку замовлень + по одному запиту на кожного різного співробітника.
-- JPQL:  from Orders o order by o.orderId   (далі o.getEmployees().getLastName() для кожного замовлення)
-- Результат у програмі: "Ліниве завантаження: SQL-запитів 6".
-- Запит 1 з 6: список замовлень.
select
    o1_0.`OrderID`,
    o1_0.`CustomerID`,
    o1_0.`EmployeeID`,
    o1_0.`Freight`,
    o1_0.`OrderDate`,
    o1_0.`RequiredDate`,
    o1_0.`ShipAddress`,
    o1_0.`ShipCity`,
    o1_0.`ShipCountry`,
    o1_0.`ShipName`,
    o1_0.`ShipPostalCode`,
    o1_0.`ShipRegion`,
    o1_0.`ShippedDate`,
    o1_0.`ShipVia`
from
    `northwind_orm`.`orders` o1_0
order by
    o1_0.`OrderID`
limit
    ?;

-- Запити 2-6 з 6: Hibernate довантажує співробітника окремо для кожного з 5 різних EmployeeID серед перших 10 замовлень
-- (повторні співробітники беруться з кешу сесії). У кожному ? = EmployeeID, значення різні. Текст однаковий:
select
    e1_0.`EmployeeID`,
    e1_0.`Address`,
    e1_0.`BirthDate`,
    e1_0.`City`,
    e1_0.`Country`,
    e1_0.`ReportsTo`,
    e1_0.`Extension`,
    e1_0.`FirstName`,
    e1_0.`HireDate`,
    e1_0.`HomePhone`,
    e1_0.`LastName`,
    e1_0.`Notes`,
    e1_0.`Photo`,
    e1_0.`PhotoPath`,
    e1_0.`PostalCode`,
    e1_0.`Region`,
    e1_0.`Title`,
    e1_0.`TitleOfCourtesy`
from
    `northwind_orm`.`employees` e1_0
where
    e1_0.`EmployeeID`=?;

-- Частина 2. join fetch: співробітники приходять разом із замовленнями одним запитом.
-- JPQL:  from Orders o join fetch o.employees order by o.orderId
-- Результат у програмі: "join fetch: SQL-запитів 1".
select
    o1_0.`OrderID`,
    o1_0.`CustomerID`,
    e1_0.`EmployeeID`,
    e1_0.`Address`,
    e1_0.`BirthDate`,
    e1_0.`City`,
    e1_0.`Country`,
    e1_0.`ReportsTo`,
    e1_0.`Extension`,
    e1_0.`FirstName`,
    e1_0.`HireDate`,
    e1_0.`HomePhone`,
    e1_0.`LastName`,
    e1_0.`Notes`,
    e1_0.`Photo`,
    e1_0.`PhotoPath`,
    e1_0.`PostalCode`,
    e1_0.`Region`,
    e1_0.`Title`,
    e1_0.`TitleOfCourtesy`,
    o1_0.`Freight`,
    o1_0.`OrderDate`,
    o1_0.`RequiredDate`,
    o1_0.`ShipAddress`,
    o1_0.`ShipCity`,
    o1_0.`ShipCountry`,
    o1_0.`ShipName`,
    o1_0.`ShipPostalCode`,
    o1_0.`ShipRegion`,
    o1_0.`ShippedDate`,
    o1_0.`ShipVia`
from
    `northwind_orm`.`orders` o1_0
join
    `northwind_orm`.`employees` e1_0
        on e1_0.`EmployeeID`=o1_0.`EmployeeID`
order by
    o1_0.`OrderID`
limit
    ?;
