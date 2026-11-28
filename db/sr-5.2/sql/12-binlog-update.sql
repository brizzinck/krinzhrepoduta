/* Крок 3. Двійковий журнал: зміна даних, яка потрапить у журнал як Update_rows.
   Звідки: demo.sh, функція binlog(). Запускається в базі car_rental. 15-binlog-revert.sql повертає значення. */
UPDATE cars SET mileage_km = mileage_km + 100 WHERE car_id = 1;
