package com.fulfilment.application.monolith.products;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class ProductTest {

    @Test
    void testProductDefaultConstructor() {
        Product product = new Product();
        assertNull(product.id);
        assertNull(product.name);
        assertNull(product.description);
        assertNull(product.price);
        assertEquals(0, product.stock);
    }

    @Test
    void testProductNameConstructor() {
        Product product = new Product("TestProduct");
        assertNull(product.id);
        assertEquals("TestProduct", product.name);
        assertNull(product.description);
        assertNull(product.price);
        assertEquals(0, product.stock);
    }

    @Test
    void testProductNameConstructorWithSpecialCharacters() {
        Product product = new Product("Product @#$% & Special");
        assertEquals("Product @#$% & Special", product.name);
    }

    @Test
    void testProductAllFields() {
        Product product = new Product("CompleteProduct");
        product.id = 1L;
        product.description = "A complete product";
        product.price = new BigDecimal("99.99");
        product.stock = 50;

        assertEquals(1L, product.id);
        assertEquals("CompleteProduct", product.name);
        assertEquals("A complete product", product.description);
        assertEquals(new BigDecimal("99.99"), product.price);
        assertEquals(50, product.stock);
    }

    @Test
    void testProductWithNullDescription() {
        Product product = new Product("NullDescProduct");
        product.description = null;
        assertNull(product.description);
    }

    @Test
    void testProductWithZeroPrice() {
        Product product = new Product("FreeProduct");
        product.price = new BigDecimal("0.00");
        assertEquals(new BigDecimal("0.00"), product.price);
    }

    @Test
    void testProductWithLargePrice() {
        Product product = new Product("ExpensiveProduct");
        product.price = new BigDecimal("999999.99");
        assertEquals(new BigDecimal("999999.99"), product.price);
    }

    @Test
    void testProductWithZeroStock() {
        Product product = new Product("OutOfStockProduct");
        product.stock = 0;
        assertEquals(0, product.stock);
    }

    @Test
    void testProductWithLargeStock() {
        Product product = new Product("BulkProduct");
        product.stock = 999999;
        assertEquals(999999, product.stock);
    }

    @Test
    void testProductWithNegativeStock() {
        Product product = new Product("NegativeStockProduct");
        product.stock = -5;
        assertEquals(-5, product.stock);
    }

    @Test
    void testProductEquality() {
        Product product1 = new Product("Product1");
        Product product2 = new Product("Product1");

        // Different instances, so they're not equal
        assertNotEquals(product1, product2);
    }

    @Test
    void testProductWithLongName() {
        Product product = new Product("VeryLongProductNameThatShouldBeStoredCorrectly");
        product.price = new BigDecimal("25.50");
        product.stock = 100;

        assertNotNull(product.name);
        assertEquals("VeryLongProductNameThatShouldBeStoredCorrectly", product.name);
    }

    @Test
    void testProductWithUnicodeCharacters() {
        Product product = new Product("产品 Продукт محصول");
        product.price = new BigDecimal("75.00");
        product.stock = 15;

        // id is only set when persisted; check the name is stored correctly
        assertNotNull(product.name);
        assertEquals("产品 Продукт محصول", product.name);
    }

    @Test
    void testProductPrecisionPrice() {
        Product product = new Product("PrecisionTest");
        // Test with specific decimal places (precision 10, scale 2)
        product.price = new BigDecimal("1234567.89");
        assertEquals(new BigDecimal("1234567.89"), product.price);
    }

    @Test
    void testProductFieldTypes() {
        Product product = new Product("TypeTest");
        assertIsLongOrNull(product.id);
        assertIsString(product.name);
        assertIsInt(product.stock);
    }

    private void assertIsLongOrNull(Object obj) {
        assertTrue(obj == null || obj instanceof Long);
    }

    private void assertIsString(Object obj) {
        assertTrue(obj == null || obj instanceof String);
    }

    private void assertIsInt(Object obj) {
        assertTrue(obj instanceof Integer);
    }
}
