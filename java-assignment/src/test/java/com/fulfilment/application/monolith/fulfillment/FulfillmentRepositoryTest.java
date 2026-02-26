package com.fulfilment.application.monolith.fulfillment;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;

import java.util.List;

@QuarkusTest
class FulfillmentRepositoryTest {

    @Inject
    FulfillmentRepository repository;

    @Test
    @Transactional
    void testFindByProductAndStore() {

        Product product = Product.findById(1L);
        Store store = Store.findById(1L);

        List<ProductWarehouseStoreAssociation> result =
                repository.findByProductAndStore(product, store);

        assertNotNull(result);
    }

    @Test
    @Transactional
    void testFindByStore() {

        Store store = Store.findById(1L);

        List<ProductWarehouseStoreAssociation> result =
                repository.findByStore(store);

        assertNotNull(result);
    }

    @Test
    @Transactional
    void testFindByWarehouse() {

        List<ProductWarehouseStoreAssociation> result =
                repository.findByWarehouse("MWH.001");

        assertNotNull(result);
    }

    @Test
    @Transactional
    void testCountWarehousesForProductInStore() {

        Product product = Product.findById(1L);
        Store store = Store.findById(1L);

        long count = repository.countWarehousesForProductInStore(product, store);

        assertTrue(count >= 0);
    }

    @Test
    @Transactional
    void testCountWarehousesForStore() {

        Store store = Store.findById(1L);

        long count = repository.countWarehousesForStore(store);

        assertTrue(count >= 0);
    }

    @Test
    @Transactional
    void testCountProductTypesInWarehouse() {

        long count = repository.countProductTypesInWarehouse("MWH.001");

        assertTrue(count >= 0);
    }

    @Test
    @Transactional
    void testExists_WhenFalse() {

        Product product = Product.findById(1L);
        Store store = Store.findById(1L);

        boolean exists =
                repository.exists(product, "NON_EXISTING", store);

        assertFalse(exists);
    }

    @Test
    @TestTransaction
    void testExists_WhenTrue_AfterInsert() {

        Product product = Product.findById(1L);
        Store store = Store.findById(1L);

        ProductWarehouseStoreAssociation association =
                new ProductWarehouseStoreAssociation();

        association.product = product;
        association.store = store;
        association.warehouseBusinessUnitCode = "TEST_WH";

        repository.persist(association);

        boolean exists =
                repository.exists(product, "TEST_WH", store);

        assertTrue(exists);
    }
}