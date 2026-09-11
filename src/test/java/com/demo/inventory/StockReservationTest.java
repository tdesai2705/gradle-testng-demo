package com.demo.inventory;

import org.testng.Assert;
import org.testng.annotations.Test;

public class StockReservationTest {

    @Test
    public void testReserveWithinStock() {
        Assert.assertEquals(StockReservation.reserve(10, 4), 6);
    }

    @Test
    public void testReserveExactStock() {
        Assert.assertEquals(StockReservation.reserve(10, 10), 0);
    }

    @Test
    public void testReserveZero() {
        Assert.assertEquals(StockReservation.reserve(10, 0), 10);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNegativeRequestRejected() {
        StockReservation.reserve(10, -1);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testOverReserveRejected() {
        StockReservation.reserve(10, 11);
    }
}
