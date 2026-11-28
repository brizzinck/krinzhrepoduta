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
