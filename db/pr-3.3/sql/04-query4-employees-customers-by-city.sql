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
