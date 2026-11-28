-- Крок 1. Функція над стовпцем заважає індексу, діапазон ні. Дивись type і rows у EXPLAIN.
-- Підготовка: індекс по даті замовлення, потім свіжа статистика.
CREATE INDEX idx_orders_date ON orders (OrderDate);
ANALYZE TABLE orders;

-- До: YEAR() над стовпцем, індекс читається повністю (type index, rows 830).
EXPLAIN SELECT OrderID FROM orders WHERE YEAR(OrderDate) = 1997;

-- Зміна: умову переписано з YEAR(OrderDate) = 1997 на діапазон дат, стовпець без функції.
-- Після: діапазон дат, індекс працює як seek (type range, rows 408).
EXPLAIN SELECT OrderID FROM orders WHERE OrderDate >= '1997-01-01' AND OrderDate < '1998-01-01';
