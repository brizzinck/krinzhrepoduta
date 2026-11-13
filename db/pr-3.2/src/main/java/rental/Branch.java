package rental;

/** Рядок таблиці branches. Jdbi заповнює запис за назвами стовпців (branch_id стає branchId). */
public record Branch(int branchId, String name, String city, String address, String phone) {
}
