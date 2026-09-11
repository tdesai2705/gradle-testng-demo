package com.demo.pricing;

public class DiscountCalculator {

    private static final double MAX_PERCENT_OFF = 100.0;

    public static double applyDiscount(double price, double percentOff) {
        if (price < 0) {
            throw new IllegalArgumentException("price must be non-negative");
        }
        if (percentOff < 0 || percentOff > MAX_PERCENT_OFF) {
            throw new IllegalArgumentException("percentOff must be between 0 and 100");
        }
        return Math.round(price * (1 - percentOff / 100) * 100.0) / 100.0;
    }
}
