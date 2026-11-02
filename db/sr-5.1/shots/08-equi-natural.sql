SELECT * FROM car_classes k INNER JOIN car_models m ON m.class_id = k.class_id LIMIT 5;
SELECT * FROM car_classes NATURAL JOIN car_models LIMIT 5;
SELECT COUNT(*) AS rows_inner FROM car_classes k INNER JOIN car_models m ON m.class_id = k.class_id;
SELECT COUNT(*) AS rows_natural FROM car_classes NATURAL JOIN car_models;
