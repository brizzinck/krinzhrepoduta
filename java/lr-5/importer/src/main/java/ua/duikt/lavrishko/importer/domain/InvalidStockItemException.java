package ua.duikt.lavrishko.importer.domain;

/**
 * Порушено правило предметної області: залишок товару з такими даними існувати не може.
 * Перевірюваний виняток: той, хто створює StockItem, зобов'язаний вирішити, що робити з помилкою.
 */
public class InvalidStockItemException extends Exception {

    public InvalidStockItemException(String message) {
        super(message);
    }
}
