package com.fulfilment.application.monolith.fulfilment;

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
class FulfilmentRepositoryTest {

    @Inject
    FulfilmentRepository repository;

    @Test
    @Transactional
    void testFindByProductAndStore() {

        Product product = new Product();
        product.persist();

        Store store = new Store();
        store.persist();

        List<ProductWarehouseStoreAssociation> result =
                repository.findByProductAndStore(product, store);

        assertNotNull(result);
    }

    @Test
    @Transactional
    void testFindByStore() {

        Store store = new Store();
        store.persist();

        List<ProductWarehouseStoreAssociation> result =
                repository.findByStore(store);

        assertNotNull(result);
    }

    @Test
    @Transactional
    void testFindByWarehouse() {

        Product product = new Product();
        product.persist();

        Store store = new Store();
        store.persist();

        ProductWarehouseStoreAssociation association =
                new ProductWarehouseStoreAssociation(product, 1L, store);

        repository.persist(association);

        List<ProductWarehouseStoreAssociation> result =
                repository.findByWarehouse(1L);

        assertNotNull(result);
    }

    @Test
    @Transactional
    void testCountWarehousesForProductInStore() {

        Product product = new Product();
        product.persist();

        Store store = new Store();
        store.persist();

        long count =
                repository.countWarehousesForProductInStore(product, store);

        assertTrue(count >= 0);
    }

    @Test
    @Transactional
    void testCountWarehousesForStore() {

        Store store = new Store();
        store.persist();

        long count =
                repository.countWarehousesForStore(store);

        assertTrue(count >= 0);
    }

    @Test
    @Transactional
    void testCountProductTypesInWarehouse() {

        long count =
                repository.countProductTypesInWarehouse(1L);

        assertTrue(count >= 0);
    }

    @Test
    @Transactional
    void testExists_WhenFalse() {

        Product product = new Product();
        product.persist();

        Store store = new Store();
        store.persist();

        boolean exists =
                repository.exists(product, 999L, store);

        assertFalse(exists);
    }

    @Test
    @TestTransaction
    void testExists_WhenTrue_AfterInsert() {

        Product product = new Product();
        product.persist();

        Store store = new Store();
        store.persist();

        ProductWarehouseStoreAssociation association =
                new ProductWarehouseStoreAssociation(product, 5L, store);

        repository.persist(association);

        boolean exists =
                repository.exists(product, 5L, store);

        assertTrue(exists);
    }
}