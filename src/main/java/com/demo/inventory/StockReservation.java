package com.demo.inventory;

public class StockReservation {

    public static int reserve(int available, int requested) {
        if (requested < 0) {
            throw new IllegalArgumentException("requested must be non-negative");
        }
        if (requested > available) {
            throw new IllegalStateException("cannot reserve more than available stock");
        }
        return available - requested;
    }
}
