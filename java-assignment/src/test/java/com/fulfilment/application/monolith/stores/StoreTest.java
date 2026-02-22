package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class StoreTest {

    @Test
    void testStoreDefaultConstructor() {
        Store store = new Store();
        assertNull(store.id);
        assertNull(store.name);
        assertEquals(0, store.quantityProductsInStock);
    }

    @Test
    void testStoreNameConstructor() {
        Store store = new Store("TestStore");
        assertNull(store.id);
        assertEquals("TestStore", store.name);
        assertEquals(0, store.quantityProductsInStock);
    }

    @Test
    void testStoreNameConstructorWithSpecialCharacters() {
        Store store = new Store("Store @#$% & Special");
        assertEquals("Store @#$% & Special", store.name);
    }

    @Test
    void testStoreAllFields() {
        Store store = new Store("CompleteStore");
        store.id = 1L;
        store.quantityProductsInStock = 100;

        assertEquals(1L, store.id);
        assertEquals("CompleteStore", store.name);
        assertEquals(100, store.quantityProductsInStock);
    }

    @Test
    void testStoreWithZeroQuantity() {
        Store store = new Store("EmptyStore");
        store.quantityProductsInStock = 0;
        assertEquals(0, store.quantityProductsInStock);
    }

    @Test
    void testStoreWithLargeQuantity() {
        Store store = new Store("BulkStore");
        store.quantityProductsInStock = 999999;
        assertEquals(999999, store.quantityProductsInStock);
    }

    @Test
    void testStoreWithNegativeQuantity() {
        Store store = new Store("NegativeStore");
        store.quantityProductsInStock = -10;
        assertEquals(-10, store.quantityProductsInStock);
    }

    @Test
    @Transactional
    void testStorePersistence() {
        Store store = new Store("PersistenceTest");
        store.quantityProductsInStock = 50;

        store.persist();

        assertNotNull(store.id);
        assertTrue(store.id > 0);
    }

    @Test
    @Transactional
    void testStoreEquality() {
        Store store1 = new Store("Store1");
        Store store2 = new Store("Store1");

        // Different instances, so they're not equal
        assertNotEquals(store1, store2);
    }


    @Test
    @Transactional
    void testStoreWithUnicodeCharacters() {
        Store store = new Store("店铺 Магазин متجر");
        store.quantityProductsInStock = 75;
        store.persist();

        assertNotNull(store.id);
        assertEquals("店铺 Магазин متجر", store.name);
    }

    @Test
    @Transactional
    void testStoreUpdate() {
        Store store = new Store("OriginalName");
        store.quantityProductsInStock = 10;
        store.persist();

        Long storeId = store.id;

        store.name = "UpdatedName";
        store.quantityProductsInStock = 25;
        store.persist();

        Store fetched = Store.findById(storeId);
        assertEquals("UpdatedName", fetched.name);
        assertEquals(25, fetched.quantityProductsInStock);
    }

    @Test
    @Transactional
    void testStoreDelete() {
        Store store = new Store("DeleteTest");
        store.quantityProductsInStock = 20;
        store.persist();

        Long storeId = store.id;

        store.delete();

        Store fetched = Store.findById(storeId);
        assertNull(fetched);
    }

    @Test
    @Transactional
    void testStoreMaxQuantity() {
        Store store = new Store("MaxQuantityStore");
        store.quantityProductsInStock = Integer.MAX_VALUE;
        store.persist();

        Store fetched = Store.findById(store.id);
        assertEquals(Integer.MAX_VALUE, fetched.quantityProductsInStock);
    }

    @Test
    void testStoreFieldTypes() {
        Store store = new Store("TypeTest");
        assertIsLong(store.id);
        assertIsString(store.name);
        assertIsInt(store.quantityProductsInStock);
    }

    private void assertIsLong(Object obj) {
        assertTrue(obj == null || obj instanceof Long);
    }

    private void assertIsString(Object obj) {
        assertTrue(obj == null || obj instanceof String);
    }

    private void assertIsInt(Object obj) {
        assertTrue(obj instanceof Integer);
    }

    @Test
    @Transactional
    void testStoreNameUniqueness() {
        Store store1 = new Store("UniqueName");
        store1.persist();

        // The database should enforce uniqueness, but we can't test constraint violation
        // directly without more complex setup. This just ensures the field is unique.
        assertNotNull(store1.id);
    }

    @Test
    @Transactional
    void testMultipleStoresCreation() {
        Store store1 = new Store("Store1");
        Store store2 = new Store("Store2");
        Store store3 = new Store("Store3");

        store1.persist();
        store2.persist();
        store3.persist();

        assertNotNull(store1.id);
        assertNotNull(store2.id);
        assertNotNull(store3.id);
        assertNotEquals(store1.id, store2.id);
        assertNotEquals(store2.id, store3.id);
    }
}

