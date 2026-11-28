/* Крок 3. Двійковий журнал: зворотна зміна, щоб дані car_rental лишилися без змін.
   Звідки: demo.sh, функція binlog(), кінець. Запускається в базі car_rental. */
UPDATE cars SET mileage_km = mileage_km - 100 WHERE car_id = 1;
