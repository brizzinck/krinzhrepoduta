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
