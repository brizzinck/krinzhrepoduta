SELECT constraint_type, COUNT(*) AS cnt FROM information_schema.table_constraints WHERE table_schema = 'car_rental' GROUP BY constraint_type;
SELECT delete_rule, COUNT(*) AS cnt FROM information_schema.referential_constraints WHERE constraint_schema = 'car_rental' GROUP BY delete_rule;
