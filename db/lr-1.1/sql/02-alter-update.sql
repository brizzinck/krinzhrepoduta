-- Крок 2. Зміна структури cars (колонка color) і зміна наявних даних.
ALTER TABLE cars ADD COLUMN color VARCHAR(20) NULL;
UPDATE cars SET color = 'білий' WHERE car_id IN (2, 3, 4);
UPDATE cars SET color = 'чорний' WHERE car_id = 5;
SELECT car_id, plate_number, color FROM cars WHERE color IS NOT NULL;
