-- 9. ON DELETE SET DEFAULT: MySQL приймає опис, але InnoDB дію не виконує (видалення батьківського рядка відхиляється)
INSERT INTO branches (branch_id, name, city, address, phone) VALUES (5, 'Тестовий пункт', 'Харків', 'вул. Тестова, 1', '+380575550105');
CREATE TABLE demo_set_default (
    id INT PRIMARY KEY,
    branch_id INT NOT NULL DEFAULT 1,
    FOREIGN KEY (branch_id) REFERENCES branches (branch_id) ON DELETE SET DEFAULT
);
INSERT INTO demo_set_default (id, branch_id) VALUES (1, 5);
DELETE FROM branches WHERE branch_id = 5;
SELECT * FROM demo_set_default;
