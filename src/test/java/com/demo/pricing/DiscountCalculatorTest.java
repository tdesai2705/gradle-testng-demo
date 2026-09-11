package com.demo.pricing;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DiscountCalculatorTest {

    @Test
    public void testNoDiscount() {
        Assert.assertEquals(DiscountCalculator.applyDiscount(100.0, 0.0), 100.0);
    }

    @Test
    public void testHalfDiscount() {
        Assert.assertEquals(DiscountCalculator.applyDiscount(100.0, 50.0), 50.0);
    }

    @Test
    public void testFullDiscount() {
        Assert.assertEquals(DiscountCalculator.applyDiscount(100.0, 100.0), 0.0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNegativePriceRejected() {
        DiscountCalculator.applyDiscount(-1.0, 10.0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testOverHundredPercentRejected() {
        DiscountCalculator.applyDiscount(100.0, 150.0);
    }

    @Test
    public void testRoundingBehavior() {
        Assert.assertEquals(DiscountCalculator.applyDiscount(10.0, 33.0), 6.7);
    }
}
