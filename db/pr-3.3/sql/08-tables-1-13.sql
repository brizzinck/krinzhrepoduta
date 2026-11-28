-- Показати таблицю: меню пункт 1, таблиці 1-13 (перші 10 рядків кожної). Сценарій: demo/05-all-tables.txt (make tables).
-- Звідки: src/main/java/northwind/Queries.java, метод list(int table): JPQL збирається з масивів from і fields,
-- далі setMaxResults(10). Параметр ? у limit = 10. Нижче для кожної таблиці JPQL і SQL, який згенерував Hibernate.

-- Таблиця 1: categories
-- JPQL: select c.categoryId, c.categoryName, c.description from Categories c order by 1, 2
select
    c1_0.`CategoryID`,
    c1_0.`CategoryName`,
    c1_0.`Description`
from
    `northwind_orm`.`categories` c1_0
order by
    1,
    2
limit
    ?;

-- Таблиця 2: products
-- JPQL: select c.productId, c.productName, c.unitPrice, c.unitsInStock from Products c order by 1, 2
select
    p1_0.`ProductID`,
    p1_0.`ProductName`,
    p1_0.`UnitPrice`,
    p1_0.`UnitsInStock`
from
    `northwind_orm`.`products` p1_0
order by
    1,
    2
limit
    ?;

-- Таблиця 3: customers
-- JPQL: select c.customerId, c.companyName, c.city, c.country from Customers c order by 1, 2
select
    c1_0.`CustomerID`,
    c1_0.`CompanyName`,
    c1_0.`City`,
    c1_0.`Country`
from
    `northwind_orm`.`customers` c1_0
order by
    1,
    2
limit
    ?;

-- Таблиця 4: employees
-- JPQL: select c.employeeId, c.firstName, c.lastName, c.city from Employees c order by 1, 2
select
    e1_0.`EmployeeID`,
    e1_0.`FirstName`,
    e1_0.`LastName`,
    e1_0.`City`
from
    `northwind_orm`.`employees` e1_0
order by
    1,
    2
limit
    ?;

-- Таблиця 5: orders
-- JPQL: select c.orderId, c.orderDate, c.shipCountry, c.freight from Orders c order by 1, 2
select
    o1_0.`OrderID`,
    o1_0.`OrderDate`,
    o1_0.`ShipCountry`,
    o1_0.`Freight`
from
    `northwind_orm`.`orders` o1_0
order by
    1,
    2
limit
    ?;

-- Таблиця 6: order details
-- JPQL: select c.id.orderId, c.id.productId, c.unitPrice, c.quantity, c.discount from OrderDetails c order by 1, 2
select
    od1_0.`OrderID`,
    od1_0.`ProductID`,
    od1_0.`UnitPrice`,
    od1_0.`Quantity`,
    od1_0.`Discount`
from
    `northwind_orm`.`order details` od1_0
order by
    1,
    2
limit
    ?;

-- Таблиця 7: suppliers
-- JPQL: select c.supplierId, c.companyName, c.city, c.country from Suppliers c order by 1, 2
select
    s1_0.`SupplierID`,
    s1_0.`CompanyName`,
    s1_0.`City`,
    s1_0.`Country`
from
    `northwind_orm`.`suppliers` s1_0
order by
    1,
    2
limit
    ?;

-- Таблиця 8: shippers
-- JPQL: select c.shipperId, c.companyName, c.phone from Shippers c order by 1, 2
select
    s1_0.`ShipperID`,
    s1_0.`CompanyName`,
    s1_0.`Phone`
from
    `northwind_orm`.`shippers` s1_0
order by
    1,
    2
limit
    ?;

-- Таблиця 9: region
-- JPQL: select c.regionId, trim(c.regionDescription) from Region c order by 1, 2
select
    r1_0.`RegionID`,
    trim(BOTH from r1_0.`RegionDescription`)
from
    `northwind_orm`.`region` r1_0
order by
    1,
    2
limit
    ?;

-- Таблиця 10: territories
-- JPQL: select c.territoryId, trim(c.territoryDescription), c.region.regionId from Territories c order by 1, 2
select
    t1_0.`TerritoryID`,
    trim(BOTH from t1_0.`TerritoryDescription`),
    t1_0.`RegionID`
from
    `northwind_orm`.`territories` t1_0
order by
    1,
    2
limit
    ?;

-- Таблиця 11: employee territories (@ManyToMany)
-- JPQL: select e.employeeId, c.territoryId from Employees e join e.territorieses c order by 1, 2
select
    e1_0.`EmployeeID`,
    t1_0.`TerritoryID`
from
    `northwind_orm`.`employees` e1_0
join
    `northwind_orm`.`employeeterritories` t1_0
        on e1_0.`EmployeeID`=t1_0.`EmployeeID`
order by
    1,
    2
limit
    ?;

-- Таблиця 12: customer demographics
-- JPQL: select c.customerTypeId, c.customerDesc from Customerdemographics c order by 1, 2
select
    c1_0.`CustomerTypeID`,
    c1_0.`CustomerDesc`
from
    `northwind_orm`.`customerdemographics` c1_0
order by
    1,
    2
limit
    ?;

-- Таблиця 13: customer demo (@ManyToMany)
-- JPQL: select e.customerId, c.customerTypeId from Customers e join e.customerdemographicses c order by 1, 2
select
    c1_0.`CustomerID`,
    c2_0.`CustomerTypeID`
from
    `northwind_orm`.`customers` c1_0
join
    `northwind_orm`.`customercustomerdemo` c2_0
        on c1_0.`CustomerID`=c2_0.`CustomerID`
order by
    1,
    2
limit
    ?;
