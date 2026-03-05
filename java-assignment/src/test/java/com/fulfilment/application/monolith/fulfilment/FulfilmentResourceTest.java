package com.fulfilment.application.monolith.fulfilment;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;

import java.util.List;

@QuarkusTest
class FulfilmentResourceTest {

    @Inject
    FulfilmentResource resource;

    @Inject
    ProductRepository productRepository;

    @Inject
    FulfilmentRepository fulfilmentRepository;

    // -------------------- CREATE TESTS --------------------

    @Test
    void testCreateAssociation_InvalidProductId() {
        FulfilmentAssociationRequest request = new FulfilmentAssociationRequest();
        request.productId = -1L;
        request.storeId = 1L;
        request.warehouseId = 1L;

        assertThrows(WebApplicationException.class,
                () -> resource.createAssociation(request));
    }

    @Test
    void testCreateAssociation_InvalidWarehouseId() {
        FulfilmentAssociationRequest request = new FulfilmentAssociationRequest();
        request.productId = 1L;
        request.storeId = 1L;
        request.warehouseId = null;

        assertThrows(WebApplicationException.class,
                () -> resource.createAssociation(request));
    }

    @Test
    void testCreateAssociation_InvalidStoreId() {
        FulfilmentAssociationRequest request = new FulfilmentAssociationRequest();
        request.productId = 1L;
        request.storeId = -1L;
        request.warehouseId = 1L;

        assertThrows(WebApplicationException.class,
                () -> resource.createAssociation(request));
    }

    @Test
    void testCreateAssociation_ProductNotFound() {
        FulfilmentAssociationRequest request = new FulfilmentAssociationRequest();
        request.productId = 9999L;
        request.storeId = 1L;
        request.warehouseId = 1L;

        assertThrows(WebApplicationException.class,
                () -> resource.createAssociation(request));
    }

    // -------------------- GET ASSOCIATIONS --------------------

    @Test
    void testGetAssociations_InvalidParams() {
        assertThrows(WebApplicationException.class,
                () -> resource.getAssociations(null, null));
    }

    @Test
    void testGetAssociations_ProductNotFound() {
        assertThrows(WebApplicationException.class,
                () -> resource.getAssociations(9999L, 1L));
    }

    @Test
    @Transactional
    void testGetAssociations_Valid() {

        Product product = new Product();
        product.persist();

        Store store = new Store();
        store.persist();

        List<ProductWarehouseStoreAssociation> result =
                resource.getAssociations(product.id, store.id);

        assertNotNull(result);
    }

    @Test
    void testGetStoreAssociations_NotFound() {
        assertThrows(WebApplicationException.class,
                () -> resource.getStoreAssociations(9999L));
    }

    @Test
    @Transactional
    void testGetStoreAssociations_Valid() {

        Store store = new Store();
        store.persist();

        List<ProductWarehouseStoreAssociation> result =
                resource.getStoreAssociations(store.id);

        assertNotNull(result);
    }

    @Test
    @Transactional
    void testGetWarehouseAssociations() {

        Product product = new Product();
        product.persist();

        Store store = new Store();
        store.persist();

        // create association so warehouse has data
        ProductWarehouseStoreAssociation association =
                new ProductWarehouseStoreAssociation(product, 10L, store);

        fulfilmentRepository.persist(association);

        List<ProductWarehouseStoreAssociation> result =
                resource.getWarehouseAssociations(String.valueOf(10L));

        assertNotNull(result);
    }

    // -------------------- DELETE --------------------

    @Test
    @Transactional
    void testDeleteAssociation_NotFound() {
        assertThrows(WebApplicationException.class,
                () -> resource.deleteAssociation(9999L));
    }

    @Test
    @TestTransaction
    void testDeleteAssociation_Valid() {

        Product product = new Product();
        product.persist();

        Store store = new Store();
        store.persist();

        ProductWarehouseStoreAssociation association =
                new ProductWarehouseStoreAssociation(product, 20L, store);

        fulfilmentRepository.persist(association);

        resource.deleteAssociation(association.id);

        ProductWarehouseStoreAssociation deleted =
                fulfilmentRepository.findById(association.id);

        assertNull(deleted);
    }

    // -------------------- CONSTRAINTS --------------------

    @Test
    void testGetConstraints() {
        FulfilmentConstraints constraints =
                resource.getConstraints();

        assertNotNull(constraints);
    }
}