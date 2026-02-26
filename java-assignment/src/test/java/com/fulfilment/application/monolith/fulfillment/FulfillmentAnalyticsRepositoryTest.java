package com.fulfilment.application.monolith.fulfillment;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class FulfillmentAnalyticsRepositoryTest {

    @Inject
    FulfillmentAnalyticsRepository repository;

    @Test
    void testCountAssociationsForStore() {

        long result = repository.countAssociationsForStore(1L);

        assertNotNull(result);
        assertTrue(result >= 0);
    }

    @Test
    void testCountAssociationsForProduct() {

        long result = repository.countAssociationsForProduct(1L);

        assertNotNull(result);
        assertTrue(result >= 0);
    }

    @Test
    void testGetAverageWarehousesPerProduct() {

        double result = repository.getAverageWarehousesPerProduct();

        assertNotNull(result);
        assertTrue(result >= 0.0);
    }
}