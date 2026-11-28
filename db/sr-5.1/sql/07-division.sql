-- Крок 7. Ділення через подвійне NOT EXISTS: клієнти, що орендували і «Економ», і «Преміум».
-- 11. Ділення
SELECT class_id, name FROM car_classes WHERE name IN ('Економ', 'Преміум');

SELECT c.customer_id, c.last_name
FROM customers c
WHERE NOT EXISTS (
  SELECT 1 FROM car_classes k
  WHERE k.name IN ('Економ', 'Преміум')
    AND NOT EXISTS (
      SELECT 1 FROM rentals r
      JOIN cars a ON a.car_id = r.car_id
      JOIN car_models m ON m.model_id = a.model_id
      WHERE r.customer_id = c.customer_id AND m.class_id = k.class_id AND r.status <> 'cancelled'));
