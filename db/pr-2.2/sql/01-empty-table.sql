-- Крок 1 (методичка, крок 01). Порожня таблиця без ключа: InnoDB кластеризує її за прихованим GEN_CLUST_INDEX.
CREATE TABLE test_index (
  id INT NOT NULL,
  pole1 CHAR(36) CHARACTER SET ascii NOT NULL,
  pole2 CHAR(216) CHARACTER SET ascii NOT NULL
);

