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
